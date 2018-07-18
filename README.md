# AngryGenesis

## Install
ELK is cross platform but the instruction will be for windows.
Get the ELK installer from https://bitnami.com/stack/elk
Follow the instructions from https://docs.bitnami.com/installer/apps/elk/
Install the following plugin https://github.com/dlumbrer/kbn_network (make sure you have to correct version for ELK)

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


