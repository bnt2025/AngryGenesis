import socket
import os
import signal
import select
import errno
import decoder
import time
import datetime
import json
import sys
from libmich.formats import IEEE802154
from struct import unpack
from time import strftime, localtime, sleep
from binascii import hexlify
from CC2531 import CHANNELS
from libmich.formats.IEEE802154 import TI_USB, TI_CC, IEEE802154
from gps_thread import GPSThread

# export filtering
__all__ = ['interpreter']

# this is to customize another 802.15.4 frame decoder
DECODER = IEEE802154
# this is the default CC2531 behavior
DECODER.PHY_INCL = False
DECODER.FCS_INCL = False


def LOG(msg=''):
    print('[interpreter] %s' % msg)


class interpreter(object):
    # debug level
    DEBUG = 1
    # for interrupt handler and looping control
    _THREADED = False
    _STOP_EVENT = None

    #SOCK_ADDR = '/tmp/cc2531_sniffer'
    SOCK_ADDR = ('127.10.0.1', 2154)

    # select loop and socket recv settings
    SELECT_TO = 0.5
    SOCK_BUFLEN = 1024

    # interpreter output (stdout and/or file)
    OUTPUT_STDOUT = True
    #OUTPUT_FILE = None
    OUTPUT_FILE = '/tmp/cc2531_sniffer'
    # output even when the FCS check fails
    FCS_IGNORE = False

    def __init__(self):
        # create the socket server
        if isinstance(self.SOCK_ADDR, str):
            self._create_file_serv()
        elif isinstance(self.SOCK_ADDR, tuple) and len(self.SOCK_ADDR) == 2 \
                and isinstance(self.SOCK_ADDR[0], str) and isinstance(self.SOCK_ADDR[1], int):
            self._create_udp_serv()
        else:

            raise(Exception('bad SOCK_ADDR parameter'))

        self.gps = GPSThread()
        self.gps.useGPSD()
        self.gps.start()
        # catch CTRL+C
        if not self._THREADED:
            def serv_int(signum, frame):
                self.stop()
                LOG('SIGINT: quitting')
            signal.signal(signal.SIGINT, serv_int)

        # check output parameters
        if self.OUTPUT_FILE:
            try:
                pass
            except IOError:
                self.OUTPUT_FILE = None

        # init empty message struct
        self._cur_msg = {}
        self._processing = False

    def _log(self, msg=''):
        LOG(msg)

    def _create_file_serv(self):
        try:
            os.unlink(self.SOCK_ADDR)
        except OSError:
            if os.path.exists(self.SOCK_ADDR):
                raise(Exception('cannot clean %s' % self.SOCK_ADDR))
        # serv on the file
        sk = socket.socket(socket.AF_UNIX, socket.SOCK_DGRAM)
        try:
            sk.bind(self.SOCK_ADDR)
        except socket.error:
            raise(Exception('cannot clean %s' % addr))
        if self.DEBUG:
            self._log('server listening on %s' % self.SOCK_ADDR)
        self._sk = sk

    def _create_udp_serv(self):
        # serv on UDP port
        sk = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            sk.bind(self.SOCK_ADDR)
        except socket.error:
            raise(Exception('cannot bind on UDP port %s' % list(self.SOCK_ADDR)))
        if self.DEBUG:
            self._log('server listening on %s' % list(self.SOCK_ADDR))
        self._sk = sk

    def stop(self):
        self._processing = False

        if self.gps is not None:
            self.gps.stop()
        sleep(0.2)
        self._sk.close()

    def output(self, line=''):
        if self.OUTPUT_STDOUT:
            sys.stdout.write(line)
            sys.stdout.write("\n")
            sys.stdout.flush()
        if self.OUTPUT_FILE:
            try:
                fd = open(self.OUTPUT_FILE, 'a')
            except IOError:
                pass
            else:
                fd.write('%s\n' % line)
                fd.close()

    def looping(self):
        if not self._processing:
            return False
        else:
            if not self._THREADED:
                return True
            elif hasattr(self._STOP_EVENT, 'is_set') \
                    and not self._STOP_EVENT.is_set():
                return True
            return False

    def process(self):
        # loop on recv()
        self._processing = True
        while self.looping():
            try:
                r = select.select([self._sk], [], [], self.SELECT_TO)[0]
            except select.error as e:
                if e.args[0] == errno.EINTR:
                    self._processing = False
                else:
                    pass
            else:
                for sk in r:
                    msg = sk.recv(self.SOCK_BUFLEN)
                    while len(msg) >= 4:
                        frame_len = unpack('!I', msg[:4])[0]
                        frame = msg[4:4+frame_len]
                        self.interpret(frame)
                        msg = msg[4+frame_len:]

    def get_dtg(self):
        return str(datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f"))

    def interpret(self, msg=''):
        self._cur_msg = {}
        # parse it into the structure
        while len(msg) > 0:
            msg = self._get_tlv(msg)
        # output it nicely
        if 'frame' in self._cur_msg \
                and "timestamp" in self._cur_msg \
                and "channel" in self._cur_msg:
            if self._cur_msg["FCS_OK"]:
                fcschk = "OK"
            else:
                fcschk = "error"
            loc = self.gps.getLastGoodGPSFix()
            data = {
                "timestamp": self.get_dtg(),
                "type": "zigbee",
                "tx_id": "1234",
                "rx_id": "4321",
                "band": "7",
                "channel": self._cur_msg["channel"],
                "rssi": self._cur_msg["RSSI"],
                "location": {"lat": loc["Latitude"], "lon": loc['Longitude']},
                "localname": "TI CC2531",
                "codename": "Zigbee Survey",
                "deviceid": "12345",
                "frame": hexlify(self._cur_msg["frame"])
            }

            self.output(json.dumps(data))

    def _get_tlv(self, msg=''):
        if len(msg) > 2:
            T, L = unpack('!BH', msg[0:3])
            if L and len(msg) >= 3+L:
                V = msg[3:3+L]
            elif L:
                if self.DEBUG:
                    self._log('corrupted message')
                return ''
            self._interpret_TV(T, V)
            return msg[3+L:]
        else:
            if self.DEBUG:
                self._log('corrupted message')
            return ''

    def _interpret_TV(self, T=0, V=''):
        if T == 1:
            self._cur_msg["channel"] = ord(V[0])
            self.makejson(self._cur_msg)
        elif T == 2:
            self._cur_msg["timestamp"] = float(V)
        elif T == 3:
            # TODO: check exactly how GPS position is computed
            self._cur_msg["position"] = V
        elif T == 0x10:
            # TI_PSD structure
            self._interpret_TI_USB(V)
        elif T == 0x20:
            self._cur_msg["frame"] = V
            mac = self.DECODER()
            mac.parse(V)
            self._cur_msg["MAC"] = mac
            self.makejson(mac)

    def _interpret_TI_USB(self, V=" "):
        usb = TI_USB()
        try:
            usb.map(V)
        except:
            return
        # process only 802.15.4 frames with correct checksum,
        # or process all frames if FCS is ignored
        if self.FCS_IGNORE or usb.TI_CC.FCS():
            self._cur_msg["dev_ts"] = usb.TS()
            self._cur_msg["RSSI"] = usb.TI_CC.RSSI()
            self._cur_msg["frame"] = usb.TI_CC.Payload()
            self._cur_msg["FCS_OK"] = usb.TI_CC.FCS()
            mac = DECODER()
            try:
                mac.parse(self._cur_msg["frame"])
            except:
                mac = " "
            self._cur_msg["MAC"] = mac

    def makejson(self, mac):

        data = {
            "timestamp": "DDHHMM",
            "type": "zigbee",
            "tx_id": "1234",
            "rx_id": "4321",
            "band": 7,
            "channel": 7,
            "rssi": 8,
            "location": {
                    "lat": 50,
                    "lon": 50
            },
            "localname": "iot sniffer",
            "codename": "Test Capture",
            "deviceid": "1234",
            "rawframe": str(mac)
        }
