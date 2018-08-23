import os
import socket
import signal
import argparse
import json
import datetime
from time import time, sleep
from binascii import hexlify, unhexlify
from threading import Thread, Event
from CC2531 import *
from receiver import *
from interpreter import *

def LOG(msg=''):
    print('[sniffer] %s' % msg)


def create_file_serv(addr):
    # Make sure the socket does not already exist
    try:
        os.unlink(addr)
    except OSError:
        if os.path.exists(addr):
            raise(Exception('cannot clean %s' % addr))
    # serv on file
    sk = socket.socket(socket.AF_UNIX, socket.SOCK_DGRAM)
    try:
        sk.bind(addr)
    except socket.error:
        raise(Exception('cannot clean %s' % addr))
    return sk


def create_udp_serv(addr):
    # serv on UDP port
    sk = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        sk.bind(addr)
    except socket.error:
        raise(Exception('cannot bind on UDP port %s' % list(addr)))
    return sk


def test_receiver(x=0):
    cc = CC2531(get_CC2531()[x])
    receiver.DEBUG = 1
    receiver.SOCK_ADDR = ('localhost', 2154)
    receiver.CHAN_LIST = [0x0f, 0x14, 0x19]
    receiver.CHAN_PERIOD = 10
    serv = create_udp_serv(receiver.SOCK_ADDR)
    s = receiver(cc)
    s.listen()

###
# Multi-receiver for multi-threaded execution
###


def threadit(task, *args, **kwargs):
    th = Thread(target=task, args=args, kwargs=kwargs)
    th.daemon = True
    th.start()
    return th


def prepare_receiver(chans=[0x0f, 0x14, 0x19]):
    ccs = map(CC2531, get_CC2531())
    #
    if len(ccs) == 0:
        LOG(' no CC2531 dongles found')
        return []

    # split the chans' list into separate lists for all receivers
    e, r = len(chans)//len(ccs), len(chans) % len(ccs)
    cl = []
    start, stop = 0, 0
    for i in range(len(ccs)):
        if stop > 0:
            start = stop
        if i < r:
            stop = start + e + 1
        else:
            stop = start + e
        cl.append(chans[start:stop])
    ss = [receiver(cc) for cc in ccs]
    for i in range(len(cl)):
        # configure channels' list of each CC2531 receiver
        ss[i].CHAN_LIST = cl[i]
    #
    return ss

###
# Main program
###

def prolog():
    # command line handler
    parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter,
                                     description='Use TI CC2531 dongles to sniff on IEEE 802.15.4 channels.\n'
                                     'Forward all sniffed frames over the network (UDP/2154 port).\n'
                                     'Each frame is packed with a list of TLV fields:\n'
                                     '\tTag : uint8, Length : uint8, Value : char*[L]\n'
                                     '\tT=0x01, 802.15.4 channel, uint8\n'
                                     '\tT=0x02, epoch time at frame reception, ascii encoded\n'
                                     '\tT=0x03, position at frame reception (if positionning server available)\n'
                                     '\tT=0x10, 802.15.4 frame within TI PSD structure\n'
                                     '\tT=0x20, 802.15.4 frame\n'
                                     'Output 802.15.4 frame information (channel, RSSI, MAC header, ...)')
    #
    parser.add_argument('-d', '--debug', type=int, default=0,
                        help='debug level (0: silent, 3: very verbose)')
    parser.add_argument('-c', '--chans', nargs='*', type=int, default=range(11, 27),
                        help='list of IEEE 802.15.4 channels to sniff on (between 11 and 26)')
    parser.add_argument('-p', '--period', type=float, default=1.0,
                        help='time (in seconds) to sniff on a single channel before hopping')
    parser.add_argument('-n', '--nofcschk', action='store_true', default=False,
                        help='displays all sniffed frames, even those with failed FCS check')
    parser.add_argument('--ip', type=str, default='localhost',
                        help='network destination for forwarding 802.15.4 frames')
    parser.add_argument('--filesock', action='store_true', default=False,
                        help='forward 802.15.4 frames to a UNIX file socket /tmp/cc2531_server '
                        'instead of the UDP socket')
    parser.add_argument('-f', '--file', action='store_true', default=False,
                        help='output (append) frame information to file /tmp/cc2531_sniffer')
    parser.add_argument('-s', '--silent', action='store_true', default=False,
                        help='do not print frame information on stdout')
    #
    args = parser.parse_args()
    #
    if args.debug:
        LOG(' command line arguments:\n%s' % repr(args))
    CC2531.DEBUG = max(0, args.debug-2)
    receiver.DEBUG = max(0, args.debug-1)
    interpreter.DEBUG = args.debug

    # Writes data to a socket
    receiver.CHAN_PERIOD = args.period
    if args.filesock:
        receiver.SOCK_ADDR = 'logjsonl'
    else:
        receiver.SOCK_ADDR = (args.ip, 2154)
    #
    chans = [c for c in args.chans if 11 <= c <= 26]
    if chans == []:
        chans = CHANNELS.keys()

    # Writes data to a file.
    interpreter.SOCK_ADDR = receiver.SOCK_ADDR
    if args.file:
        interpreter.OUTPUT_FILE = datetime.datetime.now().strftime("%Y%m%d-%H%M%S_iot.jsonl")
    else:
        interpreter.OUTPUT_FILE = None
    interpreter.OUTPUT_STDOUT = not args.silent
    interpreter.FCS_IGNORE = args.nofcschk
    return chans

def main():
    global running
    running = False
    chans = prolog()
    threads = []
    stop_event = Event()
    interpreter._THREADED = True
    interpreter._STOP_EVENT = stop_event
    receiver._THREADED = True
    receiver._STOP_EVENT = stop_event

    def int_handler(signum, frame):
        print('SIGINT: quitting')
        stop_event.set()
        global running
        running = False
    signal.signal(signal.SIGINT, int_handler)

    running = True
    # start interpreter (/server)
    interp = interpreter()
    threads.append((interp, threadit(interp.process)))

    # start CC2531 receivers
    ccs = prepare_receiver(chans)
    for cc in ccs:
        threads.append((cc, threadit(cc.listen)))

    # loop infinitely until SIGINT is caught
    # this loop lets all daemonized threads running
    while running:
        sleep(1)

    # finally, wait for each thread to stop properly after they received
    # the stop_event signal
    for c, t in threads:
        t.join()


if __name__ == '__main__':
    main()
   
   