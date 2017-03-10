package org.lenchan139.ncbookmark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import org.jsoup.Jsoup;
import org.lenchan139.ncbookmark.Class.BookmarkItem;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TagViewActivity extends AppCompatActivity {
    String jsonUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String url = getIntent().getStringExtra("url");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        if(url.lastIndexOf("/") != url.length()-1){
            url = url + "/";
        }
        if(url!=null && username!=null && password!=null){
            jsonUrl = url + "index.php/apps/bookmarks/public/rest/v1/bookmark?user="
                    + username + "&password=" + password
                    + "&select[]=tags&sortby=tags";
            Log.v("jsonUrl",jsonUrl);
        }


        new HandleJsonTask().execute();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }
    private class HandleJsonTask extends AsyncTask<URL, Integer, Long> {
        String jsonText;
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
                    if(Objects.equals(temp, "")){ temp="!ungrouped"; }
                    strTags[i] = temp;

                }

                //start print to UI
                final ListView listviewTags = (ListView) findViewById(R.id.tagList);
                listviewTags.setVisibility(View.VISIBLE);
                ListAdapter listAdptTags = new ArrayAdapter<String>(TagViewActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,strTags);
                listviewTags.setAdapter(listAdptTags);

                listviewTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        openBookmarkView(strTags[position]);
                        


                    }

                });
            }catch (JSONException e){
                e.printStackTrace();

            }
        }
    }
    private  void openBookmarkView(String tag){
        Intent intent = new Intent(this,BookmarkViewActivity.class);
        intent.putExtra("username",getIntent().getStringExtra("username"));
        intent.putExtra("password",getIntent().getStringExtra("password"));
        intent.putExtra("url",getIntent().getStringExtra("url"));
        intent.putExtra("tags",tag);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bookmark_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset) {
            SharedPreferences sp = getSharedPreferences("data",0);
            sp.edit().clear().commit();
            startActivity(new Intent(this,MainActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
