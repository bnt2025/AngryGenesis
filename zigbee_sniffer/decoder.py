import sys
from struct import unpack
from time import strftime, localtime
from libmich.formats import pcap
from libmich.formats import IEEE802154
from libmich.core.element import Int

DECODER = IEEE802154.IEEE802154
DECODER.PHY_INCL = False
DECODER.FCS_INCL = False

Tags = {
    0x01: 'channel',
    0x02: 'time',
    0x03: 'position',
    0x10: 'TI_PSD with 802.15.4 frame',
    0x20: '802.15.4 frame',
}


def process_pcap(pcap_file='test.pcap'):
    try:
        s = open(pcap_file, 'rb').read()
    except IOError:
        print('ERROR: cannot open file')
        return
    print('pcap file length: %i bytes\n' % len(s))
    Int._endian = 'little'
    glob = pcap.Global()
    glob.parse(s)
    s = s[len(glob):]
    print('pcap global header:\n%s\n' % glob.show())
    while len(s) > 16:
        s = chk_record(s)


def chk_record(s):
    Int._endian = 'little'
    rec = pcap.Record()
    rec.parse(s)
    l = rec.incl_len()
    process_packet(s[16:16+l])
    s = s[16+l:]
    return s


def process_packet(buf):
    dstport = (ord(buf[36]) << 8) + ord(buf[37])
    if dstport != 2154:
        return
    buf = buf[42:]
    if len(buf) > 40:
        print('[+] packet received:')
    else:
        print('[-] packet too short... strange')
        return
    while len(buf) >= 4:
        frame_len = unpack('!I', buf[:4])[0]
        frame = msg[4:4+frame_len]
        while len(frame) > 2:
            frame = chk_tlv(buf)
            print(30*'-')
        msg = msg[4+frame_len:]


def chk_tlv(buf):
    Int._endian = 'big'
    T, L = unpack('!BH', msg[0:3])
    if L:
        V = buf[3:3+L]
        if T == 1:
            print('channel: %i' % ord(V[0]))
        elif T == 2:
            print('time: %s' % strftime('%Y-%m-%d %H:%M:%S', localtime(float(V))))
        elif T == 3:
            print('position (GPRMC): %s' % V)
        elif T == 0x10:
            psd = IEEE802154.TI_USB()
            psd.parse(V)
            print('TI USB structure:\n%s' % usb.show())
            data = usb.TI_CC.Payload()
            if len(data) >= 2:
                frame = DECODER()
                try:
                    frame.parse(psd.Data())
                    print('IEEE 802.15.4 frame:\n%s' % frame.show())
                except:
                    print('IEEE 802.15.4 frame: -decoder error-')
        elif T == 0x20:
            frame = DECODER()
            try:
                frame.parse(V)
                print('IEEE 802.15.4 frame:\n%s' % frame.show())
            except:
                print('IEEE 802.15.4 frame: -decoder error-')
    return buf[2+L:]


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('%s:    please provide path to captured pcap file' % sys.argv[0])
        exit()
    process_pcap(sys.argv[1])
