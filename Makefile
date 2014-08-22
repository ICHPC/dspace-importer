all:
	javac -d class src/*.java
	jar -cvfm jar/METSWriter.jar src/META-INF/MANIFEST.MF -C class .

clean:
	rm -f class/*.class jar/METSWriter.jar
