#!/bin/bash -e
for f in *;
do
  cd $f
  echo $f
  git rev-list origin/master..origin/recovery | sed -e 's/^/    /g'
  cd ..
done
  
