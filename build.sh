#!/bin/sh

paramBuildScripts=0
for arg in $@; do
    if [[ "$arg" = "--build-scripts" ]]; then
	paramBuildScripts=1
    fi
done



if [[ $paramBuildScripts = 1 ]]; then
    ## build scripts
    for prog in $(java -jar util-say.jar --list); do
	echo "java -jar /usr/bin/util-say.jar $prog \$@" > ./$prog
	chmod 755 $prog
    done
else
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
fi
