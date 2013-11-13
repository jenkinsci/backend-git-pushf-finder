#!/bin/bash -x
for f in *
do
  echo $f
  pushd $f
    git push git@github.com:jenkinsci/$f recovery:master
  popd
done
