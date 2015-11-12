package com.py.player.ui;

import com.py.player.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class FileBrowseFragment extends ListFragment{
   
	public final static String TAG = FileBrowseFragment.class.getSimpleName();
    private FileBrowseAdapter mFileBrowseAdapter;
    private ListView mListView;
    public FileBrowseFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileBrowseAdapter = new FileBrowseAdapter(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        getActivity().registerReceiver(messageReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.file_view, container, false);
        setListAdapter(mFileBrowseAdapter);
        mListView = (ListView)v.findViewById(android.R.id.list);
        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
                if(mFileBrowseAdapter.isChildFile(position)) {
                    return false;
                } else {
                    return true; 
                }
            }
        });
        return v;
    }

    @Override
    public void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(messageReceiver);
    }

    @Override
    public void onListItemClick(ListView l, View v, int p, long id) {
        int success = mFileBrowseAdapter.browse(p);

        if(success < 0)
        	openVideoFile(p);
        else
            setSelection(success);
    }

    public boolean isRootDirectory() {
        return mFileBrowseAdapter.isRoot();
    }

    public void showParentDirectory() {
        int success = mFileBrowseAdapter.browse("..");

        if(success >= 0)
            setSelection(success);
    };

    private void openVideoFile(int p) {

        String videoFile = mFileBrowseAdapter.getVideoLocation(p);
        Intent intent = new Intent(getActivity(), VideoActivity.class);
		intent.setData(Uri.parse(videoFile));
		startActivity(intent);
    }

    public void refresh() {
        if (mFileBrowseAdapter != null)
        	mFileBrowseAdapter.refresh();
    }

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED) ||
                action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED) ||
                action.equalsIgnoreCase(Intent.ACTION_MEDIA_REMOVED) ||
                action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {
                    refresh();
                }
	        }
	    };
   }

