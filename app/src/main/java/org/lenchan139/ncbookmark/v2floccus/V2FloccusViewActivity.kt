package org.lenchan139.ncbookmark.v2floccus

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import org.lenchan139.ncbookmark.R

import kotlinx.android.synthetic.main.activity_v2_floccus_view.*
import kotlinx.android.synthetic.main.content_v2_floccus_view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.*
import org.lenchan139.ncbookmark.Constants
import org.lenchan139.ncbookmark.MainActivity
import org.lenchan139.ncbookmark.v2.AddBookmarkActivityV2
import org.lenchan139.ncbookmark.v2.EditBookmarkActivityV2
import java.io.IOException
import java.net.URL
import java.util.ArrayList

class V2FloccusViewActivity : AppCompatActivity() {

    internal var urlNt: String? = null
    internal var username: String? = null
    internal var password: String? = null
    internal lateinit var login: String
    internal var urlSe = Constants.V2_API_ENDPOINT + "tag"
    internal var arrayFloccusHelper = FloccusHelper()
    var isInit = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_v2_floccus_view)
        setSupportActionBar(toolbar)
        val window = getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fab.setOnClickListener { view ->
            openAddBookmarkActivity()
        }
        urlNt = intent.getStringExtra("url")
        username = intent.getStringExtra("username")
        password = intent.getStringExtra("password")

        login = username + ":" + password

        if (urlNt != null && username != null && password != null) {
            DlTagTask().execute()
        } else {
            Toast.makeText(this, "Plase login first!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    override fun onPostResume() {
        if(isInit) {
            updateTagsGUI()
        }
        super.onPostResume()
    }

    override fun onBackPressed() {
        if(arrayFloccusHelper.canBack()){
            if(arrayFloccusHelper.goBack()){
                updateTagsGUI()
            }else{

            }
        }else{

        }
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
            openAddBookmarkActivity()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun initBackButton(){
        if(arrayFloccusHelper.canBack()){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }else{
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    internal inner class DlTagTask : AsyncTask<String, Int, Int>() {
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
                val arrListviewOnClick = ArrayList<FloccusListviewItem>()
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
                            //arrListviewOnClick.add(FloccusListviewItem(FloccusListviewItemConst().ITEM_TYPE_TAG, arrayFloccusHelper.getPossibleLowerPath().get(arrayFloccusHelper.getPossibleLowerPath().lastIndex)))
                        }

                    }
                    if(arrayFloccusHelper.bookmarks.isNotEmpty()){
                       updateTagsGUI()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()

                }

            }
            super.onPostExecute(integer)
        }

    }

    fun updateTagsGUI(){
        supportActionBar?.title = arrayFloccusHelper.currentPath
        progressBar.visibility = View.VISIBLE
        val arrListviewOnClick = ArrayList<FloccusListviewItem>()
        //start print to UI
        for(obj in arrayFloccusHelper.getPossibleLowerPath()) {
            if (obj.count() > 0) {
                val tagss = FloccusListviewItem(FloccusListviewItemConst().ITEM_TYPE_TAG, obj,null)
                arrListviewOnClick.add(tagss)
            }
        }
        Log.v("arrayTestV2Floccus1",arrayFloccusHelper.bookmarks.get(0).getFullPath())
        DlBookmarkTask(arrListviewOnClick).execute()
    }
    inner class DlBookmarkTask(var arrListviewOnClick:ArrayList<FloccusListviewItem>):AsyncTask<String,Int,Int>(){
        var tag = arrayFloccusHelper.currentPath
        val jsonUrl = urlNt + Constants.V2_API_ENDPOINT + "bookmark?sortby=tags&page=-1"+ "&tags[]=" + tag
        internal lateinit var jsonText: String
        internal val base64login = String(Base64.encode(login.toByteArray(), 0))


        override fun doInBackground(vararg params: String?): Int {
            Log.v("jsonTextCurr", jsonUrl)
            try {
                jsonText = Jsoup.connect(jsonUrl)
                        .header("Authorization", "Basic " + base64login)
                        .ignoreContentType(true)
                        .get().body().text()
                Log.v("jsonText", tag + "|" + jsonText)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@V2FloccusViewActivity, "Check your network connection!", Toast.LENGTH_SHORT).show();
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            //formating json object

            val jsonList = ArrayList<BookmarkItemV2>()
            val a = JSONObject(jsonText)
            val cast = a.getJSONArray("data")
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
            val displayArrayList = arrayFloccusHelper.getPossibleLowerPath()
            for(i in 0..displayArrayList.lastIndex){
                displayArrayList.set(i,"\uD83D\uDCC1 " + displayArrayList.get(i))
            }
            for(obj in jsonList){

                if(obj.hasTag(arrayFloccusHelper.currentPath)) {
                    arrListviewOnClick.add(FloccusListviewItem(FloccusListviewItemConst().ITEM_TYPE_BOOKMARK, obj.url!!, obj))
                    displayArrayList.add("\uD83D\uDD16 " + obj.title!!)
                }
            }


            //start update listview
            val listviewTags = findViewById<ListView>(R.id.listview)
            listviewTags.visibility = View.VISIBLE
            val listAdptTags = ArrayAdapter<String>(this@V2FloccusViewActivity, android.R.layout.simple_list_item_1, android.R.id.text1, displayArrayList)
            listviewTags.adapter = listAdptTags
            Log.v("array",arrayFloccusHelper.getPossibleLowerPath().toArray().contentToString())

            listviewTags.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
                //openBookmarkView(strTags[position]!!)

                val onClick = arrListviewOnClick.get(position)
                if(onClick.type==FloccusListviewItemConst().ITEM_TYPE_TAG){
                    if(arrayFloccusHelper.enterNextUseTag(onClick.uri)){
                        Log.v("theTestUri1",onClick.uri)
                        updateTagsGUI()
                    }else{

                    }
                }else if(onClick.type==FloccusListviewItemConst().ITEM_TYPE_BOOKMARK){
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(onClick.uri))
                    startActivity(intent)
                }

            }
            listviewTags.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id -> true
                val dialog = AlertDialog.Builder(this@V2FloccusViewActivity)
                val strOptions = arrayOf("Open", "Edit", "Delete")

                val bookmarkObj = arrListviewOnClick.get(position).bookmarkItem
                if(bookmarkObj != null) {
                    dialog.setTitle(bookmarkObj.title)
                    dialog.setItems(strOptions) { boolean1, which ->
                        if (which == 0) {
                            //open
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bookmarkObj.url))
                            startActivity(intent)
                        } else if (which == 1) {
                            //edit
                            //Toast.makeText(BookmarkViewActivityV2.this, "Function not yet.", Toast.LENGTH_SHORT).show();
                            val intent = Intent(this@V2FloccusViewActivity, EditBookmarkActivityV2::class.java)
                            intent.putExtra("username", getIntent().getStringExtra("username"))
                            intent.putExtra("password", getIntent().getStringExtra("password"))
                            intent.putExtra("url", getIntent().getStringExtra("url"))
                            intent.putExtra("id", bookmarkObj.id)
                            startActivity(intent)
                        } else if (which == 2) {
                            //delete
                            val dialogD = AlertDialog.Builder(this@V2FloccusViewActivity)
                            dialogD.setMessage("Delete This:" + bookmarkObj.title +
                                    "(Url: " + bookmarkObj.url + ") ?")
                            dialogD.setTitle("Delete Action")
                            dialogD.setNegativeButton("No") { dialog, which -> }
                            dialogD.setPositiveButton("Yes, Sure.") { dialog, which ->
                                val deleteTask = DeleteTask(bookmarkObj.id)
                                deleteTask.execute()
                            }
                            dialogD.show()
                        }
                    }
                    dialog.show()
                }else{
                    // it is not bookmark
                }
                true
            }

            progressBar.visibility = View.GONE
            initBackButton()

            isInit = true
            super.onPostExecute(result)
        }
    }

    private inner class DeleteTask(var deleteId:Int) : AsyncTask<URL, Int, Long>() {
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
                Toast.makeText(this@V2FloccusViewActivity, "Deletion Done.", Toast.LENGTH_SHORT).show()
                val intent = this@V2FloccusViewActivity.intent
                finish()
                startActivity(intent)
            } else if (deleteId == -9) {
                Toast.makeText(this@V2FloccusViewActivity, "Something was wrong when deletion.", Toast.LENGTH_SHORT).show()
            }
            deleteId = -1
            super.onPostExecute(aLong)
        }
    }
    fun openAddBookmarkActivity(){
        val intent = Intent(this, AddBookmarkActivityV2::class.java)
        intent.putExtra("username", getIntent().getStringExtra("username"))
        intent.putExtra("password", getIntent().getStringExtra("password"))
        intent.putExtra("url", getIntent().getStringExtra("url"))
        intent.putExtra("inTag",arrayFloccusHelper.currentPath)
        startActivity(intent)
    }
}
