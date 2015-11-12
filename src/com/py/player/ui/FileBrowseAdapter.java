package com.py.player.ui;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import com.py.player.R;
import com.py.player.util.ScanPathUtil;
import com.py.player.util.StringTools;
import com.py.player.util.Util;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileBrowseAdapter extends BaseAdapter {
	
    public final static String TAG = FileBrowseAdapter.class.getSimpleName();
    
    public final static void log(String printContent){
    	Log.e(TAG, printContent);
    }
    
    public static boolean videoPath(String f) {

        StringBuilder sb = new StringBuilder();
        sb.append(".+(\\.)((?i)(");
        sb.append("avi");
        sb.append('|');
        sb.append("rmvb");
        sb.append('|');
        sb.append("flv");
        sb.append('|');
        sb.append("mp4");
        sb.append('|');
        sb.append("3gp");
        sb.append('|');
        sb.append("wmv");
        sb.append('|');
        sb.append("mkv");
        sb.append("))");
        log(sb.toString());
        String video_filter = sb.toString();
        
        return Pattern.compile(video_filter, Pattern.CASE_INSENSITIVE).matcher(f).matches();
    }

    static class ViewHolder {
    	
       public  View layout;
       public TextView title;
       public TextView text;
       public ImageView icon;
    }

    private LayoutInflater mInflater;
    private FileTreeNode mRootNode;
    private FileTreeNode mCurrentNode;
    private String mCurrentDir;
    private String mCurrentRoot;

    private int mAlignMode; 

    public FileBrowseAdapter(Context context) {
    	FileBrowseAdapterCore(context, null);
    }

    private void FileBrowseAdapterCore(Context ctx, String rootDir) {
        if (rootDir != null)
            rootDir = StringTools.stripTrailingSlash(rootDir);
        Log.v(TAG, "rootDir is " + rootDir);
        mInflater = LayoutInflater.from(ctx);
        mRootNode = new FileTreeNode(rootDir);
        mCurrentDir = rootDir;
        this.fullNode(mRootNode, rootDir);
        mCurrentNode = mRootNode;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        mAlignMode = Integer.valueOf(preferences.getString("title_alignment_mode", "0"));
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getCount() {
        return mCurrentNode.children.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	FileTreeNode selectedNode = mCurrentNode.children.get(position);
    	ViewHolder holder;
        View v = convertView;

        Context context = MaxApplication.getAppContext();

        /* If view not created */
        if (v == null) {
            v = mInflater.inflate(R.layout.file_adapter_item, parent, false);
            holder = new ViewHolder();
            holder.layout = v.findViewById(R.id.layout_item);
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.title.setSelected(true);
            Util.setAlignModeByPref(mAlignMode, holder.title);
            holder.text = (TextView) v.findViewById(R.id.text);
            holder.icon = (ImageView) v.findViewById(R.id.dvi_icon);
            v.setTag(holder);
        } else
            holder = (ViewHolder) v.getTag();

        String holderText = "";
        if(selectedNode.isFile()) {
            holder.title.setText(selectedNode.fullName);
        } else
            holder.title.setText(selectedNode.getShowName());

        if(selectedNode.fullName.equals(".."))
            holderText = context.getString(R.string.parent_folder);
        
        else if(!selectedNode.isFile()) {
            int folderCount = selectedNode.subfolderCount();
            int videoFileCount = selectedNode.subfilesCount();
            holderText = "";

            if(folderCount > 0)
                holderText += context.getResources().getQuantityString(
                        R.plurals.subfolders_quantity, folderCount, folderCount
                );
            if(folderCount > 0 && videoFileCount > 0)
                holderText += ", ";
            if(videoFileCount > 0)
                holderText += context.getResources().getQuantityString(
                        R.plurals.mediafiles_quantity, videoFileCount,
                        videoFileCount);
        }
        holder.text.setText(holderText);
        if(selectedNode.isFile())
            holder.icon.setImageResource(R.drawable.logo);
        else
            holder.icon.setImageResource(R.drawable.ic_menu_folder);

        return v;
    }

    public int browse(int position) {
    	FileTreeNode selectedNode = mCurrentNode.children.get(position);
        if(selectedNode.isFile()) return -1;
        return browse(selectedNode.fullName);
    }

    public int browse(String directoryName) {
        if (this.mCurrentDir == null) {
            String storages[] = ScanPathUtil.getMediaDirectories();
            for (String storage : storages) {
                storage = StringTools.stripTrailingSlash(storage);
                if (storage.endsWith(directoryName)) {
                    this.mCurrentRoot = storage;
                    this.mCurrentDir = StringTools.stripTrailingSlash(storage);
                    break;
                }
            }
        } else {
            try {
            	this.mCurrentDir = new URI(
                        PathToURI(this.mCurrentDir + "/" + directoryName))
                        .normalize().getPath();
                this.mCurrentDir = StringTools.stripTrailingSlash(this.mCurrentDir);

                if (this.mCurrentDir.equals(getParentDir(this.mCurrentRoot))) {
                    // Returning to the storage list
                    this.mCurrentDir = null;
                    this.mCurrentRoot = null;
                }
            } catch(URISyntaxException e) {
                Log.e(TAG, "URISyntaxException in browse()", e);
                return -1;
            } catch(NullPointerException e) {
                Log.e(TAG, "NullPointerException in browse()", e);
                return -1;
            }
        }

        Log.d(TAG, "Browsing to " + this.mCurrentDir);

        int currentDirPosition = 0;
        if(directoryName.equals("..")){
            currentDirPosition = mCurrentNode.parent.getChildPosition(mCurrentNode);
            this.mCurrentNode = this.mCurrentNode.parent;
        } else {
            this.mCurrentNode = this.mCurrentNode.getChild(directoryName);
            if(mCurrentNode.subfolderCount() < 1) {
                // Clear the ".." entry
                this.mCurrentNode.children.clear();
                this.fullNode(mCurrentNode, mCurrentDir);
            }
        }

        this.notifyDataSetChanged();
        return (currentDirPosition == -1) ? 0 : currentDirPosition;
    }

    public boolean isChildFile(int position) {
    	FileTreeNode selectedNode = mCurrentNode.children.get(position);
        return selectedNode.isFile();
    }

    public String getVideoLocation(int position) {
        if (position >= mCurrentNode.children.size())
            return null;
        return PathToURI(
                this.mCurrentDir + "/" + mCurrentNode.children.get(position).fullName
        );
    }

    public boolean isRoot() {
        return mCurrentDir == null;
    }

    public String getmCurrentDir() {
        return mCurrentDir;
    }

    public ArrayList<String> getAllMediaLocations() {
        ArrayList<String> a = new ArrayList<String>();
        // i = 1 to exclude ".." folder
        for(int i = 1; i < mCurrentNode.children.size(); i++)
            if(mCurrentNode.children.get(i).isFile)
                a.add(getVideoLocation(i));
        return a;
    }

    public void refresh() {
        for(FileTreeNode n : this.mCurrentNode.children)
            n.children.clear();
        this.mCurrentNode.children.clear();
        this.fullNode(mCurrentNode, mCurrentDir);

        this.notifyDataSetChanged();
    }

    private String getParentDir(String path) {
        try {
        	 path = new URI(PathToURI(path + "/..")).normalize().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return StringTools.stripTrailingSlash(path);
    }

    private String getVisibleName(File file) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getPath())) {
                return MaxApplication.getAppContext().getString(R.string.internal_memory);
            }
        }
        return file.getName();
    }
    
    public String PathToURI(String path){
    	 return path;
    }
    
    public void readDir(String path,ArrayList<String> files){

        File file = new File(path);
        File[] subFile = file.listFiles();
        
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
        	files.add(subFile[iFileLength].getName());
        }
    }
    
    public boolean isPathDirectory(String path){
    	File file = new File(path);
    	if(file.exists()){
    		return file.isDirectory();
    	}else{
    		return false;
    	}
    }
    
    private void fullNode(FileTreeNode n, String path) {
    	fullNode(n, path, 0);
    }

    private void fullNode(FileTreeNode n, String path, int depth) {
    	
        if (path == null) { //root
            String storages[] = ScanPathUtil.getMediaDirectories();
            for (String storage : storages) { //sdcard0 sdcard1
                File file = new File(storage);
                FileTreeNode rootNode = new FileTreeNode(file.getName(), getVisibleName(file));
                rootNode.isFile = false;
                this.fullNode(rootNode, storage, 0);
                n.addChild(rootNode);
            }
            return;
        }

        File file = new File(path);
        if(!file.exists() || !file.isDirectory())
            return;

        ArrayList<String> files = new ArrayList<String>();
        readDir(path, files);
        
        StringBuilder sb = new StringBuilder(100);
        if(files == null || files.size() < 1) {
            //return
        } else {
            for(int i = 0; i < files.size(); i++) {
                String filename = files.get(i);
                if(filename.equals(".") || filename.equals("..") || filename.startsWith(".")) continue;

                FileTreeNode ftn = new FileTreeNode(filename);
                ftn.isFile = false;
                sb.append(path);
                sb.append("/");
                sb.append(filename);
                String newPath = sb.toString();
                sb.setLength(0);
                
                if (isPathDirectory(newPath) && depth < 10) {
                    ArrayList<String> files_int = new ArrayList<String>();
                    readDir(newPath, files_int);
                    if(files_int.size() < 8) { 
                        String mCurrentDir_old = mCurrentDir;
                        mCurrentDir = path;
                        this.fullNode(ftn, newPath, depth+1);
                        mCurrentDir = mCurrentDir_old;
                    }
                } else {
                    if(videoPath(newPath))
                    	ftn.setIsFile();
                    else
                        continue;
                }

                n.addChild(ftn);
            }
            Collections.sort(n.children);
        }

        FileTreeNode up = new FileTreeNode("..");
        n.children.add(0, up);
    }
    
}
