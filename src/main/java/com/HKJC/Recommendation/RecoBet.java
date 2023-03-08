package com.HKJC.Recommendation;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RecoBet implements Serializable {
    @JsonProperty
    public String venue;

    @JsonProperty
    public String meeting_id;

    @JsonProperty
    public String race_id;

    @JsonProperty
    public int race_no;

//    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnore
    public boolean is_flexi;

    @JsonProperty
    public RecoPool pool;

    public RecoBet(String meeting_id, String venue, String race_id, boolean is_flexi, int race_no, RecoPool pool) {
        this.meeting_id = meeting_id;
        this.venue = venue;
        this.race_id = race_id;
        this.race_no = race_no;
        this.is_flexi = is_flexi;
        this.pool = pool;
    }

}