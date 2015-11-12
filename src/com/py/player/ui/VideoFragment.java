package com.py.player.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.py.player.R;
import com.py.player.db.DbTool;
import com.py.player.util.AsyncImageLoader;
import com.py.player.util.VideoScaner;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class VideoFragment extends Fragment {

	private final String TAG = "VideoActivity";
	public static final String ACTION_DOWNLOAD_PROGRESS = "my_download_progress";
	public static final String ACTION_DOWNLOAD_SUCCESS = "my_download_success";
	public static final String ACTION_DOWNLOAD_FAIL = "my_download_fail";
	protected static final String ACTION_REFRESH= "ACTION_REFRESH";
	protected static final String ACTION_BACKKEY= "ACTION_BACKKEY";

	private ListView list_video;
	private LinearLayout scaning;
	private VideoAdapter mAdapter;
	private List<File> videos = new ArrayList<File>();
    
	public static void startRefresh() {
        Intent intent = new Intent();
        intent.setAction(ACTION_REFRESH);
        MaxApplication.getAppContext().sendBroadcast(intent);
    }
	
    public static void startBackKey() {
        Intent intent = new Intent();
        intent.setAction(ACTION_BACKKEY);
        MaxApplication.getAppContext().sendBroadcast(intent);
    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(ACTION_REFRESH)) {
            	scanVideo();
            } 
            else if(action.equalsIgnoreCase(ACTION_BACKKEY)){
            	if (VideoScaner.getInstance().isScaning) {
        			VideoScaner.getInstance().isScaning = false;
        			return;
        		};
            }
        }
    };
    
   
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        View v = inflater.inflate(R.layout.activity_main, container, false);
        list_video = (ListView) v.findViewById(R.id.list_video);
        scaning = (LinearLayout) v.findViewById(R.id.scaning);
        mAdapter = new VideoAdapter(getActivity(), list_video);
		list_video.setAdapter(mAdapter);

		list_video.setOnItemClickListener(mOnItemClickListener);
		List<Map<String, String>> savedVideo = DbTool.getInstance(getActivity()).listAll(DbTool.table_video, null, null, null);
		if (savedVideo.size() > 0) {
			Log.i(TAG, "get video from database");
			for (Map<String, String> map : savedVideo) {
				File video = new File(map.get("path"));
				synchronized (videos) {
					videos.add(video);
				}
			}
			mAdapter.notifyDataSetChanged();
		} else {
			scanVideo();
		}
		
		IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REFRESH);
        filter.addAction(ACTION_BACKKEY);
        getActivity().registerReceiver(messageReceiver, filter);
        return v;
    }
	  
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			File video = videos.get(position);
			Intent intent = new Intent(getActivity(), VideoActivity.class);
			intent.setData(Uri.parse(video.getPath()));
			startActivity(intent);
		}
	};

	private void scanVideo() {
		Log.i(TAG, "scan start");
		int count = DbTool.getInstance(getActivity()).delete(DbTool.table_video, null,
				null);
		Log.i(TAG, "delete:" + count);
		videos.clear();
		scaning.setVisibility(View.VISIBLE);
		VideoScaner.init(VideoScaner.root, 0, 5,
				new VideoScaner.ScanCallback() {

					@Override
					public void onFound(final File f) {
						Log.i(TAG, "onfound");
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								synchronized (videos) {
									videos.add(f);
									ContentValues values = new ContentValues();
									values.put("path", f.getPath());
									values.put("name", f.getName());
									DbTool.getInstance(getActivity())
											.insert(DbTool.table_video, values);
								}
								if (mAdapter != null)
									mAdapter.notifyDataSetChanged();
							}
						});

					}

					@Override
					public void onFinish() {
						Log.i(TAG, "scan finish");
						getActivity().runOnUiThread(new Runnable() {
							public void run() {
								scaning.setVisibility(View.GONE);
							}
						});

					}
				});
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(messageReceiver);
		VideoScaner.getInstance().isScaning = false;
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			scanVideo();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	static class VideoItemHolder {
		ImageView iV_video;
		TextView tV_title;
		TextView tV_info;
	}

	public class VideoAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private AsyncImageLoader asyncImageLoader;
		private ListView listview;

		public VideoAdapter(Context context, ListView listview) {
			mInflater = LayoutInflater.from(context);
			this.listview = listview;
			asyncImageLoader = new AsyncImageLoader(context);
		}

		@Override
		public int getCount() {
			return videos.size();
		}

		@Override
		public Object getItem(int position) {
			return videos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("NewApi")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			VideoItemHolder holder = null;
	
			if (convertView == null) {
				holder = new VideoItemHolder();

				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.iV_video = (ImageView) convertView
						.findViewById(R.id.iv_video);
				holder.tV_title = (TextView) convertView
						.findViewById(R.id.tv_title);
				holder.tV_info = (TextView) convertView
						.findViewById(R.id.tv_info);
		
				convertView.setTag(holder);
			} else {
				holder = (VideoItemHolder) convertView.getTag();
			}
			File video = videos.get(position);
	
			final String videopath = video.getPath();
			holder.iV_video.setTag(videopath);
			final Bitmap cachedImage = asyncImageLoader.loadImage(
					video.getPath(), new AsyncImageLoader.ImageCallback() {

						@Override
						public void imageLoaded(Bitmap image, String path) {
							ImageView imageViewByTag = (ImageView) listview
									.findViewWithTag(videopath);
							if (imageViewByTag != null) {
								imageViewByTag.setImageBitmap(image);
							}

						}
					});
			if (cachedImage == null) {
				holder.iV_video.setImageBitmap(null);
			} else {
				holder.iV_video.setImageBitmap(cachedImage);
			}
			holder.tV_title.setSelected(true);
			holder.tV_title.setText(video.getName());
			
			return convertView;
		}
	}
}
