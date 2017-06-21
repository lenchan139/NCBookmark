package org.lenchan139.ncbookmark.v2;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.lenchan139.ncbookmark.R;

import java.io.IOException;
import java.net.URL;

public class EditBookmarkActivityV2 extends AppCompatActivity {

    String urlNt,username,password;
    String[] tags;
    String login;
    EditText edtUrl,edtTitle,edtDescr,edtTag;
    Button btnSelectTag,btnSubmit;
    Toolbar toolbar;
    int bookmarkId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bookmark_v2);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edtDescr = (EditText) findViewById(R.id.description);
        edtTag = (EditText) findViewById(R.id.tag);
        edtTitle = (EditText) findViewById(R.id.title);
        edtUrl = (EditText) findViewById(R.id.url);
        btnSelectTag = (Button) findViewById(R.id.selectTag);
        btnSubmit = (Button) findViewById(R.id.submit);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);
        urlNt = getIntent().getStringExtra("url");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        bookmarkId = getIntent().getIntExtra("id",-1);
        login = username + ":" + password;
        if(urlNt != null && username != null && password !=null && bookmarkId != -1){
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new EditBookmarkActivityV2.EditBookmarkTask().execute();
                }
            });
        }else{

        }

        btnSelectTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditBookmarkActivityV2.fetchTagsTask().execute();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new ShowBookmarkTask().execute();

    }

    private class EditBookmarkTask extends AsyncTask<URL, Integer, Long> {
        int rid = bookmarkId;
        String tag,url,title,des;
        Document result;
        boolean no_error = true;
        String error_msg;
        String urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark/" + rid;
        final String base64login = new String(Base64.encode(login.getBytes(),0));
        @Override
        protected void onPreExecute() {
            tag = edtTag.getText().toString();
            url = edtUrl.getText().toString();
            title = edtTitle.getText().toString();
            des = edtDescr.getText().toString();
            if(tag == null || url == null || title == null){
                no_error = false;
                Toast.makeText(EditBookmarkActivityV2.this, "Please tag, url and title cannot be empty.", Toast.LENGTH_SHORT).show();
            }
            if(des == null){
                des = "";
            }
            super.onPreExecute();
        }


        @Override
        protected Long doInBackground(URL... params) {

            try {
                result = Jsoup.connect(urlNt + urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.PUT)
                        .data("url",url)
                        .data("record_id", String.valueOf(rid))
                        .data("item[tags][]",tag)
                        .data("title",title)
                        .data("description",des)
                        .execute().parse();
            } catch (IOException e) {
                e.printStackTrace();
                error_msg = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {

            if(error_msg != null){
                Toast.makeText(EditBookmarkActivityV2.this, error_msg, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(EditBookmarkActivityV2.this, "Bookmark Edited!", Toast.LENGTH_SHORT).show();
                EditBookmarkActivityV2.this.finish();
            }
            super.onPostExecute(aLong);
        }
    }

    private class fetchTagsTask extends AsyncTask<URL, Integer, Long> {
        String urlSe = "/index.php/apps/bookmarks/public/rest/v2/tag";
        final String base64login = new String(Base64.encode(login.getBytes(),0));
        Document result;

        @Override
        protected Long doInBackground(URL... params) {

            try {
                result = Jsoup.connect(urlNt + urlSe)
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.GET)
                        .execute().parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            Log.v("testAllTags",result.body().text());
            try {
                JSONArray jsonArray = new JSONArray(result.body().text());
                tags = new String[jsonArray.length()];
                for (int i=0;i<jsonArray.length();i++){
                    tags[i] = jsonArray.get(i).toString();
                    Log.v("testTagConverting",jsonArray.get(i).toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
                tags = null;
            }
            if(tags != null){
                AlertDialog.Builder dialog = new AlertDialog.Builder(EditBookmarkActivityV2.this);
                dialog.setTitle("Exciting Tag");
                dialog.setItems(tags, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edtTag.setText(tags[which]);
                    }
                });
                dialog.setNegativeButton("Cancel",null);
                dialog.show();
            }
            super.onPostExecute(aLong);
        }

    }

    private class ShowBookmarkTask extends AsyncTask<URL, Integer, Long> {

        int rid = bookmarkId;
        Document result;
        boolean no_error = true;
        String error_msg;
        String urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark?page=-1";
        final String base64login = new String(Base64.encode(login.getBytes(),0));
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(URL... params) {

            try {
                result = Jsoup.connect(urlNt + urlSe )
                        .ignoreContentType(true)
                        .header("Authorization", "Basic " + base64login)
                        .method(Connection.Method.GET)
                        .execute().parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v("testLog",result.body().text());
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {

            JSONObject a = null;
            JSONObject finalObject = null;
            try {
                a = new JSONObject(result.body().text());
                JSONArray cast = a.getJSONArray("data");
                for(int i =0;i<cast.length();i++){
                    if(cast.getJSONObject(i).getInt("id") == rid){
                        finalObject = cast.getJSONObject(i);
                        edtDescr.setText(finalObject.getString("description"));
                        edtTag.setText(finalObject.getJSONArray("tags").getString(0));
                        edtTitle.setText(finalObject.getString("title"));
                        edtUrl.setText(finalObject.getString("url"));

                        toolbar.setTitle(finalObject.getString("title"));

                    }else{
                        finalObject = null;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(EditBookmarkActivityV2.this, "Network Problems", Toast.LENGTH_SHORT).show();
                finish();
            }
            super.onPostExecute(aLong);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
