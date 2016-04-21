package com.devon_dickson.apps.oncampus;

import com.orm.SugarRecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ddickson1 on 2/5/2016.
 */


public class Event extends SugarRecord{
    private String eventID;
    private String name;
    private String location;
    private String description;
    private String org;
    private long endTime;
    private long startTime;
    private String image;
    private String facebook;
    private String startTimeString;
    private String endTimeString;

    public Event() {}

    public Event(String eventID, String name, String location, String description, String org, long startTime, long endTime, String image, String facebook) {
        this.eventID = eventID;
        this.name = name;
        this.location = location;
        this.description = description;
        this.org = org;
        this.startTime = startTime;
        this.endTime = endTime;
        this.image = image;
        this.facebook = facebook;


        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.US);
        SimpleDateFormat parserSDF = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        try {
            Date startDate = parserSDF.parse(startTime+"");
            this.startTimeString = format.format(startDate);

            Date endDate = parserSDF.parse(endTime+"");
            endTimeString = format.format(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getEventID() {
        return eventID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getImage() {
        return image;
    }

    public String getFacebook() {
        return facebook;
    }

    public String getDescription() {
        return description;
    }

    public String getStartTimeString() {
        return startTimeString;
    }
}