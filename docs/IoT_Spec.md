# IoT Spec 

 
## Introduction
This document will look at these protocols and how they relate to one another. This in order to provide the basis of a generic IoT spec. It will only concentrate on parameters that relate to the physical air interface. Available information will no doubt be different between the protocols, depending on the documentation.

Some of the main protocols used to provide an IoT air interface are listed below.
*	Bluetooth (Not covered in this document)
*	BLE (Not covered in this document)
*	ZigBee
*	Cellular (Not covered in this document)
*	Z-Wave
*	6LoWPAN
*	Thread
*	WiFi-ah (HaLow)
*	NB-IoT
*	SigFox
*	LoRaWAN
*	Weightless-N
*	Weightless-P
*	Weightless-W
*	ANT & ANT+
*	DigiMesh
*	EnOcean
*	Dash7
*	WirelessHART

Also, unless defined otherwise, no work has gone into actually conducting GRDC of the protocols, therefore it cannot be guaranteed that all of the parameters are actually transmitted on the air interface. This should be the next step after this document.
Some of the assumed common parameters so far are;
*	Transmitter ID
*	Receiver ID
*	RSSI (dBm)
*	Band
*	Channel
*	Type (Protocol)
*	Collector ID
*	Collector Name
*	Collector Session
*	User Name
*	GPS Co-ordinate values
 
## Common IoT Spec
The preference for file format is JSONL containing the following keys.

Field Name | Data Type | Description
--- | --- | ---
timestamp|String|Timestamp of the capture in the format of yyyy-MM-dd'T'HH:mm:ss.SSSZ
type|String|IoT protocol. For ease I think it should be the name in all lowercase instead of a lookup table.
tx_id|String|Transmitter ID of the remote device. The foramt of this will probably change between different protocols.
rx_id|String|Receiver ID that the remote device was talking to.
band|Integer|The RF band that the data was captured on. I guess we should have a lookup table that this references.
channel|Integer|The channel number that the data was captured on. I've not really seen anything that uses raw frequency values.
rssi|Integer|Measured signal strength in dBm.
location|dictonary|A dict containg the keys "lat" and "lon", with their values as doubles.
localname|String|Friendly display name for the capture device.  
codename|String|A tag for a capture session.
deviceid|String|Unique identifer for the capture device
networkid|String|Unique identifier for the network being monitored (e.g. wifi SSID, 802.15.4 PAN, etc.)
frame|String|Raw frame data from collection

### Time format
Example of how to get the time in the correct format.
Python
<pre>
print datetime.datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")
</pre>

### Band Table
This is just the ISM bands from https://en.wikipedia.org/wiki/ISM_band#Frequency_allocation
Index | Start | Stop
--- | --- | ---
1|6.765|6.795
2|13.553|13.567
3|26.957|27.283
4|40.66|40.7
5|433.05|434.79
6|902|928
7|2400|2500
8|5725|5875

### Example data
{"timestamp":"2018-06-06T23:59:52.511765", "type":"zigbee", "tx_id":"1234", "rx_id":"4321", "band":7, "channel": 15, "rssi": -10, "location":{"lat":50.00, "lon":50.00}, "localname": "iot sniffer", "codename": "Test Capture", "deviceid":"1234"}
 
## Zigbee
Zigbee is a mesh protocol. Each node can act as a data source and a repeater.

### Identifiers
### RF Characteristics
Data rate of 250kbps.
Designed to work on the 2.4GHz ISM band.

Typical output power for each node is 18 dBm

#### Bands
There are 27 channels used in Zigbee.
Channel Number | Frequency (MHz)
--- | ---
0 |	868
1 |	906
2 |	908
3 |	910
4 |	912
5 |	914
6 |	916
7 | 918
8 |	920
9 |	922
10 | 924  
11 | 2405
12 | 2410
13 | 2415
14 | 2420
15 | 2425
16 | 2430
17 | 2435
18 | 2440
19 | 2445
20 | 2450
21 | 2455
22 | 2460
23 | 2465
24 | 2470
25 | 2475
26 | 2480

### Derived parameters

 
## Z-Wave
Z-Wave is a mesh topology protocol.

