package com.HKJC.Recommendation;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.HKJC.RatingCalculator.BetType;
import com.HKJC.RatingCalculator.HorseSelection;

public class ScoreResult implements Serializable {
    @JsonProperty
    public float score;

    @JsonProperty
    public BetType type;

    @JsonIgnore
    public HorseSelection[] sel;

    @JsonProperty("sel")
    public String[] selres;

    // whether check sel order, some bettype has order
    @JsonIgnore
    public boolean orderCheck;

    public BetType getType() {
        return this.type;
    }

    public ScoreResult(float score, BetType bt, HorseSelection[] sel, boolean orderCheck) {
        this.score = score;
        this.type = bt;
        this.sel = sel;
        this.orderCheck = orderCheck;

        List<String> s = new ArrayList<String>();
        for(int i = 0; i < sel.length; i++){
            if(type == BetType.CWA){
                s.add(sel[i].group);
            }else{
                s.add(sel[i].card_no);
            }
        }
        this.selres = s.toArray(String[]::new);
    }
}