package org.lenchan139.ncbookmark

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import org.lenchan139.ncbookmark.v1.TagViewActivity
import org.lenchan139.ncbookmark.v2.TagListActivityV2
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {
    internal lateinit var edtUrl: EditText
    internal lateinit var edtUsername: EditText
    internal lateinit var edtPassword: EditText
    internal lateinit var btnSave: Button
    internal lateinit var sp: SharedPreferences
    internal lateinit var url: String
    internal lateinit var username: String
    internal lateinit var password: String
    internal lateinit var spinnerApi : Spinner
    internal lateinit var spinnerUrlPrefix : Spinner
    val apiString = arrayOf("v2", "v1")
    val urlPrefixArray = arrayOf("https://","http://")
    internal var urlSe = Constants.V2_API_ENDPOINT + "tag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp = getSharedPreferences("data", 0)

        edtUrl = findViewById<EditText>(R.id.url)
        edtUsername = findViewById<EditText>(R.id.username)
        edtPassword = findViewById<EditText>(R.id.password)
        btnSave = findViewById<Button>(R.id.save)

        spinnerApi = findViewById<Spinner>(R.id.spinnerApi)
        spinnerUrlPrefix = findViewById<Spinner>(R.id.spinnerUrl)
        val listAdapterApi = ArrayAdapter(this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                apiString)
        val listAdapterUrl = ArrayAdapter(this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                urlPrefixArray)
        spinnerApi.adapter = listAdapterApi
        spinnerUrlPrefix.adapter = listAdapterUrl


        url = sp.getString("url", "")
        username = sp.getString("username", "")
        password = sp.getString("password", "")
        if (url != "" && username != "" && password != "") {
            startActivity(startBookmarkView(this, url, username, password, spinnerApi))
            finish()
        } else {

            btnSave.setOnClickListener {
                //Toast.makeText(MainActivity.this, spinnerApi.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                Log.v("urlHere",url)
                url = edtUrl.text.toString()
                if(url.startsWith("http")){
                    url.replace("https://","").replace("http://","")
                }
                if(url.lastIndexOf("/") == url.length-1){
                    url += '/'
                }
                url = urlPrefixArray.get(spinnerUrlPrefix.selectedItemPosition) + edtUrl.text.toString()
                username = edtUsername.text.toString()
                password = edtPassword.text.toString()
                DlTask().execute()

            }
        }

    }

    fun startBookmarkView(activity: Activity, url: String, username: String, password: String, spinner: Spinner): Intent? {
        btnSave.setEnabled(false)
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
        var result: String? = null
        val login = username + ":" + password
        var isConnected = false
        var isValid = false
        var errorMessage : String? = null
        val base64login = String(Base64.encode(login.toByteArray(), 0))
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): Int? {
            Log.v("requestUrl",url+ urlSe)
            try {
                val r = Jsoup.connect(url+ urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.GET)
                        .timeout(30*1000)
                        .execute().parse()
                isConnected = true
                if(r != null){
                    result = r.body().text()
                    isValid = true
                }
                Log.v("JSON_FOR_VALIDATE_ACC",r.toString())
                //val json = JSON
                isConnected = true
            }catch (e: HttpStatusException) {
                e.printStackTrace()
                errorMessage = "It seems url, username or password is invalid."
            }catch(e:IllegalArgumentException){
                    e.printStackTrace()
                    isConnected = false
                    errorMessage = "Incorrent URL, please correct it."
            }catch (e:UnknownHostException){
                e.printStackTrace()
                isConnected = false
                errorMessage = "Unknown host on url, please correct it."
            }catch (e:FileNotFoundException){
                e.printStackTrace()
                isConnected = true
                errorMessage = ""
            } catch (e: IOException) {
                e.printStackTrace()
                isConnected = false
                errorMessage = "Connection error, please try again later."
            }

            return null
        }

        override fun onPostExecute(integer: Int?) {
            btnSave.setEnabled(true)
            if(errorMessage != null){
                Toast.makeText(this@MainActivity,errorMessage,Toast.LENGTH_SHORT).show()
                Log.v("error_type_on_validate", errorMessage)
            }else if (url.length > 0 && username.length > 0 && password.length > 0 && isConnected && isValid) {
                Toast.makeText(this@MainActivity, "Saved.", Toast.LENGTH_SHORT).show()
                sp.edit().putString("url", url).commit()
                sp.edit().putString("username", username).commit()
                sp.edit().putString("password", password).commit()
                startActivity(startBookmarkView(this@MainActivity, url, username, password, spinnerApi))
                finish()
            }else{
                Toast.makeText(this@MainActivity,"Something was wrong",Toast.LENGTH_SHORT).show()
            }
            super.onPostExecute(integer)
        }
    }

}
