#!/bin/sh

rm -r bin  2>  /dev/null

rm $(find . | grep '~$')       2>  /dev/null
rm $(find . | grep '.class$')  2>  /dev/null
rm $(find . | egrep '/(.|)#')  2>  /dev/null

