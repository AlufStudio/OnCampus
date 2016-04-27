package com.devon_dickson.apps.oncampus;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ddickson1 on 4/27/2016.
 */
public class SavedTab extends Fragment implements ApiServiceResultReceiver.Receiver{
    private SwipeRefreshLayout swipeContainer;

    public ApiServiceResultReceiver mReceiver;

    private RecyclerView rv;

    private ActionBar ab;
    private LinearLayoutManager llm;

    public List<Event> events = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_saved_tab, container, false);
        SugarContext.init(getActivity());

        mReceiver = new ApiServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Intent eventIntent = new Intent(getActivity(), ApiService.class);
        eventIntent.putExtra("receiver", mReceiver);
        eventIntent.setAction("GET_EVENTS");
        getActivity().startService(eventIntent);

        //Intent intent = new Intent(getActivity(), ApiService.class);
        //intent.putExtra("receiver", mReceiver);
        //intent.setAction("GET_SAVED_EVENTS");
        //getActivity().startService(intent);

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
                intent.setAction("GET_SAVED_EVENTS");
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
        if(resultCode==7) {
            ArrayList<String> results = resultData.getStringArrayList("events");
            if (results.size() > 0) {
                for (int i = 0; i < results.size(); i++) {
                    String eventID = results.get(i);
                    Log.d("Event ID", eventID);
                    List<Event> test = Event.listAll(Event.class);
                    Log.d("Table size", test.size()+"");
                    Event event = Event.find(Event.class, "EVENT_ID = ?", eventID).get(0);
                    events.add(event);
                }
            }

            rv.setAdapter(new RVAdapter(events, new RVAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Event event) {
                    openEvent(event.getEventID());
                }
            }));
        }
        else {
            Intent intent = new Intent(getActivity(), ApiService.class);
            intent.putExtra("receiver", mReceiver);
            intent.setAction("GET_SAVED_EVENTS");
            getActivity().startService(intent);
        }
    }

    public void openEvent(String eventID) {
        int ID = Integer.parseInt(eventID);

        Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);
        eventDetailsIntent.putExtra("EventID", ID);
        getActivity().startActivity(eventDetailsIntent);
    }
}

