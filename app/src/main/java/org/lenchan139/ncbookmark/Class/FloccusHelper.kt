package org.lenchan139.ncbookmark.Class

import android.provider.SyncStateContract
import android.util.Log
import org.lenchan139.ncbookmark.Constants

class FloccusHelper{
    var bookmarks = ArrayList<FloccusBookmarkItem>()
    var currentPathLevel = 0
    var currentPath = Constants.FLOCCUS_TAG_PREFIX
    var currentBookmarksSet = 0
   fun add(floccusBookmark:FloccusBookmarkItem): ArrayList<FloccusBookmarkItem> {
       bookmarks.add(floccusBookmark)
       return bookmarks
   }
    fun addFromString(str:String):ArrayList<FloccusBookmarkItem>{
        Log.v("arrayTestFHObj0",str)
        val obj = FloccusBookmarkItem(str)
        Log.v("arrayTestFHObj1", obj.count().toString())
        bookmarks.add(obj)
        return bookmarks
    }
    fun canBack():Boolean{
        return (currentPath.contains(">")) || (currentPathLevel>0)
    }
    fun goBack():Boolean{
        if(currentPath.contains(">") || currentPathLevel<=0){
            val curr = currentPath.split(">").toTypedArray()
            curr[curr.lastIndex] = ""
            var newPath = curr[0]
            for(i in 1..curr.lastIndex-1){
                newPath += ">" + curr[i]
            }
            currentPath = newPath
            currentPathLevel -= 1
            return true
        }else{
            return false
        }
    }
    fun enterNextUseTag(nextTag:String):Boolean{
        val nextPath = currentPath + ">" + nextTag
        for(obj in bookmarks){
            if(obj.getFullPath().contains(nextPath)){
                currentPath = nextPath
                currentPathLevel += 1
                return true
            }
        }
        return false
    }
    fun getPossibleLowerPath():ArrayList<String>{
        val arraylist = ArrayList<String>()
        val nextIndex = currentPathLevel + 1
        Log.v("arrayTestFH0",bookmarks[0].arrayTagsLevel.size.toString())
        for(i in bookmarks.indices){
            Log.v("arrayTestFH1",i.toString())
            val path = currentPath
            Log.v("arrayTestFH2Path",path)
            val arr = bookmarks[i].arrayTagsLevel
            Log.v("arrayTestFH3",bookmarks[i].getFullPath())
            if (bookmarks[i].getFullPath().startsWith(currentPath) && arr.size > 0){
                Log.v("arrayTest", arr.toString())
                if(arr.size > nextIndex) {
                    if (!arraylist.contains(arr[nextIndex])) {
                        arraylist.add(arr[nextIndex])
                    }
                }
            }
        }
        Log.v("currentLevel",arraylist.toArray().contentToString())
        return arraylist
    }
}