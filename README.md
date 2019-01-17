# Le2LeJosEv3-Ev3Meg
This is the LEGO® Mindstorms EV3 **Ev3Meg Robot** example program in the Java programming language that uses a Java Implementation of _LEGO® Mindstorms EV3 Programming Blocks (icons)_ on LeJOS EV3.

_Ev3Meg_ is a small helper robot that can drive and follow a black line on a light surface. It uses a smart fuzzy logic to drive on the right edge of the line. 

You can find the building instructions and the LEGO® icon-based program of the _Ev3Meg Robot_ in the LEGO® icon based (LabView-based) Programming Environment (Home Edition). Ev3Meg is one of the More Robots examples.
You can download the LEGO® Programming Environment at https://www.lego.com/en-us/mindstorms/downloads/download-software (or one of the other language pages).

## Dependencies
This project depends on the **Le2LeJosEv3** Library (see https://github.com/robl0377/Le2LeJosEv3) that sits on top of the current version of the LeJOS EV3 framework. 
Please add the JAR file of the _Le2LeJosEv3_ Library _(le2lejosev3.jar)_ into this project's classpath before running it. The LeJOS Eclipse plugin will take care of the transfer of the library and the program JAR files to the EV3 brick.

In this project I am using the **LeJOS EV3 v0.9.1beta** framework (see https://sourceforge.net/projects/ev3.lejos.p/) and a standard LEGO® Mindstorms EV3 Brick.

## Resources
The program uses several sound files that are in the project's _resources_ directory. 
Please do one of the following:
1. Run the ANT script _build_res.xml_ to pack the files in the _resources_ directory into the archive _ev3megres.jar_. Then add this archive to your Eclipse project's classpath. The LeJOS Eclipse plugin will take care of the transfer of the archive to the EV3 Brick before running the program.
2. Upload (via SCP) the files in the _resources_ directory to your EV3 Brick to the directory _/home/lejos/lib_.

The **sound files** are converted from those MP3 sound files found at http://www.thumb.com.tw/images/blog/lego_ev3_sound_files/ev3_sounds.zip.
There are several ways to convert the MP3 sound files to WAV. 
I was using the online converter at https://audio.online-convert.com/convert-to-wav. 
Be sure to set _Change audio channels:_ to _mono_.

---
LEGO® is a trademark of the LEGO Group of companies which does not sponsor, authorize or endorse this site.
