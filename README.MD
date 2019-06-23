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
* 20190617 - Can Play all music in a list in MusicPlayerActivity.
* 20190618 - progressbar (to bind), body - RecyclerView (to bind).
* 20190619 - Progress report meeting.
* 20190620 - MusicPlayerActivity: body, music list added, adapted, effect refined.
* 20190621 - MusicPlayerActivity: seek bar synchronize(get). Quicker single cycle mode (no prepare again).
* 20190622 - Bug fix on race of NetworkResponse & MusicServiceReady.
* 20190623 - MusicPlayerActivity: seek bar synchronize(set, buffer, every time when go in MusicPlayerActivity, do synchronization with service)
* 20190624 - TODO: refine resource list with name(get), adapt title, isPlaying effect, click jump music(set), music not there exception, over-scroll refresh on music list.

## TODO list
* to Fix: MultiActivity Launched when click fast.
* music play play from beginning, if it is the same music. (do not need to prepare, use loop)
* confirm music service bind and set resource list not pending on UI thread. (e.g. initialize service binder on Launcher/Login Activity, try thread.sleep())
* layout (Top Bar, Body, Progress Bar, Control Bar, Footer)
* Backdrop design on music lists select.
* User make, change, delete other lists.
* Add music to list, remove music from list.
* Music List Local, Online, inApp design
* a UI needed when background play screen locked
* a easy float button for user to stop music.
* file multiple-select, file menu
* do not show full path, only last layer enough.
* File secondary app bar use "Backdrop" design. Ref. also to Baidu Net Disk.
* Entering an item effect. (layered expand)
* Launching Screen. Loading layer.
* Back-end: From "net.sf.json" to "Gson"
* Use "Socket" to receive music (download while listening)
* Can relate tag/note/comments on music files.
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
* 0x12 - onFileGet for FileFragment
* 0x13 - onAllMP3Get for MP3Fragment

## Implementation Logic Note
* MainActivity -> TabLayout -> ViewPager -> PagerAdaptor -> Fragment
* MusicPlayerActivity (Top Bar, Body, Progress Bar, Control Bar, Footer)
