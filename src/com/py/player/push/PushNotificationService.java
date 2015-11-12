package com.py.player.push;

import cn.jpush.android.api.JPushInterface;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

public class PushNotificationService extends Service
{
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Bundle bundle = intent.getExtras();
		String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
	    String content = bundle.getString(JPushInterface.EXTRA_ALERT);
	    if(! TextUtils.isEmpty(content) && content.startsWith("http:")){
	    	Intent downloadUrl =new Intent(Intent.ACTION_VIEW);
	    	downloadUrl.setData(Uri.parse(content));
			this.startActivity(downloadUrl);
	    }
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}
