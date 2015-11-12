package com.py.player.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;

public class VideoScaner implements Runnable {
	
	private static final String TAG = "FileLister";
	public static final File root = Environment.getExternalStorageDirectory();
	private static VideoScaner mScaner;
	public volatile boolean isScaning = false;
	private File dir;
	private int deepth;
	private int maxDeepth;
	private ScanCallback callback;
	private Thread scanThread;

	private VideoScaner() {

	}

	public interface ScanCallback {
		void onFound(File f);

		void onFinish();
	}

	private FileFilter videoFilter = new FileFilter() {
		Pattern pattern = Pattern.compile(".*\\.(avi|rmvb|flv|mp4|3gp|wmv|mkv)");

		@Override
		public boolean accept(File pathname) {
			Matcher matcher = pattern.matcher(pathname.getName());
			return pathname.isDirectory() || matcher.matches();
		}
	};

	public static VideoScaner getInstance() {
		if (mScaner == null)
			mScaner = new VideoScaner();
		return mScaner;
	}

	public static void init(File dir, int deepth, int maxDeepth,
			ScanCallback callback) {
		if (mScaner == null)
			mScaner = new VideoScaner();
		mScaner.isScaning = true;
		mScaner.dir = dir;
		mScaner.deepth = deepth;
		mScaner.maxDeepth = maxDeepth;
		mScaner.callback = callback;
		mScaner.scanThread = new Thread(mScaner);
		mScaner.scanThread.start();
	}

	public void asyncScan(File dir, int deepth, int maxDeepth,
			ScanCallback callback) {
		if (!isScaning)
			return;
		File[] files = dir.listFiles(videoFilter);
		if (files == null)
			return;
		for (File f : files) {
			if (f.isDirectory()) {
				if (!isScaning)
					return;
				if (deepth < maxDeepth)
					asyncScan(f, deepth + 1, maxDeepth, callback);
			} else {
				if (!isScaning)
					return;
				Log.i(TAG, "deepth:" + deepth + "  file:" + f.getPath());
				callback.onFound(f);
			}
		}
		if (deepth == 0)
			callback.onFinish();
	}

	@Override
	public void run() {
		asyncScan(dir, deepth, maxDeepth, callback);
	}
}