### Identifiers
### RF Characteristics
Data rate of 100kbps.
Designed to work on the 2.4GHz ISM band.
Has a planning range of  10-100m
### Derived parameters

 
## Weightless-P
Weightless-P takes a lot of cues from cellular technologies.  
Optimise for small payload packets (<48B) at a data rate of 0.625kbps to 100kbps.
It has a similar air interface to Cellular with multiple clients or End Devices (ED) speaking to a single Base Station (BS). Multiple BS's can be joined together on to a single Base Station Network (BSN), which manages the radio resources of multiple BSs across the network.

### Identifiers
*	uuEID.  The ED is identified by the Universally Unique End Device Identifier (uuEID), which is a 128bit UUID.
*	iEID.   Once the ED has registered to the network it is given am Individual End Device Identifier (iEID). A 18bit ID. Cannot be 0.
*	gEID.   The Group End Device Identifier (gEID) is used for multicast messges to a group og EDs. A 18 bit ID. Cannot be 0.
*	BS_ID.  Base Station Identifier (BS_ID) is the identifier of the BS in the BSN. A 16bit ID. Cannot be 0.
*	BSN_ID. Base Station Network Identifier (BSN_ID).A 16bit ID. Cannot be 0. If the MSB 0, it is a public network, if 1, a private network.

### RF Characteristics
Designed to work on any ISM sub-GHz unlicensed band. The spec defines the following bands;
*	138MHz
*	433MHz
*	470MHz
*	780MHz
*	868MHz
*	915MHz
*	923MHz

Frame durations;
*	2 seconds.
*	4 seconds.
*	8 seconds.
*	16 seconds.

In all cases a timeslot is 50ms.

Has support for multiple bandwidths;
*	Single channel is 12.5 kHz.
*	4 channel is 50 kHz.
*	8 channel is 100 kHz.

Typical output power;
*	Base Station. 27 dBm.
*	End Device. 14 dBm.
*	Max BC and ED. 30 dBm.

Has a planning range of  

Min output power is prescribed per ED by the BSN. Each ED is put into the following classes;
*	A. 0dBm
*	B. 10dBm
*	C. 17dBm
*	D. 27dBm
*	E. 30dBm  

Weightless-P performs frequency hopping. The hopping sequence is defined in the System Information Block (SIB) of each frame. The hopping is only in band.

### Derived parameters
*	TRx ID is assumed to be uuEID, iEID, gEID or BS_ID.
*	BSN_ID.
*	Band.
*	Frequency.
*	Bandwidth.
*	Frame Duration.
*	Hopping channels.
*	ED Min output power.


 
## ANT
ANT is a multicast sensor network. Nodes are able to be TX only, RX only or TRx. ANT is primarily used for fitness sensors as the spec is owned by Garmin, but also in use by Fitbit, Nike adn Suunto to name a few.

Supports Point-2-Point, star, tree and mesh topologies.

### Identifiers
### RF Characteristics
Broadcast/Ack packets have a data rate of 12.8 kbps.
Burst packets have a data rate of 20 kbps.
Advanced Burst packets have a data rate of 60kbps.

Designed to work on the 2.4GHz ISM band
Has a planning range of 30m

### Derived parameters
 
## 6LoWPAN
### Identifiers
### RF Characteristics
### Derived parameters
 
## Thread
### Identifiers
### RF Characteristics
### Derived parameters

 
## WiFi-ah (HaLow)
### Identifiers
### RF Characteristics
### Derived parameters

 
## NB-IoT
### Identifiers
### RF Characteristics
### Derived parameters

 
## SigFox
### Identifiers
### RF Characteristics
### Derived parameters

 
## LoRaWAN
### Identifiers
### RF Characteristics
### Derived parameters
 
## Weightless-N
### Identifiers
### RF Characteristics
### Derived parameters
 
## Weightless-W
### Identifiers
### RF Characteristics
### Derived parameters
 
## DigiMesh
### Identifiers
### RF Characteristics
### Derived parameters
 
## EnOcean
### Identifiers
### RF Characteristics
### Derived parameters
 
## Dash7
### Identifiers
### RF Characteristics
### Derived parameters
 
## WirelessHART
### Identifiers
### RF Characteristics
### Derived parameters
