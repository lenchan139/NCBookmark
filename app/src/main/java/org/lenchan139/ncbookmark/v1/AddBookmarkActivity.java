package org.lenchan139.ncbookmark.v1;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.lenchan139.ncbookmark.Class.BookmarkItem;
import org.lenchan139.ncbookmark.R;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddBookmarkActivity extends AppCompatActivity {
    SharedPreferences sp;
    String url, username, password;
    EditText edtUrl,edtTitle,edtDes,edtTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bookmark);
        sp = getSharedPreferences("data",0);
        edtUrl = (EditText) findViewById(R.id.url);
        edtDes = (EditText) findViewById(R.id.description);
        edtTag = (EditText) findViewById(R.id.tag);
        edtTitle = (EditText) findViewById(R.id.title);

        url = sp.getString("url","");
        username = sp.getString("username","");
        password = sp.getString("password","");
        if(Objects.equals(url, "") || Objects.equals(username, "") || Objects.equals(password, "")){
            finish();
        }


    }
    private class DlExsitTags extends AsyncTask<URL, Integer, Long> {
        String jsonText;
        String jsonUrl;
        String strTagsList[];
        @Override
        protected void onPreExecute() {
            if(url!=null && username!=null && password!=null){
                jsonUrl = url + "index.php/apps/bookmarks/public/rest/v1/bookmark?user="
                        + username + "&password=" + password
                        + "&select[]=tags&sortby=tags";
                Log.v("jsonUrl",jsonUrl);
            }
            super.onPreExecute();
        }

        protected Long doInBackground(URL... urls) {

            try {
                jsonText =
                        Jsoup.connect(jsonUrl)
                                .header("header","application/json")
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
            List<BookmarkItem> jsonList = new ArrayList<BookmarkItem>();
            try {
                //JSONObject jsonResponse = new JSONObject(jsonText);

                //raw to raw list
                JSONArray cast = new JSONArray(jsonText);
                for (int i = 0; i < cast.length(); i++) {
                    JSONObject tempJ = cast.getJSONObject(i);
                    BookmarkItem tempBi = new BookmarkItem();
                    tempBi.setUrl(tempJ.getString("url"));
                    tempBi.setTags(tempJ.getString("tags"));
                    tempBi.setTitle(tempJ.getString("title"));
                    jsonList.add(tempBi);
                    Log.v("currJSONObj",tempBi.toString());
                }
                //get all tags
                List<String> listTags = new ArrayList<>();
                for (int i1 =0;i1<jsonList.size();i1++){
                    String tempTags = jsonList.get(i1).getTags();
                    boolean isHad = false;
                    //check tag if had.
                    for(int i2=0;i2<listTags.size();i2++){
                        if(Objects.equals(tempTags, listTags.get(i2))){
                            isHad = true;
                        }
                    }
                    //if not had, add it.
                    if(!isHad){
                        listTags.add(tempTags);
                    }
                }
                Log.v("listArray",listTags.toString());

                //convert taglist to String[]
                final String[] strTags = new String[listTags.size()];
                for(int i =0;i<listTags.size();i++){
                    String temp = listTags.get(i);
                    temp = temp.replace("[\"","").replace("\"]","");
                    if(!Objects.equals(temp, "")){
                        strTags[i] = temp;
                    }


                }
                strTagsList = strTags;

                //reserve for diabox
            }catch (JSONException e){
                e.printStackTrace();

            }
        }
    }

    private class PostBookmark extends AsyncTask<URL, Integer, Long> {
        String jsonUrl;
        String pTitle, pUrl, pDes, pTag;
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pTitle = edtTitle.getText().toString();
            pUrl = edtUrl.getText().toString();
            pDes = edtDes.getText().toString();
            pTag = edtTag.getText().toString();

            if(url!=null && username!=null && password!=null ) {
                jsonUrl = url + "index.php/apps/bookmarks/public/rest/v1/bookmark?user="
                        + username + "&password=" + password
                        ;
                Log.v("jsonUrl", jsonUrl);
            }
        }

        @Override
        protected Long doInBackground(URL... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
        }
    }
}

