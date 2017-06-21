package org.lenchan139.ncbookmark.v2;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.lenchan139.ncbookmark.Class.BookmarkItem;
import org.lenchan139.ncbookmark.R;
import org.lenchan139.ncbookmark.v1.BookmarkViewActivity;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookmarkViewActivityV2 extends AppCompatActivity {
    String jsonUrl;
    String gloTag;
    int deleteId = -1;
    String login;
    String urlNt;

    @Override
    protected void onResume() {

        new BookmarkViewActivityV2.HandleJsonTask().execute();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String url = getIntent().getStringExtra("url");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        urlNt = url;
        login = username + ":" + password;
        String tag = getIntent().getStringExtra("tags");
        if(tag == null){
            Toast.makeText(this, "DONT OPEN THIS DIRECTRY!", Toast.LENGTH_SHORT).show();
            finish();

        }
        toolbar.setTitle(tag);

        setSupportActionBar(toolbar);
        if(Objects.equals(tag, "!ungrouped")){
            tag = "";
        }
        if(url!=null && url.lastIndexOf("/") != url.length()-1){
            url = url + "/";
        }
        if(url!=null && username!=null && password!=null){
            jsonUrl = url + "index.php/apps/bookmarks/public/rest/v2/bookmark?sortby=tags";
            Log.v("jsonUrl",jsonUrl);
        }
        gloTag = tag;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new BookmarkViewActivityV2.HandleJsonTask().execute();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private class HandleJsonTask extends AsyncTask<URL, Integer, Long> {
        String jsonText;

        final String base64login = new String(Base64.encode(login.getBytes(),0));
        protected Long doInBackground(URL... urls) {
            if(Objects.equals(gloTag, "")){
            }else{
                jsonUrl = jsonUrl +"&tags[]=" + gloTag;
            }
            try {
                jsonText =
                        Jsoup.connect(jsonUrl)
                                .header("Authorization", "Basic " + base64login)
                                .ignoreContentType(true)
                                .get().body().text();
                Log.v("jsonText",jsonText);
            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(TagViewActivity.this, "Check your network connection!", Toast.LENGTH_SHORT).show();
            }

            return Long.valueOf(0);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            final List<BookmarkItem> jsonList = new ArrayList<BookmarkItem>();
            try {
                //JSONObject jsonResponse = new JSONObject(jsonText);

                //raw to raw list

                JSONObject a =  new JSONObject(jsonText);
                JSONArray cast = a.getJSONArray("data");
                if(Objects.equals(gloTag, "")){

                    for (int i = 0; i < cast.length(); i++) {
                        JSONObject tempJ = cast.getJSONObject(i);
                        if(Objects.equals(tempJ.getString("tags"), "[\"\"]")) {
                            BookmarkItem tempBi = new BookmarkItem();
                            tempBi.setUrl(tempJ.getString("url"));
                            tempBi.setTags(tempJ.getString("tags"));
                            tempBi.setTitle(tempJ.getString("title"));
                            tempBi.setId(tempJ.getInt("id"));
                            jsonList.add(tempBi);
                            Log.v("currJSONObj", tempBi.toString());
                        }
                    }
                }
                else {
                    for (int i = 0; i < cast.length(); i++) {
                        JSONObject tempJ = cast.getJSONObject(i);
                        BookmarkItem tempBi = new BookmarkItem();
                        tempBi.setUrl(tempJ.getString("url"));
                        tempBi.setTags(tempJ.getString("tags"));
                        tempBi.setTitle(tempJ.getString("title"));
                        tempBi.setId(tempJ.getInt("id"));
                        jsonList.add(tempBi);
                        Log.v("currJSONObj", tempBi.toString());
                    }
                }
                //convert taglist to String[]
                String[] strTags = new String[jsonList.size()];
                for(int i =0;i<jsonList.size();i++){
                    String temp = jsonList.get(i).getTitle();
                    temp = temp.replace("[\"","").replace("\"]","");
                    strTags[i] = temp;

                }

                //start print to UI
                final ListView listviewTags = (ListView) findViewById(R.id.tagList);
                listviewTags.setVisibility(View.VISIBLE);
                final ListAdapter listAdptTags = new ArrayAdapter<String>(BookmarkViewActivityV2.this,android.R.layout.simple_list_item_1,android.R.id.text1,strTags);
                listviewTags.setAdapter(listAdptTags);

                listviewTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            final int position, long id) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(BookmarkViewActivityV2.this);
                        String[] strOptions = {"Open","Edit","Delete"};
                        dialog.setTitle(jsonList.get(position).getTitle());
                        dialog.setItems(strOptions, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    //open
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(jsonList.get(position).getUrl()));
                                    startActivity(intent);
                                }else if(which == 1){
                                    //edit
                                    //Toast.makeText(BookmarkViewActivityV2.this, "Function not yet.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(BookmarkViewActivityV2.this,EditBookmarkActivityV2.class);
                                    intent.putExtra("username",getIntent().getStringExtra("username"));
                                    intent.putExtra("password",getIntent().getStringExtra("password"));
                                    intent.putExtra("url",getIntent().getStringExtra("url"));
                                    intent.putExtra("id",jsonList.get(position).getId());
                                    startActivity(intent);
                                }else if(which == 2){
                                    //delete
                                    AlertDialog.Builder dialogD = new AlertDialog.Builder(BookmarkViewActivityV2.this);
                                    dialogD.setMessage("Delete This:" + jsonList.get(position).getTitle() +
                                            "(Url: " + jsonList.get(position).getUrl() + ") ?");
                                    dialogD.setTitle("Delete Action");
                                    dialogD.setNegativeButton("No", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    dialogD.setPositiveButton("Yes, Sure.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteId = jsonList.get(position).getId();
                                            new DeleteTask().execute();
                                        }
                                    });
                                    dialogD.show();
                                }
                            }
                        });
                        dialog.show();

                    }

                });
                listviewTags.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                        return false;
                    }
                });
            }catch (JSONException e){
                e.printStackTrace();

            }
        }
    }

    private class DeleteTask extends AsyncTask<URL, Integer, Long> {
        Document result;
        String urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark/" + deleteId;
        final String base64login = new String(Base64.encode(login.getBytes(),0));
        @Override
        protected Long doInBackground(URL... params) {
            if(deleteId != -1) {
                try {
                    result = Jsoup.connect(urlNt + urlSe)
                            .ignoreContentType(true)
                            .header("Authorization", "Basic " + base64login)
                            .method(Connection.Method.DELETE)
                            .execute().parse();
                    deleteId = -3;
                } catch (IOException e) {
                    e.printStackTrace();
                    deleteId = -9;
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {
            if(deleteId == -1){

            }else if(deleteId == -3){
                Toast.makeText(BookmarkViewActivityV2.this,"Deletion Done.",Toast.LENGTH_SHORT).show();
                Intent intent = BookmarkViewActivityV2.this.getIntent();
                finish();
                startActivity(intent);
            }else if(deleteId == -9){
                Toast.makeText(BookmarkViewActivityV2.this,"Something was wrong when deletion.",Toast.LENGTH_SHORT).show();
            }
            deleteId = -1;
            super.onPostExecute(aLong);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
