package com.HKJC.BetPool;

import com.HKJC.RatingCalculator.BetType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Pool implements Serializable {
    @JsonProperty
    public BetType pool_type;

    @JsonProperty
    public int race_no;

    @JsonProperty
    public String[] sel;
    public Pool() {
    }
    public Pool(BetType pt, int rn, String[] sel) {
        this.pool_type = pt;
        this.race_no = rn; 
        this.sel = sel;
    }
}
