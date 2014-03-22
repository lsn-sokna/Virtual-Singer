1. First thing we have to do is download jCadencii by running command "sudo apt-get install jcadencii".
2. Download all of these files
3. After you downloaded three engines, let's work on these engines:
	- efb-gw: run command below:
		+ sudo apt-get install libfftw3-dev
		+ make

	-wavtool-pl: run command below:(make sure you're on its directory)
		+sudo apt-get install autoreconfig
		+sudo apt-get install libsndfile1-dev
		+autoreconfig -fi
		+./configure
		+make
	-xvsqExec:
		+sudo apt-get install ant
		+sudo apt-get install openjdk-7jdk
		+ant(on xvsqExec directory)

3. Now let create .wav file by using command line: java -jar xvsqexec.jar <patch of efb-gw> <patch of wavtool-pl> filename.wav composefile.xvsq <pathc of oto.ini>
4. Example: java -jar xvsqexec.jar ~/Desktop/efb-gw/ ~/Desktop/wavtool-pl/ output.wav happybirthday.xvsq ~/Desktop/11/oto.ini




