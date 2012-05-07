mkdir bin 2>/dev/null

javac7 -Xlint:all,-serial -cp . -s src -d bin src/se/kth/maandree/*/*.java
