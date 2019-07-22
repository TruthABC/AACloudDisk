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
* 20190624 - MusicPlayerActivity: refine resource list with name(get), adapt title, click jump music(set), bug fix on single cycle mode,
* 20190625 - MusicPlayerActivity: Refactor; MusicService: in Random mode, when click Prev, go back last played track. ("isPlaying effect" failed)
* 20190626 - Week 2 progress report.
* 20190627 - refactor RecyclerView as ListView, improve all ListView resource usage and fluency.
* 20190628 - bug on folder/file image fixed, improve isPlaying effect, new progress color, divider.
* 20190629 - Holiday.
* 20190630 - Holiday.
* 20190701 - Holiday.
* 20190702 - Holiday Lag, and continued next few days :D
* 20190703 - Week 3 progress report. Project Report Writing Course.
* 20190704 - Project Presentation Course.
* 20190705 - Big Progress: Caching while Listening Available.
    Todo: 3、添加“主动缓存”，下载后歌名附加图标(isCached=true存入本地数据库)（主动缓存前即使被动缓存完毕也不自动触发，但可以依赖于被动缓存，测试“未播放，不触发被动，仅主动缓存”后的play），主动缓存后存入与被动缓存不同本地文件夹(Next: 为其专门开设Local List)。
          4、只能删除已经“主动缓存”过、isCached=true、有小图标的音乐，删除后被动缓存依然存在，可以[清空“被动缓存”]。
          5、添加离线模式，在线模式：按照ALLMusic返回列表，结合SQL标注缓存过的文件。离线模式：Scan主动缓存文件夹(读取本地SQL)。
