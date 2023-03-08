package com.HKJC.SolMessager;

import com.HKJC.BetPool.Bet;
// import com.HKJC.Recommendation.RecommendRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class SolMessage implements Serializable {
    // public RecommendRequest message;
    @JsonIgnore
    public String bet_acc_no;

    @JsonIgnore
    public TopicHeader header;

    @JsonProperty
    public String meeting_id;

    @JsonProperty
    public String message;
    @JsonProperty
    public String request_id;

    // page: betslip or race_landing
    @JsonIgnore
    public String page;

    @JsonProperty
    public int betslip_display_num;

    @JsonProperty
    public int betconfirmation_display_num;

    @JsonProperty
    public Map<String, Integer> bettype_display_num;

    @JsonProperty
    public ArrayList<Bet> betfilter;

    @JsonProperty
    public int race_no;

    @JsonProperty
    public String race_id;

    @JsonProperty
    public String start_time;

    // public SolMessage(RecommendRequest message, String ID, String type) {
    // this.message = message;
    // this.ID = ID;
    // this.type = type;
    // }
    @Override
    public String toString(){
        return "bet_acc_no:" + bet_acc_no + " requestId:" + request_id;
     }
}
