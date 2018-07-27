package org.lenchan139.ncbookmark.Class

import org.json.JSONArray

/**
 * Created by len on 28/6/2017.
 */

class BookmarkItemV2{

    var title: String? = null
    var url: String? = null
    var tags: JSONArray? = null
    var id: Int = 0

    override fun toString(): String {
        return "$url|$title|$tags"
    }
    fun hasTag(tag:String): Boolean {
        if(tags != null){
            for(o in 0..tags!!.length()-1){
                if(tag.equals(tags!![o])){
                    return true
                }
            }
        }
        return false
    }
}
