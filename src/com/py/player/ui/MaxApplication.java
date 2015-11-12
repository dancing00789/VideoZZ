package com.py.player.ui;

import java.util.Locale;
import cn.jpush.android.api.JPushInterface;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class MaxApplication extends Application {

	 private static MaxApplication instance;
	 public final static String SLEEP_INTENT = "com.py.player.SleepIntent";
	 
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		try{
            JPushInterface.setDebugMode(true);
      	    JPushInterface.init(this);
         }catch(Exception e){
            e.printStackTrace();	
         }
		
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String p = pref.getString("set_locale", "");
        if(p != null && !p.equals("")) {
            Locale locale;
            if(p.equals("zh-TW")) {
                locale = Locale.TRADITIONAL_CHINESE;
            } else if(p.startsWith("zh")) {
                locale = Locale.CHINA;
            } else if(p.equals("pt-BR")) {
                locale = new Locale("pt", "BR");
            } else if(p.equals("bn-IN") || p.startsWith("bn")) {
                locale = new Locale("bn", "IN");
            } else {
                if(p.contains("-"))
                    p = p.substring(0, p.indexOf('-'));
                locale = new Locale(p);
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        
		instance = this;
	}

	public static Context getAppContext()
	{
	    return instance;
	}
	    
	public static Resources getAppResources()
	{
	    if(instance == null) return null;
	    return instance.getResources();
	}
}
