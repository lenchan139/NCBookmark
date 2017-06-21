package org.lenchan139.ncbookmark;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.lenchan139.ncbookmark.v1.TagViewActivity;
import org.lenchan139.ncbookmark.v2.TagListActivityV2;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
EditText edtUrl,edtUsername,edtPassword;
    Button btnSave;
    SharedPreferences sp;
    String url, username, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("data",0);

        edtUrl = (EditText) findViewById(R.id.url);
        edtUsername = (EditText) findViewById(R.id.username);
        edtPassword = (EditText) findViewById(R.id.password);
        btnSave = (Button) findViewById(R.id.save);

        final Spinner spinner = ((Spinner) findViewById(R.id.spinnerApi));
        String[] apiString = {"v2","v1"};
        ArrayAdapter<String> list1 = new ArrayAdapter(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                apiString);
        spinner.setAdapter(list1);


        url = sp.getString("url","");
        username = sp.getString("username","");
        password = sp.getString("password","");
        if(!Objects.equals(url, "") && !Objects.equals(username, "") && !Objects.equals(password, "")){
            startActivity(startBookmarkView(this,url,username,password,spinner));
            finish();
        }else {

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(MainActivity.this, spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                    url = edtUrl.getText().toString();
                    username = edtUsername.getText().toString();
                    password = edtPassword.getText().toString();
                    if(url.length()>0 && username.length()>0 && password.length()>0) {
                        Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();
                        sp.edit().putString("url", url).commit();
                        sp.edit().putString("username", username).commit();
                        sp.edit().putString("password", password).commit();
                        startActivity(startBookmarkView(MainActivity.this,url,username,password,spinner));
                        finish();
                    }
                }
            });
        }

    }

    public Intent startBookmarkView(Activity activity, String url, String username, String password,Spinner spinner){
        if (spinner.getSelectedItem().toString().contains("v1")) {
            Intent intent = new Intent(activity, TagViewActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            return intent;
        }else if(spinner.getSelectedItem().toString().contains("v2")){
            Intent intent = new Intent(activity, TagListActivityV2.class);
            intent.putExtra("url", url);
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            return intent;
        }
        else{
            return null;
        }

    }
}
