package com.devon_dickson.apps.orgspace;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous API calls on a separate handler thread
 */
public class ApiService extends IntentService {
    //Base URL
    private static final String url = "http://devon-dickson.com/";

    //API path
    private static final String api = "api/v1/";

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    // Intent Actions
    private static final String ACTION_GET_EVENTS = "GET_EVENTS";
    private static final String ACTION_GET_GUESTS = "GET_GUESTS";
    private static final String ACTION_POST_GUESTS = "POST_GUESTS";
    private static final String ACTION_GET_TOKEN = "GET_TOKEN";
    private static final String ACTION_POST_EVENTS = "POST_EVENTS";

    //Intent Extras
    private static final String EXTRA_EVENT_ID = "EventID";
    private static final String EXTRA_USER_ACCESS_TOKEN = "UserAccessToken";
    private static final String EXTRA_FACEBOOK_ID = "FacebookID";
    private static final String EXTRA_EVENT = "Event";

    //JSON Nodes
    private static final String TAG_EVENTID = "id";
    private static final String TAG_EVENTNAME = "name";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_ORG = "org";
    private static final String TAG_START = "startTime";
    private static final String TAG_END = "endTime";
    private static final String TAG_DESC = "description";
    private static final String TAG_IMG = "image";
    private static final String TAG_FACE = "facebook";

    //Results Receiver
    public ResultReceiver receiver;


    public ApiService() {
        super("ApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service", "Started");
        if (intent != null) {
            final String action = intent.getAction();
            receiver = intent.getParcelableExtra("receiver");
            Log.d("Action", action);
            if (ACTION_GET_EVENTS.equals(action)) {
                Log.d("Service", "Starting GetEvents");
                try {
                    handleActionGetEvents();
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            } else if (ACTION_GET_GUESTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT_ID);
                try {
                    handleActionGetGuests(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            } else if (ACTION_GET_TOKEN.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_USER_ACCESS_TOKEN);
                final String param2 = intent.getStringExtra(EXTRA_FACEBOOK_ID);
                try {
                    handleActionGetToken(param1, param2);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            } else if (ACTION_POST_EVENTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT);
                try {
                    handleActionPostEvents(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            } else if (ACTION_POST_GUESTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT_ID);
                try {
                    handleActionPostGuests(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }
        }
    }

    private void handleActionPostGuests(String eventID) throws Exception{
        int result;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String prefJWT = settings.getString("jwt", "");
        Log.d("JWT", prefJWT);

        RequestBody formBody = new FormBody.Builder()
                .add("token", prefJWT)
                .build();

        Request request = new Request.Builder()
                .url(url+api+"events/"+ eventID+"/guests?token="+prefJWT)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String jsonStr = response.body().string();

        if(jsonStr.equals("\"Success\"")) {
            result = 1;
        }else {
            result = 0;
        }
        Log.d("1 means you were added", result+"");
        receiver.send(result, null);
    }

    public void handleActionGetToken(String token, String facebook_id) throws Exception{
        Request request = new Request.Builder().url(url + "auth?id=" + facebook_id + "&token=" + token).build();
        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()) {
            Log.d("Test","failed");
            throw new IOException("Unexpected code " + response);
        }
        Log.d("Test", "Success!");
        String resp = response.body().string();
        Log.d("jwt", resp);

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("jwt", resp);
        editor.commit();
        receiver.send(1, null);
    }

    public void handleActionGetEvents() throws Exception{
        Log.d("Service", "GetEvents started");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String prefJWT = settings.getString("jwt", "");
        Event.deleteAll(Event.class);

        Request request = new Request.Builder()
                .url(url+api+"events?token=" + prefJWT)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        //Event event = gson.fromJson(response.body().charStream(), Event.class);
        String jsonStr = response.body().string();

        if (jsonStr != null) {
            try {
                JSONArray events = new JSONArray(jsonStr);

                // looping through All Contacts
                for (int i = 0; i < events.length(); i++) {
                    JSONObject c = events.getJSONObject(i);

                    String eventID = c.getString(TAG_EVENTID);
                    String name = c.getString(TAG_EVENTNAME);
                    String location = c.getString(TAG_LOCATION);
                    String desc = c.getString(TAG_DESC);
                    String org = c.getString(TAG_ORG);
                    String startTime = c.getString(TAG_START);
                    String endTime = c.getString(TAG_END);
                    String image = c.getString(TAG_IMG);
                    String facebook = c.getString(TAG_FACE);

                    long startInt = 0;
                    long endInt = 0;

                    SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
                    SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    try {
                        Date startDate = parserSDF.parse(startTime);
                        startInt = startDate.getTime();

                        Date endDate = parserSDF.parse(endTime);
                        endInt = endDate.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Event eventTableRow = new Event(eventID, name, location, desc, org, startInt, endInt, image, facebook);
                    eventTableRow.save();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }

        receiver.send(1, null);
    }

    public void handleActionGetGuests(String eventID) throws Exception{
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String prefJWT = settings.getString("jwt", "");
        Log.d("JWT", prefJWT);

        Request request = new Request.Builder()
                //.url(url+api+"events/"+eventID+"/guests?token=" + prefJWT)
                .url(url+api+"events/"+ eventID+"/guests")
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        //Event event = gson.fromJson(response.body().charStream(), Event.class);
        String jsonStr = response.body().string();
        jsonStr = jsonStr.trim();
        Log.d("Guests Response", jsonStr.length()+"");
        Bundle bundle = new Bundle();
        if(jsonStr.equals("\"\"")) {
            bundle.putSerializable("guests", new ArrayList<HashMap<String, String>>());
        }else {
            Log.d("This should", "Not be happening");
            Type collectionType = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();
            ArrayList<HashMap<String, String>> guests = gson.fromJson(jsonStr, collectionType);
            bundle.putSerializable("guests", guests);
        }

        receiver.send(3, bundle);
    }

    public void handleActionPostEvents(String param1) {

    }
}
