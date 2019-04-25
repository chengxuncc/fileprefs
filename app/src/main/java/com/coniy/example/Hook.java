package com.coniy.example;

import com.coniy.fileprefs.FileSharedPreferences;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private FileSharedPreferences sharedPreferences;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        sharedPreferences.reload();
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        sharedPreferences = new FileSharedPreferences("com.coniy.example", "default");
    }
}