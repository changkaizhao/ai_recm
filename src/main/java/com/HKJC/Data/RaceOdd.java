package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class RaceOdd {
    @JsonProperty
    public String mtg_id;

    @JsonProperty
    public String acct_id;
    @JsonProperty
    public String betting_acct_no;

    @JsonProperty
    public int prox_intvl;

    @JsonProperty
    public int leg_rs_no;

    @JsonProperty
    public int win_odds_top1_range;

    @JsonProperty
    public float predict;

    @Override
    public String toString() {
        return "interval: " + this.prox_intvl + "   race_no: " + this.leg_rs_no + " prop: " + this.predict + "\n";
    }
}
