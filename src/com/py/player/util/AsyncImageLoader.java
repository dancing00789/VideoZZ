package com.py.player.util;

import io.vov.vitamio.ThumbnailUtils;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

public class AsyncImageLoader {

	private Context context;
	private HashMap<String, SoftReference<Bitmap>> imageCache;

	public AsyncImageLoader(Context context) {
		this.context = context;
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	public Bitmap loadImage(final String path,
			final ImageCallback imageCallback) {
		if (imageCache.containsKey(path)) {
			SoftReference<Bitmap> softReference = imageCache.get(path);
			Bitmap bitmap = softReference.get();
			if (bitmap != null) {
				return bitmap;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Bitmap) message.obj, path);
			}
		};
		new Thread() {
			@Override
			public void run() {
				Bitmap bitmap = loadThumbnail(path);
				imageCache.put(path, new SoftReference<Bitmap>(bitmap));
				Message message = handler.obtainMessage(0, bitmap);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}

	public Bitmap loadThumbnail(String path) {
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(context, path,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap image, String path);

	}
}