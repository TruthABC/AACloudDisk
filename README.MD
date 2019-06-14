# AACloudDisk Android Client

## Online Repository
* Android Client: https://github.com/TruthABC/AACloudDisk/
* Back-end Server: https://github.com/TruthABC/aacloud

## Progress
* 20180601 - Handler Memory Leak Fixed, MP3Fragment life cycle confirming
* 20180602 - Research on Downloading while Playing & Music Player. (Decision: 1. Play only online Music, 2. Play Online or Downloaded either, 3. Downloaded + Download while playing)
* 20180603 - Play single music done. MusicService added to Play all music (DOING).
* 20180604 - Gap. (Do with Career Things)
* 20180605 - MusicService done (to connect to activity). Back-end Mysql dependency removed.
* 20180606 - Back-end better ways of Exception throwing, MusicService connect (bind) to MainActivity.
* 20180607 - Gap. (Dragon Boat Festival 1)
* 20180608 - Gap. (Dragon Boat Festival 2)
* 20180609 - Gap. (Dragon Boat Festival 3)
* 20180610 - Gap. (Flight Canceled for Weather.)
* 20180611 - Gap. ("Jet Lag" and Recover :))
* 20180612 - Research on Material Design for Music Player. Research on Website Design for PJ.
* 20180613 - Implement UI of "Material Design" principles for some layouts.
* 20180614 - MSc web page

## 1. Run
* Platform (Recommended): "Xiaomi Redmi 5A (Android 7.1.2, API25)"
* Resolution (Recommended): W x H = 720 x 1280 (5-Inch)
* Network: Thie App needs Internet to access its server. For example, to "login", access database and file system.
* Browser: This App needs to start implicit intent of HTTP-URI targeting browser application. For example, to "download", "preview" and "upload".
* Downloader: This App could start implicit intent of HTTP-URI targeting downloader application (or download management application).

## 2. Develop
* IDE: Android Studio
* SDK Version: compileSdkVersion 28; targetSdkVersion 28; minSdkVersion 19;
* Extra Dependency: gson:2.6.2

## 3. Message Handler
* 0x13 - "getAllMP3" for MP3Fragment

## Implementation Logic Note
* MainActivity -> TabLayout -> ViewPager -> PagerAdaptor -> Fragment

## TODO list
* todo: PJ Website by 20190616. (2 days left)
* todo: Theme and UI change
* todo: Activity separated for music playing.
* todo: MusicService select one and play all in list.
* todo: file menu
* todo: file multiple-select
* User make, change, delete other lists.
* Add music to list, remove music from list.
* Back-end: From "net.sf.json" to "Gson"
* Use "Socket" to receive music (download while listening)
* Can relate tag/note/comments on picture files.
* future: Find a good Job :)
