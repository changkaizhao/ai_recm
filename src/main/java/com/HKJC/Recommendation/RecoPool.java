package com.HKJC.Recommendation;

import com.HKJC.RatingCalculator.BetType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class RecoPool implements Serializable {
    @JsonProperty
    public BetType pool_type;

    @JsonProperty
    public String[] sel;

    public RecoPool(BetType pt, String[] sel) {
        this.pool_type = pt;
        this.sel = sel;
    }
}
