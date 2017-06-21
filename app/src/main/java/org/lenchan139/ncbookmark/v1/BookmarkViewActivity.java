package org.lenchan139.ncbookmark.v1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import org.lenchan139.ncbookmark.MainActivity;
import org.lenchan139.ncbookmark.R;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookmarkViewActivity extends AppCompatActivity {
    String jsonUrl;
    String gloTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        String url = getIntent().getStringExtra("url");
        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
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
            jsonUrl = url + "index.php/apps/bookmarks/public/rest/v1/bookmark?user="
                    + username
                    + "&password=" + password
                    + "&select[]=tags&sortby=tags";
            Log.v("jsonUrl",jsonUrl);
        }
        gloTag = tag;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new HandleJsonTask().execute();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
    }
    private class HandleJsonTask extends AsyncTask<URL, Integer, Long> {
        String jsonText;
        protected Long doInBackground(URL... urls) {
            if(Objects.equals(gloTag, "")){
            }else{
                jsonUrl = jsonUrl +"&tags[]=" + gloTag;
            }
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
            final List<BookmarkItem> jsonList = new ArrayList<BookmarkItem>();
            try {
                //JSONObject jsonResponse = new JSONObject(jsonText);

                //raw to raw list

                JSONArray cast = new JSONArray(jsonText);
                if(Objects.equals(gloTag, "")){

                    for (int i = 0; i < cast.length(); i++) {
                        JSONObject tempJ = cast.getJSONObject(i);
                        if(Objects.equals(tempJ.getString("tags"), "[\"\"]")) {
                            BookmarkItem tempBi = new BookmarkItem();
                            tempBi.setUrl(tempJ.getString("url"));
                            tempBi.setTags(tempJ.getString("tags"));
                            tempBi.setTitle(tempJ.getString("title"));
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
                ListAdapter listAdptTags = new ArrayAdapter<String>(BookmarkViewActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,strTags);
                listviewTags.setAdapter(listAdptTags);

                listviewTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(jsonList.get(position).getUrl()));
                        startActivity(intent);

                    }

                });
            }catch (JSONException e){
                e.printStackTrace();

            }
        }
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
            Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
