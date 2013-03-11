#!/bin/sh


paramBuild=0
for arg in $@; do
    if [[ "$arg" = "--build-scripts" ]]; then
	paramBuild=1
    elif [[ "$arg" = "--build-make" ]]; then
	paramBuild=2
    fi
done



if [[ $paramBuild = 1 ]]; then
    ## build scripts
    for prog in $(java -jar util-say.jar --list); do
	echo '#!/usr/bin/env bash' > ./$prog
	echo 'java -jar /usr/bin/util-say.jar '"$prog"' "$@"' >> ./$prog
	chmod 755 $prog
    done
elif [[ $paramBuild = 2 ]]; then
    ## build make file
    cp src-Makefile Makefile
    for prog in $(java -jar util-say.jar --list); do
	echo -e '\tunlink "${DESTDIR}/usr/bin/'$prog'"' >> ./Makefile
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