* 20190706 - Clear Cache Available. Revise MP3 Item Icon, OnclickEvent. Revise Tab Stricture. Enhance UI Thread Efficiency (more specific threads).
* 20190707 - File Fragment Ripple effect. JumpTo Same Music Logic Bug fixed. File RootItem Onclick. New Icons.
* 20190708 - "Backdrop Design -> Bottom Sheet" Finished. "MusicListService -> Serializable -> Gson". LoginActivity OfflineMode & Refactoring.
* 20190709 - Project presentation workshop.
* 20190710 - Weekly report 4, project report writing workshop.
* 20190711 - Project presentation workshop.
* 20190712 - Project presentation workshop.
* 20190713 - Player Bottom Sheet completed (list add/remove/rename). Refactoring (Main, Files, Music, MusicPlayer, icons).
* 20190713 - Music Tab Bottom Sheet completed (music add to list). Following MusicPlayer MainActivityMusicTab adapted to new feature.
* 20190714 - Make poster for this Project.
* 20190715 - Make poster for this Project, project report writing workshop.
* 20190716 - ResourceList double delete bug fixed. MSc Project web page update.
* 20190717 - Week 5 progress report.
* 20190718 - (Gap) Send material of traveling.
* 20190719 - (Gap) Worrying.
* 20190720 - (Gap) Rent house.
* 20190721 - (Gap) Rent house.
* 20190722 - File Tab Refactor; MP3 delete from list view bug-fix, MP3 add to list duplication auto-detect.
* 20190722 - Me Tab Refactor.
* 201907xx - TODO: upload menu -> 1.upload (give a file) 2. cloud download (give a url) 3. shared file (give a sharing code)
* 201907xx - TODO: upload menu -> 2. cloud download (pop-up1: please give a url end with .mp3, pop-up2: started or url don't end with mp3)
* 201907xx - TODO: 3.1. file menu: share this file. TODO: 3.2. manage a json in backend(code = ID + RandomChar, timestamp, hash, sharelimit, timelimit), 3.3. enter share code to get file
* 201907xx - TODO: 3+. show all shared files, delete shared files. (re-share, use the same code, delete file -> delete share, share cannot than 5 single files)
* 201907xx - TODO: Cache status. Apply music caching function: 1. Use An ImageView showing whether cached 2.Click and then popup to caching the music (or delete cache) 3.Finished, change ImageView

## Future Work
* (urgent) set "flag" in case of "multiple network cost" in MusicTab
* (urgent) MusicService store MusicList index
* (medium) worry: if no any music (a new account), will it be problems with music list (all music online)
* (medium) 1GB cache!!!!
* (medium) "FLAC" support
* (medium) refactor MusicTab & MainActivity.
* (medium) re-new project website
* (medium) crash (player activity UI e.g. progress not follow) when enter offline, then back, many clicks among folders, go player.
* (medium) MainActivity::initAllService rename, LoginActivity Refactor(handler, id password field), MainActivity Async part Refactor With Flag
* (medium) handler activity leak fix.
* (medium) music running in backend (when no screen off)
* (minor) name scrolling.
* (minor) Default list of Fav.
* (minor) rename of "Add to Music List"
* (minor) File secondary app bar use "Backdrop" design. Ref. also to Baidu Net Disk.
* (minor) refresh on music list (e.g. Music Tab over-scroll).
* (minor) show menu when click on MusicTabItem
* (minor) fileTab refresh button
* (minor) customized cache limit - 512mb.
* (minor) show add which music.
* (minor) remove icon's visibility
* (minor) music not there exception.
* (minor) file multiple-select, file menu
* (minor) "java import" in FilesFragment.
* (minor) Improve Passive Cache Name Mapping or Hashing.
* (minor) change UI when no files or get files failed.
* (minor) to Fix: Multi-Activity Launched when click fast.
* (minor-minor) response.getErrmsg() with "-" but ":" Connected
* (minor-minor) App bar height uniform.
* (minor-minor) status bar color change.
* (minor-minor) Loading layer.
* (minor-minor) can reset "all music" scan/ignore range and expanded name.
* (minor-minor) GIF DISK BODY.
* (minor-minor) a UI needed when background play screen locked
* (minor-minor) a easy float button for user to stop music.
* (minor-minor) do not show full path, only last layer enough.
* (minor-minor) Entering an item effect. (layered expand)
* (minor-minor) Launching Screen.
* (minor-minor) Back-end: From "net.sf.json" to "Gson"
* (minor-minor) Can relate tag/note/comments on music files.
* (done) fixed bug when delete a music in list (double delete bug).
* (done) "duplication test" before add music to list.

## 1. Run
* Platform (Recommended): "Xiaomi Redmi 5A (Android 7.1.2, API25)"
* Resolution (Recommended): W x H = 720 x 1280 (5-Inch)
* Network: Thie App needs Internet to access its server. For example, to "login", access file system.
* Browser: This App needs to start implicit intent of HTTP-URI targeting browser application. For example, to "download", "preview" and "upload".
* Downloader: This App could start implicit intent of HTTP-URI targeting downloader application (or download management application).

## 2. Develop
* IDE: Android Studio
* SDK Version: compileSdkVersion 28; targetSdkVersion 28; minSdkVersion 21;
* Extra Dependency: 'com.google.code.gson:gson:2.8.2', 'com.danikula:videocache:2.7.1'

## 3. Message Handler
* 0x11 - onLogin
* 0x12 - onFileGet for MainActivity/FileFragment/
* 0x13 - onAllMP3Get for MP3Fragment
* 0x16 - onDeleteFile for FileInfoListAdaptor
* 0x17 - onRenameFile for FileInfoListAdaptor
* 0x18 - onCreatedFolder for FileFragment
* 0x22 - onChangePassword

## Music Passive Cache
* Basic: When listen to music, download it from the "playing stream" with no extra network cost.
* Clear Cache: Can Clear Music Passive Cache.
* Over-drag: When over-drag, wait for downloading all data in the middle, and continue.
* Network-cut: Available until timestamp when net is cut, when restart, former cached part also available.
* Offline Jump To Un-cached Song: Stop play without hint.

## Implementation Logic Note
* MainActivity -> TabLayout -> ViewPager -> PagerAdaptor -> Fragment (-> ArrayAdaptor)
* MusicPlayerActivity (REAL BODY: Top Bar, Body, Progress Bar), Following Backdrop Design's "HIDDEN BODY".
* MusicPlayerActivity (HIDDEN BODY: Peek Height (Control Bar, Footer), Bottom Top Bar, Bottom Body)
* MusicService (HttpCacheProxy + ResourceList), Body, when change music, to find visible list item to make it highlight, in adaptor, also give a highlight logic.
