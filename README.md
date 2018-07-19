# AngryGenesis

ANGRY GENESIS is an 802.15.4 Zigbee survey tool using a CC2531 usb dongle whilst geo-tagging the collection data. The software comprises of a python backend feeding a Java packet decryptor with an ELK stack displaying the data captured.

The CC2531 module works 'out the box' and does not require the firmware to be reflashed.

The Python module was modified from [mitshell](https://github.com/mitshell/CC2531)

## Getting Started

To use ANGRY GENESIS ensure that you have the following hardware;

* Texus Instrumemts CC2531 Zigbee USB sniffer - http://www.ti.com/tool/CC2531EMK
* G-Star IV USB GPS adaptor - https://wholesaler.alibaba.com/product-detail/Wholesale-GlobalSat-BU-353S4-Cable-USB_60105188624.html

### Prerequisites

ANGRY GENESIS requires the following packages to be installed;

* GPSD: http://www.catb.org/gpsd/installation.html
* python-libusb1: https://github.com/vpelletier/python-libusb1
* and the libmich library: https://github.com/mitshell/libmich

### Installing

ANGRY GENESIS has been developed for Linux OS and as such can be run within Ubuntu, Raspberry Pi etc
An installer can be found within the root directory of the project, shockingly called `install.sh`. 


### Start

Angry Genesis is installed as a Systemd service which can be started with;
<pre>
sudo service angrygenesis start
</pre>
Output can be checked/monitored with;
<pre>
service angrygenesis status
journelctl -fu angrygenesis
</pre>
And finally to stop;
<pre>
sudo service angrygenesis stop
</pre> 

The python zigbee sniffer can also run stand alone that dumps the raw frames within the JSON object
To start the software navigate to the zigbee_sniffer folder.

<pre>
cd /opt/AngryGenesis/zigbee_sniffer/
</pre>

And run the following command;

<pre>
sudo python sniffer.py
</pre>

If all goes well you should see some output that looks simular to this example output;

```
{"timestamp": "2018-07-18T10:45:31.623456", "frame": "0200b9", "band": "7", "localname": "TI CC2531", "rx_id": "4321", "tx_id": "1234", "location": {"lat": 1.222, "lon": 2.3333}, "rssi": 12, "codename": "Zigbee Survey", "type": "zigbee", "channel": 15, "deviceid": "12345"}
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



# Analysis Setup
## Install
ELK is cross platform but the instruction will be for windows.
* Get the ELK installer from https://bitnami.com/stack/elk
* Follow the instructions from https://docs.bitnami.com/installer/apps/elk/
* Install the following plugin https://github.com/dlumbrer/kbn_network (make sure you have to correct version for ELK)
Other recommended plugins, that can help  with analysis
* https://github.com/prelert/kibana-swimlane-vis
* https://github.com/elo7/cohort
* https://github.com/sirensolutions/kibi_radar_vis


Restart everything using the manager window (be patient it takes a while to restart)

## Setup
* Log into Kibana.
* Go to Dev Tools on the left hand side.
* Copy the contents of docs/elkmapping.txt into the left hand console.
* Hover over the section and press the green arrow that appears.
* Import some data as per the note section below.
* After importing data go to the Management section on the left hand side.
* Click Index Patterns.
* Click Create Index Pattern.
* Type iot into the index-name-* box and click next.
* Select timestamp from the drop down menu and click next.

## Visualise
* Click Visualise and Click New.
* Click Coordinate Map.
* Select iot from the left hand column.
* Click Geo Coordinate unser Buckets.
* Select GeoHash in the new drop down menu that appears.
* Select location in the other new drop down menu.
* Click the play button to view the data on a map.
* Click Save on the top of the webpage to do just that, for ease later on.

## Notes
We have to wrap the IoT JSONL format into another dictionary for ELK to understand it.   
The command to upload a file using cURL to ELK is below. This was done uisng MobaXterm on Windows.

<pre>
FILENAME="test.jsonl"
cat $FILENAME | sed -e 's/^/{"index":{}}\n/' >$FILENAME.elk
curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/iot/doc/_bulk?pretty' --data-binary @$FILENAME.elk >/dev/null
</pre>


## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)
* **mitshell** - *Additional work* - [mitshell](https://github.com/mitshell/CC2531)
* **The rest of us** - *Final code changes*

## License

This project is licensed under the CeCILL-B License - see the [license.txt](license.txt) file for details.
This was the the license of the mitshell CC2531 project.



