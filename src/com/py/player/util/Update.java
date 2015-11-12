package com.py.player.util;

import org.json.JSONException;
import org.json.JSONObject;

public class Update {
	public final static String URL = "http://ad.kpl01.com:8088/data/app2/VideoZZ.json";

	public interface Callback {
		void onUpdate(int version, String url);
	}

	public static void update(final Callback callback) {

		HttpGet.getContent(URL, new HttpGet.Callback() {

			@Override
			public void onSuccess(String result) {
				int Version = -1;
				String Url = "";
				if (result != null) {
					try {
						JSONObject jsonObject = new JSONObject(result);
						Version = jsonObject.getInt("Version");
						Url = jsonObject.getString("Url");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				callback.onUpdate(Version, Url);

			}

			@Override
			public void onFailed(String mes) {
				callback.onUpdate(-1, null);

			}
		});
	}
}
