#! /bin/sh
# $Id: build.sh 14757 2008-05-17 05:40:36Z dthompson $
#

# A skeleton script to call the real build script at
# ./argouml-build/build.sh

cd argouml-build
./build.sh $*
cd ..
