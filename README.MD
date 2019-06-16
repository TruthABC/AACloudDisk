# AACloudDisk Android Client

## Online Repository
* Android Client: https://github.com/TruthABC/AACloudDisk/
* Back-end Server: https://github.com/TruthABC/aacloud

## Progress
* 20190601 - Handler Memory Leak Fixed, MP3Fragment life cycle confirming
* 20190602 - Research on Downloading while Playing & Music Player. (Decision: 1. Play only online Music, 2. Play Online or Downloaded either, 3. Downloaded + Download while playing)
* 20190603 - Play single music done. MusicService added to Play all music (DOING).
* 20190604 - Gap. (Do with Career Things)
* 20190605 - MusicService done (to connect to activity). Back-end Mysql dependency removed.
* 20190606 - Back-end better ways of Exception throwing, MusicService connect (bind) to MainActivity.
* 20190607 - Gap. (Dragon Boat Festival 1)
* 20190608 - Gap. (Dragon Boat Festival 2)
* 20190609 - Gap. (Dragon Boat Festival 3)
* 20190610 - Gap. (Flight Canceled for Weather.)
* 20190611 - Gap. ("Jet Lag" and Recover :))
* 20190612 - Research on Material Design for Music Player. Research on Website Design for PJ.
* 20190613 - Implement UI of "Material Design" principles for some layouts.
* 20190614 - MSc webpage ver.1.0
* 20190615 - App theme change. Fixed ripple effect. Study & implement on Material Design. Implement "MusicPlayerActivity".
* 20190615 - complete: MusicPlayerActivity: layouts (Top Bar, Control Bar, Footer), intent to get in, intent to go back.
* 20190616 - Ripple color & shape fixed. Round shape icon changed. UI design case studying.
* 20190617 - TODO: To play music in MusicPlayerActivity, design body, design data structure for playing mode.

## TODO list
* todo: finish Activity separated for music playing.
* todo: layout, round icon, onclick luminance, on click change icon, on click switch flag.
* layout (Top Bar, Body, Progress Bar, Control Bar, Footer)
* todo: Music List Service
* todo: MusicService select one and play all in list.
* todo: background play when screen locked (a UI needed)
* todo: a easy float button for user to stop music.
* todo: file menu
* todo: file multiple-select
* do not show full path, only last layer enough.
* File sub app bar use Backdrop design. Ref. But also to Baidu Net Disk.
* Entering an item effect.
* Launching Screen. Loading layer.
* User make, change, delete other lists.
* Add music to list, remove music from list.
* Back-end: From "net.sf.json" to "Gson"
* Use "Socket" to receive music (download while listening)
* Can relate tag/note/comments on picture files.
* future: Find a good Job :)

## 1. Run
* Platform (Recommended): "Xiaomi Redmi 5A (Android 7.1.2, API25)"
* Resolution (Recommended): W x H = 720 x 1280 (5-Inch)
* Network: Thie App needs Internet to access its server. For example, to "login", access database and file system.
* Browser: This App needs to start implicit intent of HTTP-URI targeting browser application. For example, to "download", "preview" and "upload".
* Downloader: This App could start implicit intent of HTTP-URI targeting downloader application (or download management application).

## 2. Develop
* IDE: Android Studio
* SDK Version: compileSdkVersion 28; targetSdkVersion 28; minSdkVersion 21;
* Extra Dependency: gson:2.8.2

## 3. Message Handler
* 0x13 - "getAllMP3" for MP3Fragment

## Implementation Logic Note
* MainActivity -> TabLayout -> ViewPager -> PagerAdaptor -> Fragment
* MusicPlayerActivity (Top Bar, Body, Progress Bar, Control Bar, Footer)
