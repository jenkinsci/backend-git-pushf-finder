#!/bin/bash -ex
cat events.txt | grep FOUND | cut -b11- | cut -d, -f1 | uniq
