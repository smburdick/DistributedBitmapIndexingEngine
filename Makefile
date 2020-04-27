sys:
	javac src/*.java -d out/

master: sys
	cd out && java MasterNode 1

slave: sys
	cd out && java SlaveNode 0

clean:
	rm -rf out/
