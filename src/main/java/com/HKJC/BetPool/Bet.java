package com.HKJC.BetPool;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bet implements Serializable {
    @JsonProperty
    public String meeting_id;

    @JsonProperty
    public String race_id;

    @JsonProperty
    public String venue;

    @JsonProperty
    public int race_no;

    // @JsonProperty
    // public int is_cross_allup;

    // @JsonProperty
    // public int is_allup;

    // @JsonProperty
    // public int is_flexi;

    @JsonProperty
    public Pool pool;

    // public Bet(String meeting_id, int is_cross_allup, int is_allup, int is_flexi,
    // Pool pool) {
    // this.meeting_id = meeting_id;
    // this.is_cross_allup = is_cross_allup;
    // this.is_allup = is_allup;
    // this.is_flexi = is_flexi;
    // this.pool = pool;
    // }
    public Bet(){
    }
    public Bet(String meeting_id, String race_id, String venue, int race_no, Pool pool) {
        this.meeting_id = meeting_id;
        this.race_id = race_id;
        this.venue = venue;
        this.race_no = race_no;
        this.pool = pool;
    }
}
