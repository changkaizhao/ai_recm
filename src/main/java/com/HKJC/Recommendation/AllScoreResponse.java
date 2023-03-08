package com.HKJC.Recommendation;

import com.HKJC.RatingCalculator.BetType;

import java.io.Serializable;
import java.util.Map;

public class AllScoreResponse implements Serializable {
    public String meeting_id;
    public int race_no;
    public  Map<BetType,  ScoreResult[]> data;

    public AllScoreResponse(String mtg, int race_no, Map<BetType,  ScoreResult[]> data) {
        this.meeting_id = mtg;
        this.race_no = race_no;
        this.data = data;
    }
}
