#!/usr/bin/env bash

find . -name "*~" -exec rm {} \;
for d in . onejar ; do
   (cd $d ; rm -fr target project/target project/boot project/project nohup.out .settings  .cache .classpath .project)
done
