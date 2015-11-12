package com.py.player.ui;

import java.util.ArrayList;
import java.util.ListIterator;
import com.py.player.util.StringTools;

   public class FileTreeNode implements Comparable<FileTreeNode> {
        
    	public FileTreeNode parent;
        public ArrayList<FileTreeNode> children;
        public String fullName;
        public String showName;
        public Boolean isFile;

        public FileTreeNode(String fullName) {
            this(fullName, null);
        }

        public FileTreeNode(String fullName, String showName) {
            this.fullName = fullName;
            this.showName = showName;
            this.children = new ArrayList<FileTreeNode>();
            this.isFile = false;
            this.parent = null;
        }

        public void addChild(FileTreeNode fileNode) {
        	fileNode.parent = this;
            this.children.add(fileNode);
        }

        public FileTreeNode getChild(String dirName) {
            for(FileTreeNode fileNode : this.children) {
                if(fileNode.fullName.equals(dirName))
                    return fileNode;
            }
            FileTreeNode fn = new FileTreeNode(dirName);
            this.addChild(fn);
            return fn;
        }

        public Boolean isFile() {
            return this.isFile;
        }

        public void setIsFile() {
            this.isFile = true;
        }

        public Boolean existsChild(String fullName) {
            for(FileTreeNode fn : this.children) {
                if(StringTools.nullEquals(fn.fullName, fullName)) return true;
            }
            return false;
        }

        public int getChildPosition(FileTreeNode child){
            if(child == null)
                return -1;

            ListIterator<FileTreeNode> it = children.listIterator();
            while(it.hasNext()){
                FileTreeNode fileNode = it.next();
                if(child.equals(fileNode)) return it.previousIndex();
            }

            return -1;
        }

        public FileTreeNode ensureExists(String fullName) {
            for(FileTreeNode fileNode : this.children) {
                if(StringTools.nullEquals(fileNode.fullName, fullName)) return fileNode;
            }
            FileTreeNode fn = new FileTreeNode(fullName);
            this.children.add(fn);
            return fn;
        }

        public int subfolderCount() {
            int count = 0;
            for(FileTreeNode fn : this.children) {
                if(fn.isFile() == false && !fn.fullName.equals("..")) 
            	count++;
        }
        return count;
    }

    public int subfilesCount() {
        int count = 0;
        for(FileTreeNode fn : this.children) {
            if(fn.isFile() == true) count++;
        }
        return count;
    }

    public String getShowName() {
        return (this.showName != null) ? this.showName : this.fullName;
    }

    @Override
    public int compareTo(FileTreeNode arg0) {
        if(this.isFile && !(arg0.isFile))
            return 1;
        else if(!(this.isFile) && arg0.isFile)
            return -1;
        else
            return String.CASE_INSENSITIVE_ORDER.compare(this.fullName, arg0.fullName);
    }
    
}
   
  