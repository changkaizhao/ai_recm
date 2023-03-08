package com.HKJC.Recommendation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

public class RecommendationInternalData implements Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RecoBet[] betslip;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RecoBet[] bet_confirmation;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public RecoBet[] bet_type;

    public RecommendationInternalData(RecoBet[] betslip, RecoBet[] bet_confirm) {
        this.betslip = betslip;
        this.bet_confirmation = bet_confirm;
    }

    public RecommendationInternalData(RecoBet[] allScores) {
        this.bet_type = allScores;
    }

}