#!/bin/sh

cd /home/ec2-user/cnv-project
java -XX:-UseSplitVerifier pt/ulisboa/tecnico/cnv/server/WebServer
