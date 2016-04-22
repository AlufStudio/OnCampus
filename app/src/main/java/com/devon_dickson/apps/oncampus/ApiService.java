package com.devon_dickson.apps.oncampus;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    //Initialize okHTTP and GSON
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    // Intent Actions
    private static final String ACTION_GET_EVENTS = "GET_EVENTS";
    private static final String ACTION_GET_GUESTS = "GET_GUESTS";
    private static final String ACTION_POST_GUESTS = "POST_GUESTS";
    private static final String ACTION_GET_TOKEN = "GET_TOKEN";
    private static final String ACTION_POST_EVENTS = "POST_EVENTS";
    private static final String ACTION_DELETE_GUESTS = "DELETE_GUESTS";

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

    //Result Codes
    int result;
    private static final int GET_GUESTS_SUCCESS = 0;
    private static final int POST_GUEST_SUCCESS = 1;
    private static final int POST_GUEST_FAIL = 2;
    private static final int DELETE_GUEST_SUCCESS = 3;
    private static final int DELETE_GUEST_FAIL = 4;
    private static final int GET_TOKEN_SUCCESS = 5;
    private static final int GET_EVENTS_SUCCESS = 6;

    //Results Receiver
    public ResultReceiver receiver;

    //JWT
    public String prefJWT;

    public ApiService() {
        super("ApiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefJWT = settings.getString("jwt", "");

        if (intent != null) {
            final String action = intent.getAction();
            receiver = intent.getParcelableExtra("receiver");

            //Get Events Service
            if (ACTION_GET_EVENTS.equals(action)) {
                Log.d("Service", "Starting GetEvents");
                try {
                    handleActionGetEvents();
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }

            //Get Guests Service
            else if (ACTION_GET_GUESTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT_ID);
                try {
                    handleActionGetGuests(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }

            //Get JWT service
            else if (ACTION_GET_TOKEN.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_USER_ACCESS_TOKEN);
                final String param2 = intent.getStringExtra(EXTRA_FACEBOOK_ID);
                try {
                    handleActionGetToken(param1, param2);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }

            //Create new event Service
            else if (ACTION_POST_EVENTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT);
                try {
                    handleActionPostEvents(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }

            //RSVP to event service
            else if (ACTION_POST_GUESTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT_ID);
                try {
                    handleActionPostGuests(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }

            //Cancel RSVP to event service
            else if (ACTION_DELETE_GUESTS.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_EVENT_ID);
                try {
                    handleActionDeleteGuests(param1);
                }catch(Exception e) {
                    Log.d("Exception",e+"");
                }
            }
        }
    }

    //Cancel RSVP to event service
    private void handleActionDeleteGuests(String eventID) throws Exception{
        Request request = new Request.Builder()
                .url(url+api+"events/"+ eventID+"/guests/0?token="+prefJWT)
                .delete(null)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String jsonStr = response.body().string();

        if(jsonStr.equals("\"Success\"")) {
            result = DELETE_GUEST_SUCCESS;
        }else {
            result = DELETE_GUEST_FAIL;
        }

        receiver.send(result, null);
    }

    //RSVP to event service
    private void handleActionPostGuests(String eventID) throws Exception{
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
            result = POST_GUEST_SUCCESS;
        }else {
            result = POST_GUEST_FAIL;
        }

        receiver.send(result, null);
    }

    //Get JWT service
    public void handleActionGetToken(String token, String facebook_id) throws Exception{
        Request request = new Request.Builder().url(url + "auth?id=" + facebook_id + "&token=" + token).build();
        Response response = client.newCall(request).execute();

        if(!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String resp = response.body().string();

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("jwt", resp);
        editor.commit();

        receiver.send(GET_TOKEN_SUCCESS, null);
    }

    //Get events service
    public void handleActionGetEvents() throws Exception{
        Event.deleteAll(Event.class);

        Request request = new Request.Builder()
                .url(url+api+"events?token=" + prefJWT)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String jsonStr = response.body().string();

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

        receiver.send(GET_EVENTS_SUCCESS, null);
    }

    //Get event guests service
    public void handleActionGetGuests(String eventID) throws Exception{
        Request request = new Request.Builder()
                //.url(url+api+"events/"+eventID+"/guests?token=" + prefJWT)
                .url(url+api+"events/"+ eventID+"/guests")
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String jsonStr = response.body().string();

        Bundle bundle = new Bundle();

        if(jsonStr.equals("\"\"")) {
            bundle.putSerializable("guests", new ArrayList<HashMap<String, String>>());
        }else {
            Type collectionType = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();
            ArrayList<HashMap<String, String>> guests = gson.fromJson(jsonStr, collectionType);
            bundle.putSerializable("guests", guests);
        }

        receiver.send(GET_GUESTS_SUCCESS, bundle);
    }

    //Create new Event Service
    public void handleActionPostEvents(String param1) {
        //Not implemented yet
    }
}
