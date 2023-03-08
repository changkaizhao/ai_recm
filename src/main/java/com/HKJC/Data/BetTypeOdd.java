package com.HKJC.Data;

import com.HKJC.RatingCalculator.BetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BetTypeOdd {
    @JsonProperty
    public String mtg_id;

    @JsonProperty
    public String acct_id;

    @JsonProperty
    public String betting_acct_no;

    @JsonProperty
    public BetType bet_type;

    @JsonProperty
    public int leg_rs_no;

    @JsonProperty
    public int win_odds_top1_range;

    @JsonProperty
    public float predict;

}
