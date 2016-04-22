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
    private long endInt;
    private long startInt;
    private String endTime;
    private String startTime;
    private String image;
    private String facebook;

    public Event() {}

    public Event(String eventID, String name, String location, String description, String org, long startInt, String startTime, long endInt, String endTime, String image, String facebook) {
        this.eventID = eventID;
        this.name = name;
        this.location = location;
        this.description = description;
        this.org = org;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startInt = startInt;
        this.endInt = endInt;
        this.image = image;
        this.facebook = facebook;
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

    public String getStartTime() {
        return startTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getEndTime() {
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

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public long getEndInt() {
        return endInt;
    }

    public void setEndInt(long endInt) {
        this.endInt = endInt;
    }

    public long getStartInt() {
        return startInt;
    }

    public void setStartInt(long startInt) {
        this.startInt = startInt;
    }
}