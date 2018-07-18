# ANGRY GENESIS

ANGRY GENESIS is an 802.15.4 Zigbee survey tool using a CC2531 usb dongle whilst geo-tagging the collection data. The software comprises of a python backend feeding a Java packet decryptor with an ELK stack displaying the data captured.

The CC2531 module works 'out the box' and does not require the firmware to be reflashed.

## Getting Started

To use ANGRY GENESIS ensure that you have the following hardware;

* Texus Instrumemts CC2531 Zigbee USB sniffer - http://www.ti.com/tool/CC2531EMK
* G-Star IV USB GPS adaptor - https://wholesaler.alibaba.com/product-detail/Wholesale-GlobalSat-BU-353S4-Cable-USB_60105188624.html

### Prerequisites

ANGRY GENESIS requires the following packages to be installed;

* GPSD: http://www.catb.org/gpsd/installation.html
* libusb-1: http://www.libusb.org/wiki/libusb-1.0
* python-libusb1: https://github.com/vpelletier/python-libusb1
* pySerial (for gps.py): http://pyserial.sourceforge.net/
* and the libmich library: https://github.com/mitshell/libmich

### Installing

An installer script is yet to be written that will handle the prerequisites.

To start the software navigate to the zigbee_sniffer folder.


```
cd /AngryGenesis/zigbee_sniffer/
```

And run the following command;

```
sudo python sniffer.py
```

If all goes well you should see some output that looks simular to this example outpout;

```
{"timestamp": "2018-07-18T10:45:31.623456", "frame": "0200b9", "band": "7", "localname": "TI CC2531", "rx_id": "4321", "tx_id": "1234", "location": {"lat": 27.956501617, "lon": -82.437369009}, "rssi": 12, "codename": "Zigbee Survey", "type": "zigbee", "channel": 15, "deviceid": "12345"}
```

## Software Description

The software is structured as followed:

* CC2531.py is the USB *driver* for a single CC2531 dongle.

   The class CC2531 handles the main USB controls (init, set channel...) and 
   802.15.4 frames' reading methods.

* gps_thread.py is a little class to collect GPS information from GPSD and presents it to the interpreter.py file.

* receiver.py is the main handler for a CC2531 USB dongle.

   It initializes the communication with the dongle by instantiating a CC2531 
   class, and then listens to a single channel, or multiple channels
   alternatively. Channels' list is defined in `CHAN_LIST` class attribute. 
   Hopping period (for multi-channels) is defined in `CHAN_PERIOD` class
   attribute. Due to the time needed by the dongle to re-tune itself (~500ms), 
   do not expect to do quick channel hopping. When a 802.15.4 frame is read,
   metadata are added (channel number, timestamp, GPS position) and everything
   is packed and sent over a socket defined in `SOCK_ADDR` to the interpreter.

* interpreter.py is the main server which collects and interprets information coming from all CC dongles.
   
   It collects receivers' packet over the socket, and interpret them with the IEEE802154 decoder from libmich. It can also record all those textual info into a file in /tmp.

* sniffer.py is the main executable.
   
   It creates an interpreter (/ server) and drives as many CC dongles as listed 
   on USB ports of the computer.

* decoder.py is an independent little python executable file.

   You can call it to print interpreted data of a pcap file that is a capture 
   of IEEE 802.15.4 frames forwarded over UDP by receivers' instances.

## Deployment

ANGRY GENESIS has been developed on Linux, and known to not work on Windows. It seems that the winusb backend used by libusb-1 does not support certain USB controls required by the dongles.


## Built With

* [Python 3.7](https://www.python.org/getit/) - Backend survey tool.
* [Java](https://java.com/en/download/) - Packet Decryption tool.
* [ELK](https://www.elastic.co/elk-stack) - Database visualation tools.

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

* **mitshell** - *Additional work* - [mitshell](https://github.com/mitshell/CC2531)

* **The rest of us** - *Final code changes*

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
