PREFIX=/usr
BIN=/bin
DATA=/share

JAR=jar
JAVAC=javac

SRC=$(shell find src/ | grep '\.java')
OBJ=$(shell find src/ | grep '\.java' | sed -e 's_/[a-zA-Z]*\.java$$_/\*\.class_g' -e 's_^src/__g' | sort | uniq)

all: util-say.jar info

.PHONY: util-say.jar
util-say.jar:
	"$(JAVAC)" -O -cp src -s src -d . $(SRC)
	"$(JAR)" -cfm "$@" "META-INF/MANIFEST.MF" $(OBJ)


info: util-say.info.gz
%.info: %.texinfo
	$(MAKEINFO) "$<"
%.info.gz: %.info
	gzip -9c < "$<" > "$@"


pdf: util-say.pdf
%.pdf: %.texinfo
	texi2pdf "$<"

pdf.gz: util-say.pdf.gz
%.pdf.gz: %.pdf
	gzip -9c < "$<" > "$@"

pdf.xz: util-say.pdf.xz
%.pdf.xz: %.pdf
	xz -e9 < "$<" > "$@"


dvi: util-say.dvi
%.dvi: %.texinfo
	$(TEXI2DVI) "$<"

dvi.gz: util-say.dvi.gz
%.dvi.gz: %.dvi
	gzip -9c < "$<" > "$@"

dvi.xz: util-say.dvi.xz
%.dvi.xz: %.dvi
	xz -e9 < "$<" > "$@"


install: util-say.jar util-say.info.gz
	install -d -m 755 "$(DESTDIR)$(PREFIX)$(BIN)"
	install -m 755 util-say.jar "$(DESTDIR)$(PREFIX)$(BIN)"
	install -m 755 ponytool "$(DESTDIR)$(PREFIX)$(BIN)"
	install -d -m 755 "$(DESTDIR)$(PREFIX)$(DATA)/licenses/util-say"
	install -m 644 LICENSE COPYING "$(DESTDIR)$(PREFIX)$(DATA)/licenses/util-say"
	install -d -m 755 "$(DESTDIR)$(PREFIX)$(DATA)/info"
	install -m 644 util-say.info.gz "$(DESTDIR)$(PREFIX)$(DATA)/info"


uninstall:
	unlink "$(DESTDIR)$(PREFIX)$(BIN)/util-say.jar"
	unlink "$(DESTDIR)$(PREFIX)$(BIN)/ponytool"
	unlink "$(DESTDIR)$(PREFIX)$(DATA)/info/util-say.info.gz"
	yes | rm -r "$(DESTDIR)$(PREFIX)$(DATA)/licenses/util-say"


.PHONY: clean
clean:
	yes | rm -r se || true
	rm util-say.jar || true
	rm *.{info,pdf,ps,dvi}{,.*} || true
	rm *.{aux,cp,cps,fn,ky,log,op,ops,pg,toc,tp,vr} || true

