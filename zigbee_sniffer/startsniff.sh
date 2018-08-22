#!/bin/bash
# ------------------------------------------------------------------
# Script to start the python CC2531 sniffer
# Author: Dr. Dools
# ------------------------------------------------------------------

set -e

cd zigbee_sniffer
python sniffer.py -f
#python sniffer.py -f
