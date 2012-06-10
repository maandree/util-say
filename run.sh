#!/bin/sh


if [[ $1 = "--completion--" ]]; then
    _run()
    {
	local cur prev words cword
	_init_completion -n = 2>/dev/null # || return
	
	COMPREPLY=( $( compgen -W `java -ea -cp bin se.kth.maandree.utilsay.Program --list` -- "$cur" ) )
    }
    complete -o default -F _run run
else
    java -ea -cp bin se.kth.maandree.utilsay.Program "$@"
fi
