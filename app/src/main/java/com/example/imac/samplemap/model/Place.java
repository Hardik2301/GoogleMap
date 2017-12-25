package com.example.imac.samplemap.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by imac on 2/25/17.
 */

@Entity
public class Place {

    @PrimaryKey(autoGenerate = true)
    private String id;
    private String name;
    private String latitude;
    private String longitute;
    private String address;

    public Place(String id, String name, String latitude, String longitute, String address) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitute = longitute;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitute() {
        return longitute;
    }

    public void setLongitute(String longitute) {
        this.longitute = longitute;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
