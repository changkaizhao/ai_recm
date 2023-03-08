package com.HKJC.RatingCalculator;

public class BetTypeSelection extends Selection{
    public BetType bet_type;
    public float score;

    public BetTypeSelection(String mid, int lrn, BetType bt, float s) {
        super();
        this.meeting_id = mid;
        this.leg_rs_no = lrn;
        this.bet_type = bt;
        this.score = s;
    }
    @Override
    public String toString() {
        return "bet_type:" + this.bet_type + "leg_rs_no:" + this.leg_rs_no + "meeting_id: "+ this.meeting_id + "score: "+ this.score;
    }
}
