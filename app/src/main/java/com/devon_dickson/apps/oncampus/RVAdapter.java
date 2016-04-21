package com.devon_dickson.apps.oncampus;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ddickson1 on 3/26/2016.
 *
 * This is an adapter for the RecyclerView found in the UpcomingTab Class/Fragment
 * It supplies fields and methods to insert data programmatically into the appropriate views in the layout
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.EventViewHolder>{

    public static class EventViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView eventName;
        TextView eventTime;
        TextView eventLocation;
        TextView eventOrg;
        ImageView eventImage;
        int eventID;

        public EventViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardEvent);
            eventName = (TextView)itemView.findViewById(R.id.listName);
            eventTime = (TextView)itemView.findViewById(R.id.listDate);
            eventLocation = (TextView)itemView.findViewById(R.id.listLocation);
            eventImage = (ImageView)itemView.findViewById(R.id.listImage);
        }

        public void bind(final Event event, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(event);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    List<Event> events;
    EventViewHolder pvh;
    OnItemClickListener listener;

    RVAdapter(List<Event> events, OnItemClickListener listener){
        this.events = events;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_row, viewGroup, false);
        pvh = new EventViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(EventViewHolder eventViewHolder, int i) {
        eventViewHolder.eventName.setText(events.get(i).getName());
        eventViewHolder.eventTime.setText(events.get(i).getStartTimeString());
        eventViewHolder.eventLocation.setText(events.get(i).getLocation());
        String imageURL = "http://devon-dickson.com/images/events/"+ events.get(i).getImage();
        Log.d("Image URL", imageURL);
        Picasso.with(eventViewHolder.eventImage.getContext()).load(imageURL).error(R.drawable.image_error).resize(720, 304).centerCrop().into(eventViewHolder.eventImage);
        eventViewHolder.bind(events.get(i), listener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}