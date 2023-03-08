package com.HKJC.Recommendation;

import java.io.Serializable;
import java.util.Map;

import com.HKJC.BetPool.BetFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RecommendRequest implements Serializable {

    @JsonProperty
    public BetFilter betfilter;

    public String messageID;

    public String bettingID;

    public String meetingID;

    public int raceNo;

    public String race_id;

    @JsonProperty("start_time")
    public String startTime;

    @JsonProperty
    public int betslip_display_num;

    @JsonProperty
    public int betconfirmation_display_num;

    @JsonProperty
    public Map<String, Integer> bettype_display_num;
}
