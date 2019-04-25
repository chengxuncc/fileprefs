# FileSharedPreferences
<a href="https://jitpack.io/#chengxuncc/fileprefs"> <img src="https://jitpack.io/v/chengxuncc/fileprefs/month.svg" /></a>
<a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/github/license/chengxuncc/fileprefs.svg"/></a>    
A file based SharedPreferences library for android, which working great on Android SDK>=24 (Android 7.0 Nougat).  

# Usage
Add JitPack repository to project build file:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Add the dependency:
```
dependencies {
    implementation 'com.github.chengxuncc:fileprefs:1.0'
}
```

Set app sharedPreferences file to be world readable:
```java
SharedPreferences sharedpreferences = getSharedPreferences(YourPreferencesFileName, MODE_PRIVATE);
FileSharedPreferences.makeWorldReadable(YourPackageName,YourPreferencesFileName);
```

Using `FileSharedPreferences` at other apps or your xposed module hook method:
```java
FileSharedPreferences sharedPreferences = new FileSharedPreferences(YourPackageName, YourPreferencesFileName);
// if you want to reload
sharedPreferences.reload();
```

# Credit
[XSharedPreferences](https://github.com/rovo89/XposedBridge/blob/art/app/src/main/java/de/robv/android/xposed/XSharedPreferences.java)
