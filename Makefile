JAR=jar
JAVAC=javac

SRC=$(shell find src/ | grep '\.java')
OBJ=$(shell find src/ | grep '\.java' | sed -e 's_^src/__g' -e 's_\.java$$_\.class_g')


all: util-say.jar

$(OBJ): $(SRC)
	@"$(JAVAC)" -O -cp src -s src -d . $^

util-say.jar: META-INF/MANIFEST.MF $(OBJ)
	@"$(JAR)" -cfm "$@" "META-INF/MANIFEST.MF" $(OBJ)


install:
	install -d -m 755 "$(DESTDIR)/usr/bin"
	install -m 755 util-say.jar "$(DESTDIR)/usr/bin"
	install -m 755 ponytool "$(DESTDIR)/usr/bin"
	install -d -m 755 "$(DESTDIR)/usr/share/licenses/util-say"
	install -m 644 LICENSE COPYING "$(DESTDIR)/usr/share/licenses/util-say"


uninstall:
	unlink "$(DESTDIR)/usr/bin/util-say.jar"
	unlink "$(DESTDIR)/usr/bin/ponytool"
	yes | rm -r "$(DESTDIR)/usr/share/licenses/util-say"


.PHONY: clean
clean:
	yes | rm -r se || true
	rm util-say.jar || true
	rm *.{info,pdf,ps,dvi}{,.*} || true
	rm *.{aux,cp,cps,fn,ky,log,op,ops,pg,toc,tp,vr} || true

