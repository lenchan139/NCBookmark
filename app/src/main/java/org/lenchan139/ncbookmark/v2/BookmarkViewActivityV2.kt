package org.lenchan139.ncbookmark.v2

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.BookmarkItemV2
import org.lenchan139.ncbookmark.Constants
import org.lenchan139.ncbookmark.R

import java.io.IOException
import java.net.URL
import java.util.ArrayList

class BookmarkViewActivityV2 : AppCompatActivity() {
    internal lateinit var jsonUrl: String
    internal lateinit var gloTag: String
    internal var deleteId = -1
    internal lateinit var login: String
    internal lateinit var urlNt: String

    override fun onResume() {

        HandleJsonTask().execute()
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_view)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        var url: String? = intent.getStringExtra("url")
        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        if(url != null) {
            urlNt = url
        }
        login = username + ":" + password
        var tag: String? = intent.getStringExtra("tags")
        if (tag == null) {
            Toast.makeText(this, "DONT OPEN THIS DIRECTRY!", Toast.LENGTH_SHORT).show()
            finish()

        }
        toolbar.title = tag

        setSupportActionBar(toolbar)
        if (tag == "!ungrouped") {
            tag = ""
        }
        if (url != null && url.lastIndexOf("/") != url.length - 1) {
            url = url + "/"
        }
        if (url != null && username != null && password != null) {
            jsonUrl = url + Constants.V2_API_ENDPOINT + "bookmark?sortby=tags&page=-1"
            Log.v("jsonUrl", jsonUrl)
        }
        gloTag = tag!!

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        HandleJsonTask().execute()

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
    }

    private inner class HandleJsonTask : AsyncTask<URL, Int, Long>() {
        internal lateinit var jsonText: String

        internal val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun doInBackground(vararg urls: URL): Long? {
            if (gloTag == "") {
            } else {
                jsonUrl = jsonUrl + "&tags[]=" + gloTag
            }
            try {
                jsonText = Jsoup.connect(jsonUrl)
                        .header("Authorization", "Basic " + base64login)
                        .ignoreContentType(true)
                        .get().body().text()
                Log.v("jsonText", jsonText)
            } catch (e: IOException) {
                e.printStackTrace()
                //Toast.makeText(TagViewActivity.this, "Check your network connection!", Toast.LENGTH_SHORT).show();
            }

            return java.lang.Long.valueOf(0)
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: Long?) {
            val jsonList = ArrayList<BookmarkItemV2>()
            try {
                //JSONObject jsonResponse = new JSONObject(jsonText);

                //raw to raw list

                val a = JSONObject(jsonText)
                val cast = a.getJSONArray("data")
                if (gloTag == "") {

                    for (i in 0..cast.length() - 1) {
                        val tempJ = cast.getJSONObject(i)
                        if (tempJ.getString("tags") == "[\"\"]") {
                            val tempBi = BookmarkItemV2()
                            tempBi.url = tempJ.getString("url")
                            tempBi.tags = tempJ.getJSONArray("tags")
                            tempBi.title = tempJ.getString("title")
                            tempBi.id = tempJ.getInt("id")
                            jsonList.add(tempBi)
                            Log.v("currJSONObj", tempBi.toString())
                        }
                    }
                } else {
                    for (i in 0..cast.length() - 1) {
                        val tempJ = cast.getJSONObject(i)
                        val tempBi = BookmarkItemV2()
                        tempBi.url = tempJ.getString("url")
                        tempBi.tags = tempJ.getJSONArray("tags")
                        tempBi.title = tempJ.getString("title")
                        tempBi.id = tempJ.getInt("id")
                        jsonList.add(tempBi)
                        Log.v("currJSONObj", tempBi.toString())
                    }
                }
                //convert taglist to String[]
                val strTags = arrayOfNulls<String>(jsonList.size)
                for (i in jsonList.indices) {
                    var temp = jsonList[i].title
                    temp = temp!!.replace("[\"", "").replace("\"]", "")
                    strTags[i] = temp

                }

                //start print to UI
                val listviewTags = findViewById(R.id.tagList) as ListView
                listviewTags.visibility = View.VISIBLE
                val listAdptTags = ArrayAdapter<String>(this@BookmarkViewActivityV2, android.R.layout.simple_list_item_1, android.R.id.text1, strTags)
                listviewTags.adapter = listAdptTags

                listviewTags.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    val dialog = AlertDialog.Builder(this@BookmarkViewActivityV2)
                    val strOptions = arrayOf("Open", "Edit", "Delete")
                    dialog.setTitle(jsonList[position].title)
                    dialog.setItems(strOptions) { dialog, which ->
                        if (which == 0) {
                            //open
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(jsonList[position].url))
                            startActivity(intent)
                        } else if (which == 1) {
                            //edit
                            //Toast.makeText(BookmarkViewActivityV2.this, "Function not yet.", Toast.LENGTH_SHORT).show();
                            val intent = Intent(this@BookmarkViewActivityV2, EditBookmarkActivityV2::class.java)
                            intent.putExtra("username", getIntent().getStringExtra("username"))
                            intent.putExtra("password", getIntent().getStringExtra("password"))
                            intent.putExtra("url", getIntent().getStringExtra("url"))
                            intent.putExtra("id", jsonList[position].id)
                            startActivity(intent)
                        } else if (which == 2) {
                            //delete
                            val dialogD = AlertDialog.Builder(this@BookmarkViewActivityV2)
                            dialogD.setMessage("Delete This:" + jsonList[position].title +
                                    "(Url: " + jsonList[position].url + ") ?")
                            dialogD.setTitle("Delete Action")
                            dialogD.setNegativeButton("No") { dialog, which -> }
                            dialogD.setPositiveButton("Yes, Sure.") { dialog, which ->
                                deleteId = jsonList[position].id
                                DeleteTask().execute()
                            }
                            dialogD.show()
                        }
                    }
                    dialog.show()
                }
                listviewTags.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id -> false }
            } catch (e: JSONException) {
                e.printStackTrace()

            }

        }
    }

    private inner class DeleteTask : AsyncTask<URL, Int, Long>() {
        internal lateinit var result: Document
        internal var urlSe = Constants.V2_API_ENDPOINT + "bookmark/" + deleteId
        internal val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun doInBackground(vararg params: URL): Long? {
            if (deleteId != -1) {
                try {
                    result = Jsoup.connect(urlNt + urlSe)
                            .ignoreContentType(true)
                            .header("Authorization", "Basic " + base64login)
                            .method(Connection.Method.DELETE)
                            .execute().parse()
                    deleteId = -3
                } catch (e: IOException) {
                    e.printStackTrace()
                    deleteId = -9
                }

            }
            return null
        }

        override fun onPostExecute(aLong: Long?) {
            if (deleteId == -1) {

            } else if (deleteId == -3) {
                Toast.makeText(this@BookmarkViewActivityV2, "Deletion Done.", Toast.LENGTH_SHORT).show()
                val intent = this@BookmarkViewActivityV2.intent
                finish()
                startActivity(intent)
            } else if (deleteId == -9) {
                Toast.makeText(this@BookmarkViewActivityV2, "Something was wrong when deletion.", Toast.LENGTH_SHORT).show()
            }
            deleteId = -1
            super.onPostExecute(aLong)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
