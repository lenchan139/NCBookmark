package org.lenchan139.ncbookmark;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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




        url = sp.getString("url","");
        username = sp.getString("username","");
        password = sp.getString("password","");
        if(!Objects.equals(url, "") && !Objects.equals(username, "") && !Objects.equals(password, "")){
            startActivity(startBookmarkView(this,url,username,password));
            finish();
        }else {

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    url = edtUrl.getText().toString();
                    username = edtUsername.getText().toString();
                    password = edtPassword.getText().toString();
                    if(url.length()>0 && username.length()>0 && password.length()>0) {
                        Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();
                        sp.edit().putString("url", url).commit();
                        sp.edit().putString("username", username).commit();
                        sp.edit().putString("password", password).commit();
                        startActivity(startBookmarkView(MainActivity.this,url,username,password));
                        finish();
                    }
                }
            });
        }

    }

    public Intent startBookmarkView(Activity activity, String url, String username, String password){
        Intent intent = new Intent(activity,TagViewActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("username",username);
        intent.putExtra("password",password);
        return intent;

    }
}
