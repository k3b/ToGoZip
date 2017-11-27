# ![](https://raw.githubusercontent.com/k3b/ToGoZip/master/app/src/main/res/drawable-hdpi/ic_launcher.png)  ToGoZip: "Add To Zip" for android's share/send menu.

<a href="https://f-droid.org/packages/de.k3b.android.toGoZip" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100"/></a>

Easy collection of files from every supporting app into one zip-file while
beeing 'on the go' or offline.

This collecting "2go.zip"-file is like a suitcase where you can put things (files)
into from nearly everywhere (from every app that supports 'share'/'send')
and that can travel everywhere (via email, bluetooth, nfc).

 In the beginning this program was developed to collect new android calendar
 entries [ics-files](https://f-droid.org/wiki/page/de.k3b.android.calendar.ics.adapter)
 that should be transfered to the desktop-pcs-s calendar. <br/>

## Features:

* supports view/send/SendMultible for file(s), folder(s), urls and texts.
* automatic file-renaming in the zip if the same name with different file-date already exists.
* if settings/DebugMode is enabled processing is logged to logcat and to clipboard as text.
* Settings automatically pops up, if the zip folder is not writable.
* [Translations](https://crowdin.com/project/togozip) : de, en, es, fr, jp, zh-CN
  * [![Crowdin](https://d322cqt584bo4o.cloudfront.net/togozip/localized.svg)](https://crowdin.com/project/togozip) [Help us to translate into other languages.](https://crowdin.com/project/togozip)
* Minimual requirements: Works with android-2.1 (api 7) and up. apk size is only about 100 KB.

## Required Android Permissions:
* WRITE_EXTERNAL_STORAGE used to store the created/updated zip-file.
* The Adroid must have external storage (aka sdcard) where the zip file is written to.

[Changelog](https://github.com/k3b/toGoZip/wiki/History)

[Technical Features](https://github.com/k3b/toGoZip/wiki/Features)

![](https://raw.githubusercontent.com/k3b/ToGoZip/master/fastlane/metadata/android/en-US/images/1-send.png)

![](https://github.com/k3b/ToGoZip/raw/master/fastlane/metadata/android/en-US/images/2-settings.png)
