# AngryGenesis


We have to wrap the IoT JSONL format into another dictionary for ELK to understand it.   
The command to upload a file to ELK is

<pre>
FILENAME="test.jsonl"

cat $FILENAME | sed -e 's/^/{"index":{}}\n/' >$FILENAME.elk

curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/iot/doc/_bulk?pretty' --data-binary @$FILENAME.elk >/dev/null
</pre>


