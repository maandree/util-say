all: utilsay

utilsay:
	javac -O -cp . -s src -d . src/se/kth/maandree/utilsay/*.java
	jar -cfm util-say.jar META-INF/MANIFEST.MF se/kth/maandree/utilsay/*.class
	rm -r se
	./build.sh --build-scripts


install:
	install -d "$(DESTDIR)/usr/bin"
	install -m 755 util-say{,.jar} "$(DESTDIR)/usr/bin"
	install -d "$(DESTDIR)/usr/share/bash-completion/completions"
	install -m 644 share/bash-completion/completions/util-say "$(DESTDIR)/usr/share/bash-completion/completions/util-say"
	install -m 755 $$(java -jar util-say.jar --list) "$(DESTDIR)/usr/bin"


uninstall:
	unlink "$(DESTDIR)/usr/bin/util-say"
	unlink "$(DESTDIR)/usr/bin/util-say.jar"
	unlink "$(DESTDIR)/usr/share/bash-completion/completions/util-say"
	unlink "$(DESTDIR)/usr/bin/img2ponysay"
	unlink "$(DESTDIR)/usr/bin/ponysay2img"
	unlink "$(DESTDIR)/usr/bin/ponysay2ttyponysay"
	unlink "$(DESTDIR)/usr/bin/tty2colourfultty"
	unlink "$(DESTDIR)/usr/bin/imgsrcrecover"
	unlink "$(DESTDIR)/usr/bin/unzebra"
	unlink "$(DESTDIR)/usr/bin/cowsay2unisay"
	unlink "$(DESTDIR)/usr/bin/Truncater"
	unlink "$(DESTDIR)/usr/bin/Wordwrap"
