package com.devon_dickson.apps.oncampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;

public class LoginActivity extends AppCompatActivity implements ApiServiceResultReceiver.Receiver{
    CallbackManager callbackManager;
    public Intent mainActivityIntent;
    public LoginResult result;
    public ApiServiceResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default");
        Log.d("Theme", themeName);
        if (themeName.equals("Night")) {
            setTheme(R.style.NightAppCompatTheme);



        }else if (themeName.equals("FIM")) {
            //Toast.makeText(this, "set theme", Toast.LENGTH_SHORT).show();

            setTheme(R.style.FimAppCompatTheme);

        }else if (themeName.equals("Default")) {

            setTheme(R.style.AppCompatTheme);

        }
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        mReceiver = new ApiServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        if(AccessToken.getCurrentAccessToken()!=null) {
            mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
        }

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.authButton);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                result = loginResult;

                Intent intent = new Intent(getApplicationContext(), ApiService.class);
                intent.setAction("GET_TOKEN");
                intent.putExtra("receiver", mReceiver);
                intent.putExtra("UserAccessToken", result.getAccessToken().getToken());
                intent.putExtra("FacebookID", result.getAccessToken().getUserId());
                getApplicationContext().startService(intent);

            }

            @Override
            public void onCancel() {
                Log.d("Facebook Login Status", "Canceled!");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook Login Status", "Failed!");
            }


        });

    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        String themeName = pref.getString("theme", "Default");
        Log.d("Theme", themeName);
        if (themeName.equals("Night")) {
            setTheme(R.style.NightAppCompatTheme);



        }else if (themeName.equals("FIM")) {
            //Toast.makeText(this, "set theme", Toast.LENGTH_SHORT).show();

            setTheme(R.style.FimAppCompatTheme);

        }else if (themeName.equals("Default")) {

            setTheme(R.style.AppCompatTheme);

        }
    }
}
