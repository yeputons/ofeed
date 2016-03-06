# ofeed
O'Feed is an offline VK feed viewer for Android

# Building
## Overview
I personally don't like Gradle because it's way too slow on my laptop.
That's why the project is created in "legacy" way using Ant.
On the other hand, VK SDK that I use is built using Gradle.
There is no direct way to run Gradle from Ant builds, so straightforward command-line building is not possible yet.
It all compiles flawlessly with IntelliJ IDEA, though.

# Building with IntelliJ IDEA
Tested with IntelliJ IDEA 15 Community Edition.

1. Clone project with submodules: `git clone https://github.com/yeputons/ofeed.git --recursive`
2. Click "Import project" in IDEA
3. Select path you've cloned the repository into (like `~/ofeed`)
4. Choose "Create project from existing sources" option (i.e. not "Import from Gradle/Maven/whatever")
5. On the page with source directories list, leave `ofeed (Android)`, `ofeed/src (Java)` and `vk-android-sdk/vksdk_library/src/main/java (Java)` selected. Unselect `VKTestApplication`.
6. There will be no libraries found, it's ok for now
7. There should be two modules found: `ofeed` and `main` (for `vksdk_library`). You can renamed the latter to `vksdk_library` for convenience.
8. Choose Android API >=21 as project SDK. The application will run on earlier versions as well, but API 21 is required for VK SDK to compile.
9. There is no need to change anything on the last page which offers you to uncheck mistakengly found manifests (it finds three of them for me).
10. After the project is created, go to "Project Structure"/"Libraries".
11. Add Android Support Library v4 to both modules, can be added with jar: `"$ANDROID_HOME/extras/android/support/v4/android-support-v4.jar`
12. Add two Maven dependencies to `ofeed` module: `com.j256.ormlite:ormlite-android:4.48`, `org.jsoup:jsoup:1.8.3`
13. Close the dialog and try building the project
