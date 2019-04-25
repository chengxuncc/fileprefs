/*
 * Copy from package de.robv.android.xposed.XSharedPreferences
 */
package com.coniy.fileprefs;

import android.content.SharedPreferences;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileSharedPreferences implements SharedPreferences {
    private static final String TAG = "FileSharedPreferences";
    private final File mFile;
    private final String mFilename;
    private Map<String, Object> mMap;
    private boolean mLoaded = false;
    private long mLastModified;
    private long mFileSize;

    public FileSharedPreferences(File prefFile) {
        mFile = prefFile;
        mFilename = mFile.getAbsolutePath();
        startLoadFromDisk();
    }

    public FileSharedPreferences(String packageName, String prefFileName) {
        mFile = new File("/data/data/"+packageName,  "shared_prefs/" + prefFileName + ".xml");
        mFilename = mFile.getAbsolutePath();
        startLoadFromDisk();
    }

    public static void makeWorldReadable(String packageName,String prefFileName) {
        File dataDir = new File("/data/data/"+packageName);
        File prefsDir = new File(dataDir, "shared_prefs");
        File prefsFile = new File(prefsDir, prefFileName + ".xml");
        if (prefsFile.exists()) {
            for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                file.setReadable(true, false);
                file.setExecutable(true, false);
            }
        }
    }

    /** @hide */
    @Override
    public Map<String, ?> getAll() {
        synchronized (this) {
            awaitLoadedLocked();
            return new HashMap<String, Object>(mMap);
        }
    }

    /** @hide */
    @Override
    public String getString(String key, String defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            String v = (String)mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    /** @hide */
    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key, Set<String> defValues) {
        synchronized (this) {
            awaitLoadedLocked();
            Set<String> v = (Set<String>) mMap.get(key);
            return v != null ? v : defValues;
        }
    }

    /** @hide */
    @Override
    public int getInt(String key, int defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Integer v = (Integer)mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    /** @hide */
    @Override
    public long getLong(String key, long defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Long v = (Long)mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    /** @hide */
    @Override
    public float getFloat(String key, float defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Float v = (Float)mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    /** @hide */
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Boolean v = (Boolean)mMap.get(key);
            return v != null ? v : defValue;
        }
    }

    /** @hide */
    @Override
    public boolean contains(String key) {
        synchronized (this) {
            awaitLoadedLocked();
            return mMap.containsKey(key);
        }
    }
    @Override
    public Editor edit() {
        throw new UnsupportedOperationException("read-only implementation");
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        throw new UnsupportedOperationException("listeners are not supported in this implementation");
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        throw new UnsupportedOperationException("listeners are not supported in this implementation");
    }

    public synchronized void reload() {
        if (hasFileChanged())
            startLoadFromDisk();
    }

    private synchronized boolean hasFileChanged() {
        try {
            FileResult result = statFile(mFilename);
            return mLastModified != result.mtime || mFileSize != result.size;
        } catch (FileNotFoundException ignored) {
            // SharedPreferences doesn't log anything in case the file doesn't exist
            return true;
        } catch (IOException e) {
            Log.w(TAG, "hasFileChanged", e);
            return true;
        }
    }

    private void awaitLoadedLocked() {
        while (!mLoaded) {
            try {
                wait();
            } catch (InterruptedException unused) {
            }
        }
    }


    private void startLoadFromDisk() {
        synchronized (this) {
            mLoaded = false;
        }
        new Thread("FileSharedPreferences-load") {
            @Override
            public void run() {
                synchronized (FileSharedPreferences.this) {
                    loadFromDiskLocked();
                }
            }
        }.start();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void loadFromDiskLocked() {
        if (mLoaded) {
            return;
        }

        Map map = null;
        FileResult result = null;
        try {
            result = getFileInputStream(mFilename, mFileSize, mLastModified);
            if (result.stream != null) {
                map = XmlUtils.readMapXml(result.stream);
                result.stream.close();
            } else {
                // The file is unchanged, keep the current values
                map = mMap;
            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "getSharedPreferences", e);
        } catch (FileNotFoundException ignored) {
            // SharedPreferencesImpl has a canRead() check, so it doesn't log anything in case the file doesn't exist
        } catch (IOException e) {
            Log.w(TAG, "getSharedPreferences", e);
        } finally {
            if (result != null && result.stream != null) {
                try {
                    result.stream.close();
                } catch (RuntimeException rethrown) {
                    throw rethrown;
                } catch (Exception ignored) {
                }
            }
        }

        mLoaded = true;
        if (map != null) {
            mMap = map;
            mLastModified = result.mtime;
            mFileSize = result.size;
        } else {
            mMap = new HashMap<String, Object>();
        }
        notifyAll();
    }

    private FileResult getFileInputStream(String filename, long previousSize, long previousTime) throws IOException {
        File file = new File(filename);
        long size = file.length();
        long time = file.lastModified();
        if (previousSize == size && previousTime == time)
            return new FileResult(size, time);
        return new FileResult(new BufferedInputStream(new FileInputStream(filename), 16*1024), size, time);
    }

    private FileResult statFile(String filename)  throws IOException{
        File file = new File(filename);
        return new FileResult(file.length(), file.lastModified());
    }

}
