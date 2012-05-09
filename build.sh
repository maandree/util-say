mkdir bin 2>/dev/null

javac -Xlint:all,-serial -cp . -s src -d bin src/se/kth/maandree/*/*.java
