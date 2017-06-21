package org.lenchan139.ncbookmark.v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.lenchan139.ncbookmark.Class.BookmarkItem;
import org.lenchan139.ncbookmark.MainActivity;
import org.lenchan139.ncbookmark.R;
import org.lenchan139.ncbookmark.v1.BookmarkViewActivity;
import org.lenchan139.ncbookmark.v1.TagViewActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TagListActivityV2 extends AppCompatActivity {
    String urlNt,username,password;
    String login;
    String urlSe = "/index.php/apps/bookmarks/public/rest/v2/bookmark?page=-1";

    @Override
    protected void onResume() {
        new DlTask().execute();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_list_v2);
        urlNt = getIntent().getStringExtra("url");
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

         login = username + ":" + password;

        if(urlNt !=null && username != null && password != null){
            new DlTask().execute();
        }else{
            Toast.makeText(this, "Plase login first!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    class DlTask extends AsyncTask<String, Integer, Integer>{
        Document result;

        final String base64login = new String(Base64.encode(login.getBytes(),0));
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                result =  Jsoup.connect(urlNt + urlSe)
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
        protected void onPostExecute(Integer integer) {
            if(result != null){
                Log.v("testLog",result.body().text());

                List<BookmarkItem> jsonList = new ArrayList<BookmarkItem>();
                try {
                    //JSONObject jsonResponse = new JSONObject(jsonText);

                    //raw to raw list
                    JSONObject a =  new JSONObject(result.body().text());
                    JSONArray cast = a.getJSONArray("data");
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
                    ListAdapter listAdptTags = new ArrayAdapter<String>(TagListActivityV2.this,android.R.layout.simple_list_item_1,android.R.id.text1,strTags);
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
            super.onPostExecute(integer);
        }
    }
    private void openBookmarkView(String tag){
        Intent intent = new Intent(this,BookmarkViewActivityV2.class);
        intent.putExtra("username",getIntent().getStringExtra("username"));
        intent.putExtra("password",getIntent().getStringExtra("password"));
        intent.putExtra("url",getIntent().getStringExtra("url"));
        intent.putExtra("tags",tag);
        startActivity(intent);
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
        }else if(id == R.id.add_bookmark){
            Intent intent = new Intent(this,AddBookmarkActivityV2.class);
            intent.putExtra("username",getIntent().getStringExtra("username"));
            intent.putExtra("password",getIntent().getStringExtra("password"));
            intent.putExtra("url",getIntent().getStringExtra("url"));
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
