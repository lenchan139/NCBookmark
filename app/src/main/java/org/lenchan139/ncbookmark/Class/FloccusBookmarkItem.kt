package org.lenchan139.ncbookmark.Class

import android.util.Log
import org.lenchan139.ncbookmark.Constants

class FloccusBookmarkItem(rawTagContent:String){
    val arrayTagsLevel = ArrayList<String>()
    init {
        Log.v("arrayTestFBItem1",rawTagContent)
        if(rawTagContent.startsWith(Constants.FLOCCUS_TAG_PREFIX) && rawTagContent.contains(">")){

             val tagsString = rawTagContent.substringAfter(Constants.FLOCCUS_TAG_PREFIX)
             val tags = rawTagContent.split(">")
             Log.v("arrayTestFBItem2",tags.toString())
             for(tag in tags){
                 if(tag.isNotEmpty())
                  arrayTagsLevel.add(tag)
             }

             Log.v("arrayTestFBItem3",arrayTagsLevel.toArray().contentToString())
         }else{


        }
    }
    fun get(i:Int):String{
        return arrayTagsLevel.get(i)
    }
    fun set(i:Int,str:String):ArrayList<String>{
        arrayTagsLevel.set(i,str)
        return arrayTagsLevel
    }
    fun count():Int{
        return arrayTagsLevel.size
    }
    fun getFullPath():String{
        return getFullWithParent(count()-1)
    }
    fun getFullWithParent(to:Int):String{
        if(arrayTagsLevel.size > 1){
            var result = arrayTagsLevel[0]
            for(i in 1..to){
                result += ">" + arrayTagsLevel.get(i)
            }
            return result
        }else{
            return ""
        }
    }
}