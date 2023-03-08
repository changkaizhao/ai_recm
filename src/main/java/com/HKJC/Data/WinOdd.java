package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WinOdd {
    @JsonProperty
    public String mtg_id;

    @JsonProperty
    public String status;

    @JsonProperty
    public int leg_rs_no;

    @JsonProperty
    public int card_no;

    @JsonProperty
    public String brand_no;

    @JsonProperty
    public float win_odds;

    @JsonProperty
    public int win_odds_order;

}
