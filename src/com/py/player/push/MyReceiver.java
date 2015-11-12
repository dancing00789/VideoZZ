package com.py.player.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "MyReceiver";
	public static boolean isForeground = false;
	public static final String MESSAGE_RECEIVED_ACTION = "com.py.player.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
		Log.d(TAG, "onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
		
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "RECEIVER Registration Id : " + regId);
           //send the Registration Id to your server...
        }else if (JPushInterface.ACTION_UNREGISTER.equals(intent.getAction())){
        	String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
        	Log.d(TAG, "UNREGISTER UnRegistration Id : " + regId);
            //send the UnRegistration Id to your server...
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "MESSAGE_RECEIVED: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "NOTIFICATION_RECEIVED");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "EXTRA_NOTIFICATION_ID: " + notifactionId);
        	
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
        	Log.d(TAG, "ACTION_NOTIFICATION_OPENED");
//        	Intent i = new Intent(context, TestActivity.class);
//        	i.putExtras(bundle);
//        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        	context.startActivity(i);
        	
//        	Intent i = new Intent(context,PushNotificationService.class);
//        	i.putExtras(bundle);
//        	context.startService(i);
        	
        	String content = bundle.getString(JPushInterface.EXTRA_ALERT);
     	    if(! TextUtils.isEmpty(content) && content.startsWith("http")){
//     	    	Toast.makeText(context, "download", Toast.LENGTH_SHORT).show();
     	    	Intent downloadIntent =new Intent(Intent.ACTION_VIEW);
     	    	downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     	    	downloadIntent.setData(Uri.parse(content.trim()));
     	    	context.startActivity(downloadIntent);
     	    }
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
        	
        } else {
        	Log.d(TAG, "Unhandled intent - " + intent.getAction());
        }
	}

	// Print all intent extra data.
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		if (isForeground) {
			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Intent msgIntent = new Intent(MESSAGE_RECEIVED_ACTION);
			msgIntent.putExtra(KEY_MESSAGE, message);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					JSONObject extraJson = new JSONObject(extras);
					if (null != extraJson && extraJson.length() > 0) {
						msgIntent.putExtra(KEY_EXTRAS, extras);
					}
				} catch (JSONException e) {
				}
			}
			context.sendBroadcast(msgIntent);
		}
	}

}
