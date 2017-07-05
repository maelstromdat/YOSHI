#Yoshi Vis

##Quick start

This software requires python >= 3.4. The software will generate a report file in the YoshiViz/output directory.
"Yoshi Vis" is getting the output of ['Yoshi'](https://github.com/maelstromdat/YOSHI)

##Dependencies

###Unix like systems
Run pip to install the dependencies (Jinja2 and PyQt4):

	% python3 setup.py install

[PyQT4 for Win/Mac/Unix](http://www.riverbankcomputing.com/software/pyqt/download)

###Windows systems
####1: Python
Download the latest python3.x distribution here: https://www.python.org/downloads/

####2: External libraries

In order to run yoshi-viz, you need to install two dependencies: Jinja2 and PyQt.

#####Jinja2

Jinja2 is available in the pypi repository. In order to download and install it, you should first open the windows command prompt: http://www.computerhope.com/issues/chdos.htm

You should then type in this prompt:

	C:\Python34\Scripts\pip3.exe install jinja2

#####PyQt

Download the latest binary distribution here (Binary Packages) section: http://www.riverbankcomputing.com/software/pyqt/download

Take care of your python version (32 or 64 bit), if you have any doubt, just open a python interpreter, your python version should appear on the header.

##Usage
###Run __main__.py

###First click browse and choose the 'input.txt' file. 
*This is the formatted data.out file from [Yoshi](https://github.com/maelstromdat/YOSHI)*. The left part of the window is projected to be working with Yoshi (when Yoshi is runnable).
![Yoshi Vis](https://raw.githubusercontent.com/NinjaTrappeur/yoshi-viz/master/YoshiViz/Documentation/Yoshi%20Vis%201.png "Browse the 'input.txt' file")

###Type the repository name (e.g. android):
![Yoshi Vis2](https://raw.githubusercontent.com/NinjaTrappeur/yoshi-viz/master/YoshiViz/Documentation/Yoshi%20Vis%202.png "Type the community")

###Go to "YoshiViz/output" and find there detailed information
![Yoshi Vis3](https://raw.githubusercontent.com/NinjaTrappeur/yoshi-viz/master/YoshiViz/Documentation/Yoshi%20Vis%203.png "Browse for more details")

![Yoshi Vis4](https://raw.githubusercontent.com/NinjaTrappeur/yoshi-viz/master/YoshiViz/Documentation/Yoshi%20Vis%204.png)
