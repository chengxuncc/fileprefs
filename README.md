# FileSharedPreferences
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