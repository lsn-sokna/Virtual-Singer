//Virtual Signer
1. First thing we have to do is download jCadencii by running command "sudo apt-get install jcadencii".

2. After that, go to http://sourceforge.net/p/purplesparrow/wiki/RelatedSoftware/ and download three engines as detailed in that link.
*Note: 
+Downloading wavtool-pl, you should do: "git clone http://scm.sourceforge.jp/gitroot/wavtool-pl/wavtool-pl.git" instead of direct download.
+Downloading xvsqExec: "sudo apt-get install bzr" and then "bzr branch lp:~paulliu/+junk/xvsqExec"
 
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





java -jar xvsqexec.jar ~/Desktop/efb-gw-pl/test1 ~/Desktop/wavtool-pl/src/wavtool-pl output1.wav 12345_abcde.xvsq ~/Desktop/11/oto.ini
