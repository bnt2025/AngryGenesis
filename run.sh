#!/bin/bash
# ------------------------------------------------------------------
# Script to run the Angry Genesis Java application.
# Author: Dr. Dools, bnt2025
# ------------------------------------------------------------------

LOG_FILE_DIR="./logdata"

#echo off
set -e

java -jar AngryGenesis/dist/AngryGenesis.jar $LOG_FILE_DIR
