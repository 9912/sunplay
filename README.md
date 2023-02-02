# SunPlay
Sunvox Player for Android, uses SunVox DLL

# Building
For compiling on Android phones, [AIDE](https://www.android-ide.com/) is used, but you can use Android Studio also if you're working on desktop.
- Get AIDE from Google Play.
- Download the repo and unzip it.
- Open AIDE and navigate to sunplay-master/app, the option "Open this project" will appear.
- On the upper toolbar pres the Play button to compile and run, make sure to give "Unknown Origin" permission before installing.
- Open the app once installed

NOTE: The free version of AIDE only compiles, to circumvent the purchase to unlock all AIDE features, I strongly recommend using [QuickEdit](https://play.google.com/store/apps/details?id=com.rhmsoft.edit).

## However...
You must create this folder in order to put the songs (*.sunvox files) onto this path:
```
(Phone Storage)/sunvoxfiles
```
to make them playable, once the files is stored there, you can listen them.

# Usage

The four basic buttons are present: Play, Stop, Previous(Prev) and Next, an additional text field is present to input the offset of the song to be played.

Below the current song playing, there is a searchable selector with the listed files for easy selection.

Also, there is a Settings section where you can change the frame rate and the buffer.
Take account that frame rate value must be 44100 or more.
Is recommended the following values for the buffer: 128, 256, 512, 1024, 2048, 4096. You can insert other value by your own risk.

# Can't run on KitKat?

Go to app/build.gradle and change targetSdkVersion to 19 instead of 30, then compile again.

# To Do:

- [x] <del>Create Proyect</del>
- [x] <del>App Icon</del>
- [x] <del>Load from folder on external storage</del>
- [x] <del>Changeable frame rate and buffer</del>
- [x] <del>Searchable Playlist Spinner</del>

# Changelog

- v1.4 Stable, SunVox updated to v2.1
- v1.3 Stable, Added an searchable spinner to select a song, SunVox updated to v2.0e
- v1.2 Beta stage, now you can change frame rate and buffer if you want to tweak (by your own responsibility)
- v1.1 Beta stage, now you can put your files onto your phone storage, packing songs into the APK is no longer needed, SunVox DLL updated to v2.0c
- v1.0 Alpha stage, packing songs on the APK is required

# Disclaimer

<del>App in beta stage</del> Now the app is stable, but some devices can get stuck or freeze with complex modules due of OpenSL implementation of the device, in this case, modify the modules with SunVox following optimization tips on [SunVox forums](https://warmplace.ru/forum/viewtopic.php?f=3&t=2379&p=7730)

Powered by [SunVox](https://warmplace.ru/soft/sunvox) (modular synth & tracker)
Copyright (c) 2008 - 2022, Alexander Zolotov <nightradio@gmail.com>, [WarmPlace.ru](https://warmplace.ru)