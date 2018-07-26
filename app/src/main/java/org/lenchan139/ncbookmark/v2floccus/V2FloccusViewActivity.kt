package org.lenchan139.ncbookmark.v2floccus

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import org.lenchan139.ncbookmark.R

import kotlinx.android.synthetic.main.activity_v2_floccus_view.*
import org.json.JSONArray
import org.json.JSONException
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.BookmarkItem
import org.lenchan139.ncbookmark.Class.FloccusHelper
import org.lenchan139.ncbookmark.Constants
import org.lenchan139.ncbookmark.MainActivity
import java.io.IOException
import java.util.ArrayList

class V2FloccusViewActivity : AppCompatActivity() {

    internal var urlNt: String? = null
    internal var username: String? = null
    internal var password: String? = null
    internal lateinit var login: String
    internal var urlSe = Constants.V2_API_ENDPOINT + "tag"
    internal var arrayFloccusHelper = FloccusHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_v2_floccus_view)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        urlNt = intent.getStringExtra("url")
        username = intent.getStringExtra("username")
        password = intent.getStringExtra("password")

        login = username + ":" + password

        if (urlNt != null && username != null && password != null) {
            DlTask().execute()
        } else {
            Toast.makeText(this, "Plase login first!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    internal inner class DlTask : AsyncTask<String, Int, Int>() {
        var result: Document? = null

        val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): Int? {
            try {
                result = Jsoup.connect(urlNt!! + urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.GET)
                        .execute().parse()
            } catch (e: IOException) {
                e.printStackTrace()

            }catch(e:IllegalArgumentException){

            }

            return null
        }

        override fun onPostExecute(integer: Int?) {
            if (result != null) {
                Log.v("testLog", result!!.body().text())

                val jsonList = ArrayList<BookmarkItem>()
                try {
                    val listTags = ArrayList<String>()
                    var jsonArray = JSONArray(result!!.body().text())
                    listTags.add("!ungrouped")
                    for (i in 0..jsonArray.length()-1){
                        listTags.add(jsonArray.getString(i))
                    }
                    Log.v("listArray", listTags.toString())

                    //convert taglist to String[]
                    val strTags = ArrayList<String>()
                    for (i in listTags.indices) {
                        var temp = listTags[i]
                        temp = temp.replace("[\"", "").replace("\"]", "")
                        if(temp.startsWith(Constants.FLOCCUS_TAG_PREFIX)){
                            arrayFloccusHelper.addFromString(temp)
                        }

                    }
                    if(arrayFloccusHelper.bookmarks.isNotEmpty()){
                        //start print to UI
                        Log.v("arrayTestV2Floccus1",arrayFloccusHelper.bookmarks.get(0).getFullPath())
                        val listviewTags = findViewById<ListView>(R.id.listview)
                        listviewTags.visibility = View.VISIBLE
                        val listAdptTags = ArrayAdapter<String>(this@V2FloccusViewActivity, android.R.layout.simple_list_item_1, android.R.id.text1, arrayFloccusHelper.getPossibleLowerPath())
                        listviewTags.adapter = listAdptTags
                        Log.v("array",arrayFloccusHelper.getPossibleLowerPath().toArray().contentToString())
                        listviewTags.onItemClickListener = AdapterView.OnItemClickListener {
                            parent, view, position, id ->
                            //openBookmarkView(strTags[position]!!)
                            
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()

                }

            }
            super.onPostExecute(integer)
        }
    }

}
