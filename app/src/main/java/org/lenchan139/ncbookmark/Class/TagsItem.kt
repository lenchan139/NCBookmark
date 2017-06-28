package org.lenchan139.ncbookmark.Class

import org.json.JSONArray

/**
 * Created by len on 28/6/2017.
 */
class TagsItem{
    fun jsonArrayToString(jsonArray: JSONArray): String?{

        if(jsonArray.length() == 1){
            return jsonArray.getString(0)
        }else if (jsonArray.length() >= 2){
            var array : ArrayList<String>
            var str : String
            str = jsonArray.getString(0)
            for(i in 1..jsonArray.length()-1){
                str = str + "," + jsonArray.getString(i)
            }
            return  str
        }
        return null
    }

    fun strToArray(str: String): Array<String>? {
        if(str.indexOf(",")<=0){
            return arrayOf(str)
        }else if(str.indexOf(",")>=1){
            return str.split(",").toTypedArray()

        }
        return null
    }
}