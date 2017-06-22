package org.lenchan139.ncbookmark.v2

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.BookmarkItem
import org.lenchan139.ncbookmark.MainActivity
import org.lenchan139.ncbookmark.R
import org.lenchan139.ncbookmark.v1.BookmarkViewActivity
import org.lenchan139.ncbookmark.v1.TagViewActivity

import java.io.IOException
import java.util.ArrayList
import java.util.Objects

class TagListActivityV2 : AppCompatActivity() {
    internal var urlNt: String? = null
    internal var username: String? = null
    internal var password: String? = null
    internal lateinit var login: String
    internal var urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark?page=-1"

    override fun onResume() {
        DlTask().execute()
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag_list_v2)
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

            }

            return null
        }

        override fun onPostExecute(integer: Int?) {
            if (result != null) {
                Log.v("testLog", result!!.body().text())

                val jsonList = ArrayList<BookmarkItem>()
                try {
                    //JSONObject jsonResponse = new JSONObject(jsonText);

                    //raw to raw list
                    val a = JSONObject(result!!.body().text())
                    val cast = a.getJSONArray("data")
                    for (i in 0..cast.length() - 1) {
                        val tempJ = cast.getJSONObject(i)
                        val tempBi = BookmarkItem()
                        tempBi.url = tempJ.getString("url")
                        tempBi.tags = tempJ.getString("tags")
                        tempBi.title = tempJ.getString("title")
                        jsonList.add(tempBi)
                        Log.v("currJSONObj", tempBi.toString())
                    }
                    //get all tags
                    val listTags = ArrayList<String>()
                    for (i1 in jsonList.indices) {
                        val tempTags = jsonList[i1].tags
                        var isHad = false
                        //check tag if had.
                        for (i2 in listTags.indices) {
                            if (tempTags == listTags[i2]) {
                                isHad = true
                            }
                        }
                        //if not had, add it.
                        if (!isHad) {
                            listTags.add(tempTags!!)
                        }
                    }
                    Log.v("listArray", listTags.toString())

                    //convert taglist to String[]
                    val strTags = arrayOfNulls<String>(listTags.size)
                    for (i in listTags.indices) {
                        var temp = listTags[i]
                        temp = temp.replace("[\"", "").replace("\"]", "")
                        if (temp == "") {
                            temp = "!ungrouped"
                        }
                        strTags[i] = temp

                    }

                    //start print to UI
                    val listviewTags = findViewById(R.id.tagList) as ListView
                    listviewTags.visibility = View.VISIBLE
                    val listAdptTags = ArrayAdapter<String>(this@TagListActivityV2, android.R.layout.simple_list_item_1, android.R.id.text1, strTags)
                    listviewTags.adapter = listAdptTags

                    listviewTags.onItemClickListener = AdapterView.OnItemClickListener {
                        parent, view, position, id -> openBookmarkView(strTags[position]!!) }
                } catch (e: JSONException) {
                    e.printStackTrace()

                }

            }
            super.onPostExecute(integer)
        }
    }

    private fun openBookmarkView(tag: String) {
        val intent = Intent(this, BookmarkViewActivityV2::class.java)
        intent.putExtra("username", getIntent().getStringExtra("username"))
        intent.putExtra("password", getIntent().getStringExtra("password"))
        intent.putExtra("url", getIntent().getStringExtra("url"))
        intent.putExtra("tags", tag)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.bookmark_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_reset) {
            val sp = getSharedPreferences("data", 0)
            sp.edit().clear().commit()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        } else if (id == R.id.add_bookmark) {
            val intent = Intent(this, AddBookmarkActivityV2::class.java)
            intent.putExtra("username", getIntent().getStringExtra("username"))
            intent.putExtra("password", getIntent().getStringExtra("password"))
            intent.putExtra("url", getIntent().getStringExtra("url"))
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
