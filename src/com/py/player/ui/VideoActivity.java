package com.py.player.ui;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import com.py.player.util.WeakHandler;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.widget.VideoView;
import com.py.player.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VideoActivity extends Activity implements OnInfoListener,OnCompletionListener {

	private final String TAG = "VideoActivity";
	private VideoView mVideoView;
	private ProgressBar pb;
	private LinearLayout track;
	private TextView tV_track;
	private View mOverlayFooter;
    private ImageButton mPlayPause;
    private TextView mTime;
    private SeekBar mSeekbar;
    private TextView mLength;
    private View mOverlayHeader;
    private TextView mBarTitle;
    private TextView mSysTime;
    private TextView mBattery;
	private PointF start;
	private Dir dir = null;
	private ImageButton mLock;
    private boolean mIsLocked = false;
    private boolean mShowing = true;
//    private MediaController mMediaController;
	private boolean isLeftSideMove = true;
	private AudioManager mAudioManager;
	private int currentVolume = 0;

	public enum Dir {
		x, y
	}
	private boolean mIsFirstBrightnessGesture = true;
    private SharedPreferences mSettings;
    private boolean mDragging;
	private int mSurfaceYDisplayRange;
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = 3600000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int FADE_OUT_INFO = 3;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                int batteryLevel = intent.getIntExtra("level", 0);
                if (batteryLevel >= 50)
                    mBattery.setTextColor(Color.GREEN);
                else if (batteryLevel >= 30)
                    mBattery.setTextColor(Color.YELLOW);
                else
                    mBattery.setTextColor(Color.RED);
                mBattery.setText(String.format("%d%%", batteryLevel));
            }
            else if (action.equalsIgnoreCase(MaxApplication.SLEEP_INTENT)) {
                finish();
            }
        }
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		 unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.activity_video);
		
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		// Clear the resume time, since it is only used for resumes in external videos.
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong("last_play_time", -1);
        editor.commit();
		
		mOverlayFooter = findViewById(R.id.progress_overlay);
		mTime = (TextView) findViewById(R.id.player_overlay_time);
	    mLength = (TextView) findViewById(R.id.player_overlay_length);
	    mPlayPause = (ImageButton) findViewById(R.id.player_overlay_play);
        mPlayPause.setOnClickListener(mPlayPauseListener);
        mSeekbar = (SeekBar) findViewById(R.id.player_overlay_seekbar);
        mSeekbar.setOnSeekBarChangeListener(mSeekListener);
		mVideoView = (VideoView) findViewById(R.id.videoView);
		mVideoView.setOnCompletionListener(this);	
		pb = (ProgressBar) findViewById(R.id.probar);
		track = (LinearLayout) findViewById(R.id.track);
		tV_track = (TextView) findViewById(R.id.tV_track);
		mOverlayHeader = findViewById(R.id.player_overlay_header);  	
	    mBarTitle = (TextView) findViewById(R.id.player_overlay_title);
	    mSysTime = (TextView) findViewById(R.id.player_overlay_systime);
	    mBattery = (TextView) findViewById(R.id.player_overlay_battery);
	    mLock = (ImageButton) findViewById(R.id.lock_overlay_button);
	    mLock.setOnClickListener(mLockListener);

	    String title = "";
		if (getIntent().getAction() != null && getIntent().getAction().equals("py.player.START")) {
        	String mLocation = getIntent().getExtras().getString("itemLocation"); //External ACTION_VIEW 
        	mVideoView.setVideoPath(mLocation);
        	title = mLocation;
        }
		else{
	    	Intent intent = getIntent();
	    	Uri uri = intent.getData();
	    	mVideoView.setVideoURI(uri);
	    	title = uri.getPath();
	    	try {
	            title = URLDecoder.decode(title, "UTF-8");
	            title = new File(title).getName();
	        } catch (UnsupportedEncodingException e) {
	        } catch (IllegalArgumentException e) {
	        }
	    }

    	try {
            title = URLDecoder.decode(title, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        } catch (IllegalArgumentException e) {
        }
    	if (title.startsWith("file:")) {
           title = new File(title).getName();
        }
//        int dotIndex = title.lastIndexOf('.');
//        if (dotIndex != -1)
//            title = title.substring(0, dotIndex);
        
//		mMediaController = new MediaController(this);
//		mVideoView.setMediaController(mMediaController);
    	
		mVideoView.requestFocus();
		mVideoView.setOnInfoListener(this);
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				// optional need Vitamio 4.0
				mediaPlayer.setPlaybackSpeed(1.0f);
			}
		});
		
        mBarTitle.setText(title);
	    mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
	    
	    IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(MaxApplication.SLEEP_INTENT);
        registerReceiver(mReceiver, filter);
	}
	
    private final OnClickListener mLockListener = new OnClickListener() {
        @Override
        public void onClick(View v) {	
            if (mIsLocked) {
                mIsLocked = false;
                unlockScreen();
            } else {
                mIsLocked = true;
                lockScreen();
            }
        }
	};

	private void lockScreen() {
       showInfo(getString(R.string.locked), 1000);
       mLock.setBackgroundResource(R.drawable.ic_locked);
       hideOverlay(true);     
   }

   private void unlockScreen() {
	   showInfo(getString(R.string.unlocked), 1000);
       mLock.setBackgroundResource(R.drawable.ic_lock);     
       mShowing = false;
       showOverlay();
   }
	
   private void hideOverlay(boolean fromUser) {
       if (mShowing) {
    	   mHandler.removeMessages(SHOW_PROGRESS);
           Log.i(TAG, "remove View!");
           if (!fromUser && !mIsLocked) { 
        	   mOverlayFooter.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));  
               mLock.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
               mOverlayHeader.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out)); 
           }
           mOverlayFooter.setVisibility(View.GONE);
           mOverlayHeader.setVisibility(View.GONE);
           mLock.setVisibility(View.GONE);
           mShowing = false;
       }
   }
   
   private void showOverlay() {
	   showOverlay(OVERLAY_TIMEOUT);
   }
   
   private void showOverlay(int timeout) {
	   mHandler.sendEmptyMessage(SHOW_PROGRESS);
	   if (!mShowing) {
           mShowing = true;
           if (!mIsLocked) {
               mOverlayFooter.setVisibility(View.VISIBLE);
               mOverlayHeader.setVisibility(View.VISIBLE);
           }else{
        	   mOverlayFooter.setVisibility(View.GONE);
        	   mOverlayHeader.setVisibility(View.GONE);
           }
           mLock.setVisibility(View.VISIBLE);
       }
       
       Message msg = mHandler.obtainMessage(FADE_OUT);
       if (timeout != 0) {
           mHandler.removeMessages(FADE_OUT);
           mHandler.sendMessageDelayed(msg, timeout);
       }
       updateOverlayPausePlay();
   }
   
	private float mTouchX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (mIsLocked) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (!mShowing) {
                    showOverlay();
                 
                } else {
                    hideOverlay(true);
                }
            }
            return false;
	    }
		
		showOverlay();
		DisplayMetrics screen = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(screen);
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchAction = TOUCH_NONE;
			Log.i(TAG, "touch");
			mTouchX = event.getRawX();
			start = new PointF(x, y);
			dir = null;
			break;
			
		case MotionEvent.ACTION_MOVE:
			if (dir == null) {
				float xOffset = Math.abs(x - start.x);
				float yOffset = Math.abs(y - start.y);
				if (xOffset > 20) {
					dir = Dir.x;
					Log.i(TAG, "trackX");	
				} else if (yOffset > 20) {
					if((int)mTouchX > (screen.widthPixels / 2)){
						isLeftSideMove =  false;
					}else if((int)mTouchX < (screen.widthPixels / 2)){
						isLeftSideMove = true;
					}
					dir = Dir.y;
					Log.i(TAG, "trackY");
					currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				}
			}
			
			if (dir != null) {
				track(x, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			doSeekTouch(x, y);
			Log.i(TAG, "trackOver");
			break;

		}
//		return super.onTouchEvent(event);
		return mTouchAction != TOUCH_NONE;
	}
	
	private int mTouchAction;
	private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;
    
	private int mAudioMax;
	private void track(float x, float y) {
		if (dir == Dir.x) { 
			if (mVideoView != null) {
//				track.setVisibility(View.VISIBLE);
				float offset = x - start.x;
//				int pos = (int) mVideoView.getCurrentPosition() + (int) offset* 1000;
//				pos = pos < 0 ? 0 : pos;
//				pos = (int) (pos > mVideoView.getDuration() ? mVideoView
//						.getDuration() : pos);
				
//				DisplayMetrics screen = new DisplayMetrics();
//		        getWindowManager().getDefaultDisplay().getMetrics(screen);
//				float xgesturesize = ((offset / screen.xdpi) * 2.54f);
//				// Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
//		        int jump = (int) (Math.signum(xgesturesize) * ((600000 * Math.pow((xgesturesize / 8), 4)) + 3000));
//
//		        int time = (int) mVideoView.getCurrentPosition();
//		        int length =  (int) mVideoView.getDuration();
//		        // Adjust the jump
//		        if ((jump > 0) && ((time + jump) > length))
//		        	jump = (int) (length - time);
//		        if ((jump < 0) && ((time + jump) < 0))
//		        	jump = (int) -time;
//		        
////				tV_track.setText("" + StringTools.generateTime(pos));
//
//		        if (length > 0)
//		            //Show the jump's size
//		            showInfo(String.format("%s%s (%s)",
//		                    jump >= 0 ? "+" : "",
//		                    		VideoActivity.millisToString(jump),
//		                    		VideoActivity.millisToString(time + jump)), 1000);
//		        else
//		            showInfo(getString(R.string.unseekable_stream), 1000);
		        
//				showInfo("" + StringTools.generateTime(jump),1000);
			}
		} else if (dir == Dir.y) {
			float offset = start.y - y;
			if(!isLeftSideMove){
				doVolumeTouch(offset);
			}else{
				doBrightnessTouch(offset);
			}
		}
	}

	private void showInfo(String text, int duration) {
		track.setVisibility(View.VISIBLE);
		tV_track.setText(text);
	    mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}
	
	private void fadeOutInfo() {
        if (track.getVisibility() == View.VISIBLE)
        	track.startAnimation(AnimationUtils.loadAnimation(
                    VideoActivity.this, android.R.anim.fade_out));
        track.setVisibility(View.INVISIBLE);
    }
	 
	private void doVolumeTouch(float offset) {
		if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
		 mTouchAction = TOUCH_VOLUME;
		 
		 DisplayMetrics screen = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(screen);
		 if (mSurfaceYDisplayRange == 0)
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
		int delta = (int) ((offset / mSurfaceYDisplayRange) * mAudioMax);
        int vol = (int) Math.min(Math.max(currentVolume + delta, 0), mAudioMax);
//		int vol = currentVolume + (int) (offset / 20);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
		showInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(vol),1000);
	}
	
	
	private void initBrightnessTouch() {
        float brightnesstemp = 0.01f;
        // Initialize the layoutParams screen brightness
        try {
            brightnesstemp = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightnesstemp;
        getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    private void doBrightnessTouch(float offset) {
    	
    	 if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
             return;
    	 mTouchAction = TOUCH_BRIGHTNESS;
  
    	 DisplayMetrics screen = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(screen);
         if (mSurfaceYDisplayRange == 0)
             mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);      
        if (mIsFirstBrightnessGesture) initBrightnessTouch();

        float delta =  offset / mSurfaceYDisplayRange * 0.07f;
        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness =  Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);
        // Set Brightness
        getWindow().setAttributes(lp);
        showInfo(getString(R.string.brightness) + '\u00A0' + Math.round(lp.screenBrightness*15),1000);
    }
    
	private void doSeekTouch(float x, float y) {
		if (dir == Dir.x && mVideoView != null) {
//			track.setVisibility(View.GONE);
			float offset = x - start.x;
//			int pos = (int) (mVideoView.getCurrentPosition() + (int) offset * 1000);
//			pos = pos < 0 ? 0 : pos;
//			pos = (int) (pos > mVideoView.getDuration() ? mVideoView
//					.getDuration() : pos);
//			mVideoView.seekTo(pos);
		    if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK)
		            return;
		    mTouchAction = TOUCH_SEEK;
		        
			DisplayMetrics screen = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(screen);
			float xgesturesize = ((offset / screen.xdpi) * 2.54f);
			// Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
	        int jump = (int) (Math.signum(xgesturesize) * ((600000 * Math.pow((xgesturesize / 8), 4)) + 3000));

	        int time = (int) mVideoView.getCurrentPosition();
	        int length =  (int) mVideoView.getDuration();
	        // Adjust the jump
	        if ((jump > 0) && ((time + jump) > length))
	        	jump = (int) (length - time);
	        if ((jump < 0) && ((time + jump) < 0))
	        	jump = (int) -time;
	        
//			tV_track.setText("" + StringTools.generateTime(pos));

	        if (length > 0)
	            //Show the jump's size
	        	showInfo(String.format("%s%s",
	                    jump >= 0 ? "+" : "",
	                    		VideoActivity.millisToString(jump),
	                    		""), 1000);
	        else
	            showInfo(getString(R.string.unseekable_stream), 1000);
	        
	        
	        if (length > 0)
	        	mVideoView.seekTo(time + jump);
	        mPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
		}
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			if (mVideoView.isPlaying()) {
				mVideoView.pause();
				pb.setVisibility(View.VISIBLE);
			}
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			mVideoView.start();
			pb.setVisibility(View.GONE);
			break;
		case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
			break;
		}
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		finish();
	}
	
	private final Handler mHandler = new VideoHandler(this);

    private static class VideoHandler 
    extends WeakHandler<VideoActivity> {
    	
        public VideoHandler(VideoActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
        	VideoActivity activity = getOwner();
            if(activity == null) return;

            switch (msg.what) {
                case FADE_OUT:
                    activity.hideOverlay(false);
                    break;
                    
                case SHOW_PROGRESS:
                    int pos = activity.setOverlayProgress();
//                    if (activity.canShowProgress()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
//                    }
                    break;

                case FADE_OUT_INFO:
                    activity.fadeOutInfo();
                    break;

            }
        }
    };
    
    static String millisToString(long millis, boolean text) {
        boolean negative = millis < 0;
        millis = java.lang.Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
        if (text) {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
            else if (min > 0)
                time = (negative ? "-" : "") + min + "min";
            else
                time = (negative ? "-" : "") + sec + "s";
        }
        else {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
            else
                time = (negative ? "-" : "") + min + ":" + format.format(sec);
        }
        return time;
    }
    
    public static String millisToString(long millis)
    {
        return millisToString(millis, false);
    }

   
    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mDragging = true;
            showOverlay(OVERLAY_INFINITE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mDragging = false;
            showOverlay();
            mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, 0);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        	 if (fromUser) {
        		 mVideoView.seekTo(progress);
                 setOverlayProgress();
                 mTime.setText(millisToString(progress));
                 showInfo(millisToString(progress),1000);
            }
        }
    };

    private int setOverlayProgress() { 
      
        int time = (int) mVideoView.getCurrentPosition();
        int length = (int) mVideoView.getDuration();
        mSeekbar.setMax(length);
        mSeekbar.setProgress(time);
        mSysTime.setText(DateFormat.getTimeFormat(this).format(new Date(System.currentTimeMillis())));
        if (time >= 0) mTime.setText(millisToString(time));
        if (length >= 0) mLength.setText(millisToString(length));
        return time;
        
    }
    
    private boolean canShowProgress() {
    	Log.e(TAG, "mDragging-->"+mDragging);
    	Log.e(TAG, "mShowing-->"+mShowing);
    	Log.e(TAG, "isPlaying-->"+mVideoView.isPlaying());
        return !mDragging && mShowing && mVideoView.isPlaying();
    }
    
    private final OnClickListener mPlayPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mVideoView.isPlaying()){
            	mVideoView.pause();
                mPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
            }
            else{
            	mVideoView.start();
            	mPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
            }
            showOverlay();
        }
    };
    
    private void updateOverlayPausePlay() {
        if (mVideoView == null) return;
        mPlayPause.setBackgroundResource(mVideoView.isPlaying() ? android.R.drawable.ic_media_pause: android.R.drawable.ic_media_play);
    }

    @Override
    protected void onPause() {
        super.onPause();  
        long time = mVideoView.getCurrentPosition();
        long length = mVideoView.getDuration();
        //remove saved position if in the last 0.1 seconds
        if (length - time < 100)
            time = 0;
        else
            time -= 100; // go back 0.1 seconds, to compensate loading time
        SharedPreferences.Editor editor = mSettings.edit();
        if (time >= 0 ) {  // Save position
           editor.putLong("last_play_time", time);  // store time just for onResume()
        }
        editor.commit();
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		showOverlay();
	    mPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
	    long rTime = mSettings.getLong("last_play_time", -1);
        Editor editor = mSettings.edit();
        editor.putLong("last_play_time", -1);
        editor.commit();
        if(rTime > 0) mVideoView.seekTo(rTime);
		super.onResume();
	}
	
}
