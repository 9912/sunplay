# SunPlay
Sunvox Player for Android, uses SunVox DLL

# Building
Built and coded with [AIDE](https://www.android-ide.com/), but you can use Android Studio.
## However...
You must put the songs (*.sunvox files) onto this path:
```
	/sunplay/app/src/main/assets/sv/
```
in order to make them playable, once the files is stored there, they will be included in the APK.

# Usage

The four basic buttons are present: Play, Stop, Previous(Prev) and Next, an additional text field is present to input the offset of the song to be played.

# To Do:

- [x] <del>Create Proyect</del>
- [x] <del>App Icon</del>
- [ ] Option to change sample rate and buffer size
- [ ] Load from folder on external storage

# Warning!

App in alpha stage, some devices can get stuck or freeze with complex modules due of OpenSL implementation of the device, SunVox DLL is robust as awesome.:+1:

Powered by [SunVox](https://warmplace.ru/soft/sunvox) (modular synth & tracker)
Copyright (c) 2008 - 2020, Alexander Zolotov <nightradio@gmail.com>, [WarmPlace.ru](https://warmplace.ru)