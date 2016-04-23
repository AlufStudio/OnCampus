package com.devon_dickson.apps.oncampus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class EventDetailsActivity extends SwipeActivity implements ApiServiceResultReceiver.Receiver{
    //ResultsReceiver resultCodes
    private static final int GET_GUESTS_SUCCESS = 0;
    private static final int POST_GUEST_SUCCESS = 1;
    private static final int POST_GUEST_FAIL = 2;
    private static final int DELETE_GUEST_SUCCESS = 3;
    private static final int DELETE_GUEST_FAIL = 4;

    public static int eventID;
    public static Event event;
    public static TextView textLocation;
    public static TextView textDate;
    public static ImageView imageBanner;
    public static String bannerURL;
    public static TextView textDescription;
    public static TextView textOrg;
    public static CardView cardDate;
    public static TextView headerFriends;
    public static Button buttonYes;
    public static Button buttonNo;
    public ArrayList<HashMap<String, String>> guests;
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
        setContentView(R.layout.activity_event_details);
        mReceiver = new ApiServiceResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            eventID = intent.getIntExtra("EventID", 1);
        }

        event = Event.find(Event.class, "EVENT_ID="+eventID).get(0);
        textLocation = (TextView) findViewById(R.id.textLocation);
        textDate = (TextView) findViewById(R.id.textDate);
        imageBanner = (ImageView) findViewById(R.id.backdrop);
        textDescription = (TextView) findViewById(R.id.description);
        cardDate = (CardView) findViewById(R.id.cardDate);
        textOrg = (TextView) findViewById(R.id.textOrg);
        headerFriends = (TextView) findViewById(R.id.headerFriends);
        buttonYes = (Button) findViewById(R.id.buttonYes);
        buttonNo = (Button) findViewById(R.id.buttonNo);


        CollapsingToolbarLayout tb = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        tb.setTitle(event.getName());
        textLocation.setText(event.getLocation());
        textDate.setText(event.getStartTime());
        textDescription.setText(event.getDescription());
        textOrg.setText(event.getOrg());

        bannerURL = "http://devon-dickson.com/images/events/"+ event.getImage();
        Picasso.with(this).load(bannerURL).error(R.drawable.image_error).resize(720, 304).centerCrop().into(imageBanner);

        setClickListeners();
        fetchGuests();
    }

    public void setClickListeners() {
        cardDate.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setData(CalendarContract.Events.CONTENT_URI);
                calIntent.putExtra(CalendarContract.Events.TITLE, event.getName());
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
                calIntent.putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription());

                //TODO: Pass exact start and end times to calendar app
                GregorianCalendar calDate = new GregorianCalendar(2016, 7, 15);
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        calDate.getTimeInMillis());
                calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                        calDate.getTimeInMillis());

                //TODO: Saving event in Calendar App should return user to this activity
                startActivityForResult(calIntent, 1);
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent yesIntent = new Intent(getApplication(), ApiService.class);
                yesIntent.putExtra("receiver", mReceiver);
                yesIntent.putExtra("EventID", event.getEventID());
                yesIntent.setAction("POST_GUESTS");
                getApplication().startService(yesIntent);
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent NoIntent = new Intent(getApplication(), ApiService.class);
                NoIntent.putExtra("receiver", mReceiver);
                NoIntent.putExtra("EventID", event.getEventID());
                NoIntent.setAction("DELETE_GUESTS");
                getApplication().startService(NoIntent);
            }
        });
    }

    //Return to previous event, or eventList if this was the first event opened.
    @Override
    protected void onSwipeRight() {
        finish();
        overridePendingTransition(R.anim.right_slide_in, R.anim.right_slide_out);
    }

    //Move to next Event in the database
    //TODO: Change method for deciding what the next activity should be
    //      Right now, the database is always refreshed with events that haven't yet occurred, in chrono
    //      order. But if I ever get filtering and searching and ordering to work in the Main Activity,
    //      Then eventID+1 will load up an event that should have been filtered out.
    @Override
    protected void onSwipeLeft() {
        try {
            long endTime = event.getEndInt();
            String newID = Event.findWithQuery(Event.class, "Select * from Event WHERE END_INT > " + endTime).get(0).getEventID();
            Intent eventDetailsIntent = new Intent(this, EventDetailsActivity.class);

            eventDetailsIntent.putExtra("EventID", (Integer.parseInt(newID)));

            this.startActivity(eventDetailsIntent);
        }catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, "No more events", Toast.LENGTH_SHORT);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        super.dispatchTouchEvent(ev);
        return gestureDetector.onTouchEvent(ev);
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if(resultCode==GET_GUESTS_SUCCESS) {
            guests = (ArrayList<HashMap<String, String>>)(resultData.getSerializable("guests"));
            displayGuests(guests);
        }else if(resultCode==POST_GUEST_SUCCESS) {
            fetchGuests();
            Toast.makeText(getApplication(), "Great, see you there!", Toast.LENGTH_SHORT).show();
        }else if(resultCode==POST_GUEST_FAIL) {
            Toast.makeText(getApplication(), "Whoops, you're already going!", Toast.LENGTH_SHORT).show();
        }else if(resultCode==DELETE_GUEST_SUCCESS) {
            fetchGuests();
            Toast.makeText(getApplication(), "Maybe next time?", Toast.LENGTH_SHORT).show();
        }else if(resultCode==DELETE_GUEST_FAIL) {
            Toast.makeText(getApplication(), "Whoops, you weren't going anyways!", Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchGuests() {
        Intent guestsIntent = new Intent(this, ApiService.class);
        guestsIntent.putExtra("receiver", mReceiver);
        guestsIntent.putExtra("EventID", event.getEventID());
        guestsIntent.setAction("GET_GUESTS");
        this.startService(guestsIntent);
    }

    private void displayGuests(ArrayList<HashMap<String, String>> guests) {
        int numGuests = guests.size();
        if(numGuests>1) {
            headerFriends.setText(numGuests + " Friends Are!");

        }else if(numGuests==1) {
            headerFriends.setText(numGuests + " Friend Is!");
        }else {
            headerFriends.setText("Be the First to RSVP!");
        }

        LinearLayout guestView = (LinearLayout)findViewById(R.id.layoutFaces);
        guestView.removeAllViews();

        for(int i = 0; i < numGuests; i++) {
            de.hdodenhof.circleimageview.CircleImageView face = new de.hdodenhof.circleimageview.CircleImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8,0,8,0);
            lp.height = 96;
            lp.width = 96;
            face.setLayoutParams(lp);
            guestView.addView(face);

            String faceURL = guests.get(i).get("avatar");
            Picasso.with(this).load(faceURL).error(R.drawable.image_error).resize(96, 96).centerCrop().into(face);
        }

    }

    @Override
    public void onResume() {
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
