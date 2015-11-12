package com.py.player.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.py.player.ui.MaxApplication;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;

@SuppressLint("NewApi")
public class ScanPathUtil {

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static String[] getStoragePaths() {
        List<String> pathsList = new ArrayList<String>();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
           
        } else {
            StorageManager storageManager = (StorageManager) MaxApplication.getAppContext().getSystemService(Context.STORAGE_SERVICE);
            try {
                Method method = StorageManager.class.getDeclaredMethod("getVolumePaths");
                method.setAccessible(true);
                Object result = method.invoke(storageManager);
                if (result != null && result instanceof String[]) {
                    String[] pathes = (String[]) result;
                    StatFs statFs;
                    for (String path : pathes) {
                        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                            statFs = new StatFs(path);
                            if (statFs.getBlockCount() * statFs.getBlockSize() != 0) {
                                pathsList.add(path);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                File externalFolder = Environment.getExternalStorageDirectory();
                if (externalFolder != null) {
                    pathsList.add(externalFolder.getAbsolutePath());
                }
            }
        }
        return pathsList.toArray(new String[pathsList.size()]);
    }

    public static String[] getMediaDirectories() {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(getStoragePaths()));
        return list.toArray(new String[list.size()]);
    }
}
