package org.lenchan139.ncbookmark.v2

import android.content.DialogInterface
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
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.TagsItem
import org.lenchan139.ncbookmark.R

import java.io.IOException
import java.net.URL

class EditBookmarkActivityV2 : AppCompatActivity() {

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
    internal lateinit var toolbar: Toolbar
    internal var bookmarkId = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_bookmark_v2)
        toolbar = findViewById(R.id.toolbar) as Toolbar
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
        urlNt = intent.getStringExtra("url")
        username = intent.getStringExtra("username")
        password = intent.getStringExtra("password")
        bookmarkId = intent.getIntExtra("id", -1)
        login = username + ":" + password
        if (urlNt != null && username != null && password != null && bookmarkId != -1) {
            btnSubmit.setOnClickListener { EditBookmarkTask().execute() }
        } else {

        }

        btnSelectTag.setOnClickListener { fetchTagsTask().execute() }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        ShowBookmarkTask().execute()

    }

    private inner class EditBookmarkTask : AsyncTask<URL, Int, Long>() {
        internal var rid = bookmarkId
        internal lateinit var tag: Array<String>
        internal var url: String? = null
        internal var title: String? = null
        internal var des: String? = null
        internal lateinit var result: Document
        internal var no_error = true
        internal var error_msg: String? = null
        internal var urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark/" + rid
        internal val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun onPreExecute() {
            val tempTag = TagsItem().strToArray(edtTag.text.toString())
            if(tempTag !=null){
                tag = tempTag
            }
            url = edtUrl.text.toString()
            title = edtTitle.text.toString()
            des = edtDescr.text.toString()
            if (url == null || title == null) {
                no_error = false
                Toast.makeText(this@EditBookmarkActivityV2, "Please, url and title cannot be empty.", Toast.LENGTH_SHORT).show()
            }
            if (des == null) {
                des = ""
            }
            super.onPreExecute()
        }


        override fun doInBackground(vararg params: URL): Long? {

            try {
                var jsoup = Jsoup.connect(urlNt!! + urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.PUT)
                        .data("url", url)
                        .data("record_id", rid.toString())
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

            return null
        }

        override fun onPostExecute(aLong: Long?) {

            if (error_msg != null) {
                Toast.makeText(this@EditBookmarkActivityV2, error_msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@EditBookmarkActivityV2, "Bookmark Edited!", Toast.LENGTH_SHORT).show()
                this@EditBookmarkActivityV2.finish()
            }
            super.onPostExecute(aLong)
        }
    }

    private inner class fetchTagsTask : AsyncTask<URL, Int, Long>() {
        internal var urlSe = "/index.php/apps/bookmarks/public/rest/v2/tag"
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
                val dialog = AlertDialog.Builder(this@EditBookmarkActivityV2)
                dialog.setTitle("Exciting Tag")
                dialog.setItems(tags) { dialog, which -> edtTag.setText(tags!![which]) }
                dialog.setNegativeButton("Cancel", null)
                dialog.show()
            }
            super.onPostExecute(aLong)
        }

    }

    private inner class ShowBookmarkTask : AsyncTask<URL, Int, Long>() {

        internal var rid = bookmarkId
        internal lateinit var result: Document
        internal var no_error = true
        internal var error_msg: String? = null
        internal var urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark?page=-1"
        internal val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun onPreExecute() {
            super.onPreExecute()
        }

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

            Log.v("testLog", result.body().text())
            return null
        }

        override fun onPostExecute(aLong: Long?) {

            var a: JSONObject? = null
            var finalObject: JSONObject? = null
            try {
                a = JSONObject(result.body().text())
                val cast = a.getJSONArray("data")
                for (i in 0..cast.length() - 1) {
                    if (cast.getJSONObject(i).getInt("id") == rid) {
                        finalObject = cast.getJSONObject(i)
                        edtDescr.setText(finalObject!!.getString("description"))
                        edtTag.setText(TagsItem().jsonArrayToString(finalObject.getJSONArray("tags")))
                        edtTitle.setText(finalObject.getString("title"))
                        edtUrl.setText(finalObject.getString("url"))

                        toolbar.title = finalObject.getString("title")

                    } else {
                        finalObject = null
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(this@EditBookmarkActivityV2, "Network Problems", Toast.LENGTH_SHORT).show()
                finish()
            }

            super.onPostExecute(aLong)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
