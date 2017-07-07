package org.lenchan139.ncbookmark.v2

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONException
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.TagsItem
import org.lenchan139.ncbookmark.Constants
import org.lenchan139.ncbookmark.R

import java.io.IOException
import java.net.URL

class AddBookmarkActivityV2 : AppCompatActivity() {

    internal var urlNt: String? = null
    internal var username: String? = null
    internal var password: String? = null
    internal lateinit var tags: Array<String?>
    internal lateinit var login: String
    internal lateinit var edtUrl: EditText
    internal lateinit var edtTitle: EditText
    internal lateinit var edtDescr: EditText
    internal lateinit var edtTag: EditText
    internal lateinit var btnSelectTag: Button
    internal lateinit var btnSubmit: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bookmark_v2)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        edtDescr = findViewById(R.id.description) as EditText
        edtTag = findViewById(R.id.tag) as EditText
        edtTitle = findViewById(R.id.title) as EditText
        edtUrl = findViewById(R.id.url) as EditText
        btnSelectTag = findViewById(R.id.selectTag) as Button
        btnSubmit = findViewById(R.id.submit) as Button

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        fab.visibility = View.GONE
        //for out call use
        var inUrl = intent.getStringExtra("inUrl")
        var inTitle = intent.getStringExtra("inTitle")
        var inTag = intent.getStringExtra("inTag")
        //
        var sp = getSharedPreferences("data", 0)
        urlNt = sp.getString("url",null)
        username = sp.getString("username",null)
        password = sp.getString("password",null)
        var apiVer = sp.getInt("apiVersion",0)
        login = username + ":" + password
        if (urlNt != null && username != null && password != null && apiVer >= 2) {
            btnSubmit.setOnClickListener { AddBookmarkTask().execute() }
        } else {
            Toast.makeText(this,"maybe you are not login yet.",Toast.LENGTH_SHORT).show()
            finish()
        }

        if(inUrl!=null){
            edtUrl.setText(inUrl)
        }
        if(inTag!=null){
            edtTag.setText(inTag)
        }
        if(inTitle!=null){
            edtTitle.setText(inTitle)
        }



        btnSelectTag.setOnClickListener { fetchTagsTask().execute() }
    }

    private inner class fetchTagsTask : AsyncTask<URL, Int, Long>() {
        internal var urlSe = Constants.V2_API_ENDPOINT + "tag"
        internal val base64login = String(Base64.encode(login.toByteArray(), 0))
        internal lateinit var result: Document

        override fun doInBackground(vararg params: URL): Long? {

            try {
                result = Jsoup.connect(urlNt!! + urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.GET)
                        .execute().parse()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
        override fun onPostExecute(aLong: Long?) {
            Log.v("testAllTags", result.body().text())
            var err_check = true
            try {
                val jsonArray = JSONArray(result.body().text())
                tags = arrayOfNulls<String>(jsonArray.length())

                for (i in 0..jsonArray.length() - 1) {
                    tags[i] = jsonArray.get(i).toString()
                    Log.v("testTagConverting", jsonArray.get(i).toString())
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                err_check = false
            }

            if (err_check) {
                val dialog = AlertDialog.Builder(this@AddBookmarkActivityV2)
                dialog.setTitle("Exciting Tag")
                dialog.setItems(tags) { dialog, which -> edtTag.setText(tags!![which]) }
                dialog.setNegativeButton("Cancel", null)
                dialog.show()
            }
            super.onPostExecute(aLong)
        }

    }

    private inner class AddBookmarkTask : AsyncTask<URL, Int, Long>() {
        internal lateinit var tag: Array<String>
        internal var url: String? = null
        internal var title: String? = null
        internal var des: String? = null
        internal lateinit var result: Document
        internal var no_error = true
        internal var error_msg: String? = null
        internal var urlSe = Constants.V2_API_ENDPOINT + "bookmark"
        internal val base64login = String(Base64.encode(login.toByteArray(), 0))

        override fun onPreExecute() {
            val tampTag = TagsItem().strToArray(edtTag.text.toString())
            if(tampTag!= null){
                tag = tampTag
            }
            url = edtUrl.text.toString()
            title = edtTitle.text.toString()
            des = edtDescr.text.toString()
            if ( url == null || title == null) {
                no_error = false
                Toast.makeText(this@AddBookmarkActivityV2, "Please tag, url and title cannot be empty.", Toast.LENGTH_SHORT).show()
            }
            if (des == null) {
                des = ""
            }
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: URL): Long? {
            if (no_error) {

                try {
                    var jsoup = Jsoup.connect(urlNt!! + urlSe)
                            .ignoreContentType(true)
                            .header("Authorization", "Basic " + base64login)
                            .method(Connection.Method.POST)
                            .data("url", url)
                            .data("title", title)
                            .data("description", des)
                           for(i in 0..tag.size-1){
                               jsoup.data("item[tags][]", tag.get(i).trim())
                           }
                    result = jsoup.execute().parse()

                } catch (e: IOException) {
                    e.printStackTrace()
                    error_msg = e.message
                }

            }
            return null
        }

        override fun onPostExecute(aLong: Long?) {
            if (error_msg != null) {
                Toast.makeText(this@AddBookmarkActivityV2, error_msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AddBookmarkActivityV2, "Bookmark Added!", Toast.LENGTH_SHORT).show()
                this@AddBookmarkActivityV2.finish()
            }
            super.onPostExecute(aLong)
        }
    }
}
