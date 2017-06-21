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
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.lenchan139.ncbookmark.R;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class AddBookmarkActivityV2 extends AppCompatActivity {

    String urlNt,username,password;
    String[] tags;
    String login;
    EditText edtUrl,edtTitle,edtDescr,edtTag;
    Button btnSelectTag,btnSubmit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bookmark_v2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        //
         urlNt = getIntent().getStringExtra("url");
         username = getIntent().getStringExtra("username");
         password = getIntent().getStringExtra("password");
        login = username + ":" + password;
        if(urlNt != null && username != null && password !=null){
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AddBookmarkTask().execute();
                }
            });
        }else{

        }

        btnSelectTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new fetchTagsTask().execute();
            }
        });
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(AddBookmarkActivityV2.this);
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

    private class AddBookmarkTask extends AsyncTask<URL, Integer, Long> {
        String tag,url,title,des;
        Document result;
        boolean no_error = true;
        String error_msg;
        String urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark";
        final String base64login = new String(Base64.encode(login.getBytes(),0));
        @Override
        protected void onPreExecute() {
            tag = edtTag.getText().toString();
            url = edtUrl.getText().toString();
            title = edtTitle.getText().toString();
            des = edtDescr.getText().toString();
            if(tag == null || url == null || title == null){
                no_error = false;
                Toast.makeText(AddBookmarkActivityV2.this, "Please tag, url and title cannot be empty.", Toast.LENGTH_SHORT).show();
            }
            if(des == null){
                des = "";
            }
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(URL... params) {
            if(no_error){

                try {
                    result = Jsoup.connect(urlNt + urlSe)
                            .ignoreContentType(true)
                            .header("Authorization", "Basic " + base64login)
                            .method(Connection.Method.POST)
                            .data("url",url)
                            .data("item[tags][]",tag)
                            .data("title",title)
                            .data("description",des)
                            .execute().parse();
                } catch (IOException e) {
                    e.printStackTrace();
                    error_msg = e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if(error_msg != null){
                Toast.makeText(AddBookmarkActivityV2.this, error_msg, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(AddBookmarkActivityV2.this, "Bookmark Added!", Toast.LENGTH_SHORT).show();
                AddBookmarkActivityV2.this.finish();
            }
            super.onPostExecute(aLong);
        }
    }
}
