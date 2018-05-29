package org.lenchan139.ncbookmark

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONException
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.lenchan139.ncbookmark.Class.BookmarkItem

import org.lenchan139.ncbookmark.v1.TagViewActivity
import org.lenchan139.ncbookmark.v2.TagListActivityV2
import java.io.IOException
import java.util.ArrayList

import java.util.Objects

class MainActivity : AppCompatActivity() {
    internal lateinit var edtUrl: EditText
    internal lateinit var edtUsername: EditText
    internal lateinit var edtPassword: EditText
    internal lateinit var btnSave: Button
    internal lateinit var sp: SharedPreferences
    internal lateinit var url: String
    internal lateinit var username: String
    internal lateinit var password: String
    internal lateinit var spinner : Spinner
    internal var urlSe = Constants.V2_API_ENDPOINT + "tag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp = getSharedPreferences("data", 0)

        edtUrl = findViewById<EditText>(R.id.url)
        edtUsername = findViewById<EditText>(R.id.username)
        edtPassword = findViewById<EditText>(R.id.password)
        btnSave = findViewById<Button>(R.id.save)

        spinner = findViewById<Spinner>(R.id.spinnerApi)
        val apiString = arrayOf("v2", "v1")
        val list1 = ArrayAdapter(this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                apiString)
        spinner.adapter = list1


        url = sp.getString("url", "")
        username = sp.getString("username", "")
        password = sp.getString("password", "")
        if (url != "" && username != "" && password != "") {
            startActivity(startBookmarkView(this, url, username, password, spinner))
            finish()
        } else {

            btnSave.setOnClickListener {
                //Toast.makeText(MainActivity.this, spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

                url = edtUrl.text.toString()
                username = edtUsername.text.toString()
                password = edtPassword.text.toString()
            }
        }

    }

    fun startBookmarkView(activity: Activity, url: String, username: String, password: String, spinner: Spinner): Intent? {
        if (spinner.selectedItem.toString().contains("v1")) {
            val intent = Intent(activity, TagViewActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("username", username)
            intent.putExtra("password", password)
            sp.edit().putInt("apiVersion", 1).commit()
            return intent
        } else if (spinner.selectedItem.toString().contains("v2")) {
            val intent = Intent(activity, TagListActivityV2::class.java)
            intent.putExtra("url", url)
            intent.putExtra("username", username)
            intent.putExtra("password", password)
            sp.edit().putInt("apiVersion", 2).commit()
            return intent
        } else {
            return null
        }

    }

    internal inner class DlTask : AsyncTask<String, Int, Int>() {
        var result: Document? = null
        val login = username + ":" + password
        var isConnected = false
        val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): Int? {
            try {
                result = Jsoup.connect(url!! + urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.GET)
                        .execute().parse()
                isConnected = true
            } catch (e: IOException) {
                e.printStackTrace()
                isConnected = false

            }catch(e:IllegalArgumentException){

                e.printStackTrace()
                isConnected = false
            }

            return null
        }

        override fun onPostExecute(integer: Int?) {
            if (url.length > 0 && username.length > 0 && password.length > 0 && false) {
                Toast.makeText(this@MainActivity, "Saved.", Toast.LENGTH_SHORT).show()
                sp.edit().putString("url", url).commit()
                sp.edit().putString("username", username).commit()
                sp.edit().putString("password", password).commit()
                startActivity(startBookmarkView(this@MainActivity, url, username, password, spinner))
                finish()
            }
            super.onPostExecute(integer)
        }
    }

}
