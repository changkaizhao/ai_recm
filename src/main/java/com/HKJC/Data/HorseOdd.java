package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HorseOdd {
    @JsonProperty
    public String mtg_id;

    @JsonProperty
    public String acct_id;

    @JsonProperty
    public String card_no;

    @JsonProperty
    public int leg_rs_no;

    @JsonProperty
    public String brand_no;

    @JsonProperty
    public String betting_acct_no;

    @JsonProperty
    public int win_odds_order;

    @JsonProperty
    public float predict;

}
