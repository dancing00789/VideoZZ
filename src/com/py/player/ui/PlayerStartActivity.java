package com.py.player.ui;

import io.vov.vitamio.LibsChecker;
import java.lang.reflect.Field;
import com.py.player.R;
import cn.jpush.android.api.JPushInterface;
import com.py.player.widget.PagerSlidingTabStrip;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class PlayerStartActivity extends FragmentActivity implements OnClickListener{

	private VideoFragment fFragment;
	private FileBrowseFragment sFragment;
	private ImageButton ib_refresh;
	private TextView tv_title;
	private PagerSlidingTabStrip tabs;
	private DisplayMetrics dm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);
		setOverflowShowingAlways();

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText(getString(R.string.app_name));
		
		ib_refresh = (ImageButton) findViewById(R.id.ib_refresh);
		ib_refresh.setOnClickListener(this);
   	
		dm = getResources().getDisplayMetrics();
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		tabs.setViewPager(pager);
		setTabsValue();
		
	}

	private void setTabsValue() {
		
		tabs.setShouldExpand(true);
		tabs.setDividerColor(Color.TRANSPARENT);
		tabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		tabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, dm));
		tabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, dm));
		tabs.setIndicatorColor(Color.RED);
		tabs.setSelectedTextColor(Color.RED);
		tabs.setTabBackground(0);
		
	}

	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private final String[] titles = {
				getString(R.string.native_video),
				getString(R.string.native_file),
		};
		
		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				if (fFragment == null) {
					fFragment = new VideoFragment();
				}
				return fFragment;
			case 1:
				if (sFragment == null) {
					sFragment = new FileBrowseFragment();
				}
				return sFragment;
	
			default:
				return null;
			}
		}
	}


	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.ib_refresh:
			VideoFragment.startRefresh();
	   		 break;
   	    }
	}
	
	@Override
	public void onBackPressed() {
		VideoFragment.startBackKey();
		super.onBackPressed();
	}
	
    @Override
    protected void onResume() {
    	
        super.onResume();
        JPushInterface.onResume(this);
        /* Load media items from database and storage */
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(this);
      
    }
   
}
