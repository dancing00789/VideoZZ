package com.py.player.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpGet {
	private static final String TAG = "HttpGet";

	public interface Callback {
		void onSuccess(String result);

		void onFailed(String mes);
	}


	public static void getContent(String url, final HttpGet.Callback callback) {
		final String mUrl = url;

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					URL url = new URL(mUrl);
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setConnectTimeout(30000);
					connection.setRequestMethod("GET");

					int code = connection.getResponseCode();
					if (code == 200) {
						String result = changeInputStream(connection
								.getInputStream());
						Log.i(TAG, "httpGet Success");
						callback.onSuccess(result);
					} else {
						Log.i(TAG, "httpGet faile:connect failed");
						callback.onFailed("connect failed");
					}
				} catch (Exception e) {
					// TODO: handle exception
					Log.i(TAG, "httpGet failed:" + e.getMessage());
					callback.onFailed(e.getMessage());
					e.printStackTrace();
				}

			}
		}).start();

	}

	private static String changeInputStream(InputStream inputStream) {
		// TODO Auto-generated method stub
		String jsonString = "";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int len = 0;
		byte[] data = new byte[1024];
		try {
			while ((len = inputStream.read(data)) != -1) {
				outputStream.write(data, 0, len);
			}
			jsonString = new String(outputStream.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonString;
	}

}
