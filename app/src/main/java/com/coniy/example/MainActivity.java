package com.coniy.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.coniy.fileprefs.FileSharedPreferences;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("default",MODE_PRIVATE);
        FileSharedPreferences.makeWorldReadable("com.coniy.example","default");
    }
}
