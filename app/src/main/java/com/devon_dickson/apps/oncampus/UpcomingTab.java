package com.devon_dickson.apps.oncampus;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.orm.SugarContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ddickson1 on 1/02/2016.
 */


public class UpcomingTab extends Fragment implements ApiServiceResultReceiver.Receiver{

    private SwipeRefreshLayout swipeContainer;

    public ApiServiceResultReceiver mReceiver;

    // URL to get contacts JSON
    private static String url = "http://devon-dickson.com/api/v1/events";

    // JSON Node names
    private static final String TAG_EVENT = "event";
    private static final String TAG_EVENTID = "id";
    private static final String TAG_EVENTNAME = "name";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_ORG = "org";
    private static final String TAG_START = "startTime";
    private static final String TAG_END = "endTime";
    private static final String TAG_DESC = "description";
    private static final String TAG_IMG = "image";
    private static final String TAG_FACE = "facebook";
    private ListView lv;
    private RecyclerView rv;
    private ActionBar ab;
    private LinearLayoutManager llm;
    private ViewPager viewPager;
    private String JWT;
    // contacts JSONArray
    JSONArray events = null;
    ArrayList<HashMap<String, String>> eventList;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    public Context context;

    //@Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_upcoming_tab, container, false);
        SugarContext.init(getActivity());

        eventList = new ArrayList<HashMap<String, String>>();

        context = getActivity();


        mReceiver = new ApiServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        //new GetContacts().execute();
        Intent intent = new Intent(getActivity(), ApiService.class);
        intent.putExtra("receiver", mReceiver);
        intent.setAction("GET_EVENTS");
        getActivity().startService(intent);


        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        //lv = (ListView) view.findViewById(R.id.eventList);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //new GetContacts().execute();
                Intent intent = new Intent(getActivity(), ApiService.class);
                intent.setAction("GET_EVENTS");
                intent.putExtra("receiver", mReceiver);
                getActivity().startService(intent);
                Log.d("Pull to refresh", "Success!");
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ab = getActivity().getActionBar();
        ab.setTitle("Events");
        ab.setSubtitle("On Campus");
    }

    @Override
    public void onResume() {
        /*Intent intent = new Intent(getActivity(), ApiService.class);
        intent.putExtra("receiver", mReceiver);
        intent.setAction("GET_EVENTS");
        getActivity().startService(intent);
        */
        List<Event> events = Event.listAll(Event.class);
        rv.setAdapter(new RVAdapter(events, new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                openEvent(event.getEventID());
            }
        }));
        super.onResume();
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        List<Event> events = Event.listAll(Event.class);
        rv.setAdapter(new RVAdapter(events, new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                openEvent(event.getEventID());
            }
        }));
    }

    public class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                updateEvents();
            }catch(Exception e) {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            List<Event> events = Event.listAll(Event.class);
            rv.setAdapter(new RVAdapter(events, new RVAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Event event) {
                    openEvent(event.getEventID());
                }
            }));

        }
    }

    public void openEvent(String eventID) {
        int ID = Integer.parseInt(eventID);

        Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);

        eventDetailsIntent.putExtra("EventID", ID);

        getActivity().startActivity(eventDetailsIntent);
    }

    public void updateEvents() throws Exception{
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        String prefJWT = settings.getString("jwt", "");
        Event.deleteAll(Event.class);

        Request request = new Request.Builder()
                .url(url+"?token=" + prefJWT)
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
    }
}
