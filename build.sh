#!/bin/sh

## completion
. run.sh --completion--

## create directory for Java binaries
mkdir bin 2>/dev/null

## warnings
warns="-Xlint:all,-serial"

## standard parameters
params="-s src -d bin"

## compile util-say
javac $warn -cp . $params $(find src | grep '.java$')  2>&1
