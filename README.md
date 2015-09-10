# Introduction
> This project have won the Up-tech CUP this year(2015.7.28).
> Here I just introduce this project for you.
> Look at the whole view of it:

![Alt text](http://img.blog.csdn.net/20150910162048430)

# Description

As you can see above, this Monitor contain **Eight** part:
From top to bottom and left to right:

> + UART interface
> + LCD & Camera Control Pane
> + Coordinator Control Pane
> + Mongo DB Control pane
> + System Message Log Pane
> + Network Topology Pane
> + Picture Pane
> + Vibrate Pane

so this project is consistent with another ZigBee Stack Project, know more about the low level hardware, you should look at the ZigBee Project.

## Details

this Monitor is developed with the Java Swing, so you can run this program free on Windows, Unix, Mac.

	java -jar wirelessLCDSystem.jar
	
	# before you run this program make sure you have download this project
	# and make sure you find the jar package in the /dist directory.
	# you have to install java run time environment.
	# make sure you have install the mongo DB database.

Here I will tell you the special function of this project.

### About the LCD
	
>> the LCD pane is working compatible with the Coordinator control pane, so if you want to control the remote LCD, you should first connect the monitor with your local coordinator, but how?

> 1. you should use an USB2UART wire to connect your PC with the coordinator, if you don't know about the coordinator, you should look up the ZigBee Docs.
> 2. open your Coordinator, and start you monitor.
> 3. open your remote LCD device, this device is what you want to control, this device is also special ones.

>> you may wondered how I control several LCDs, every end device get an external IEEE 64 address along with the short 16bit network address. so just give a record of the 64bit address of the device your new LCD wired.

>![Alt text](http://img.blog.csdn.net/20150910163958839)

>> for how to add a new map record you may need to install the mongoDB, in your local dbs, there is a address map named db.

>![Alt text](http://img.blog.csdn.net/20150910164317439)

>> if you want to add a new address just add a new entry in the roomMap collection in this db.

>![Alt text](http://img.blog.csdn.net/20150910164551842)

### About the timer screen task
> You may want to add a timer task to the remote LCD. For example you want to show the content after two hours, just use the timer task pane.

> ![Alt text](http://img.blog.csdn.net/20150910164957746)

> You can explore this in the local db. there is also a collection in the address map db.

### Camera and ADXL345
> serial camera and ADXL345 is another two type of sensors like LCD, so if your LCD works well and you get the device I specify, you can just open the end device wired the camera or ADXL345.

# Preview

![Alt text](http://img.blog.csdn.net/20150910165656669)

# Relation

> see source code [here](https://github.com/smileboywtu/Embedded-Monitor).

> [compatible project](https://github.com/smileboywtu/Wireless-LCD-Stack).

# Contact
 1. [GitHub](https://github.com/smileboywtu).
 2. Email: 294101042@qq.com


