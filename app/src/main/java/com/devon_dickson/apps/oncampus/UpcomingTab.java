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

    private RecyclerView rv;

    private ActionBar ab;
    private LinearLayoutManager llm;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_upcoming_tab, container, false);
        SugarContext.init(getActivity());

        mReceiver = new ApiServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Intent intent = new Intent(getActivity(), ApiService.class);
        intent.putExtra("receiver", mReceiver);
        intent.setAction("GET_EVENTS");
        getActivity().startService(intent);

        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
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
    public void onReceiveResult(int resultCode, Bundle resultData) {
        List<Event> events = Event.listAll(Event.class);
        rv.setAdapter(new RVAdapter(events, new RVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Event event) {
                openEvent(event.getEventID());
            }
        }));
    }

    public void openEvent(String eventID) {
        int ID = Integer.parseInt(eventID);

        Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);
        eventDetailsIntent.putExtra("EventID", ID);
        getActivity().startActivity(eventDetailsIntent);
    }
}
