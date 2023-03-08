package com.HKJC.RatingCalculator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HorseSelection extends Selection {

    @JsonProperty
    public String card_no;

    @JsonIgnore
    public String brand_no;

    @JsonIgnore
    public int hrs_rating;

    @JsonIgnore
    public int win_odds_order;

    @JsonIgnore
    public float score;

    @JsonProperty
    public String group; // A1, A2, A3

    public HorseSelection(String mtid, String cn, int lrn, String bn, int hr, int woo, float s, String g) {
        super();
        this.meeting_id = mtid;
        this.card_no = cn;
        this.leg_rs_no = lrn;
        this.brand_no = bn;
        this.hrs_rating = hr;
        this.win_odds_order = woo;
        this.score = s;
        this.group = g;
    }

    @Override
    public String toString() {
        return "score:" + this.score + "brand_no:" + this.brand_no + "card_no:" + this.card_no + " leg_rs_no: "+ this.leg_rs_no+"   |  Group:" + this.group;
    }
    @Override
    public int hashCode(){
        return this.hrs_rating + this.leg_rs_no;
    }
    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        if (!(o instanceof HorseSelection)) {
            return false;
        }

        HorseSelection h = (HorseSelection) o;
        return this.meeting_id == h.meeting_id &&
                this.card_no.equals(h.card_no) &&
                this.leg_rs_no == h.leg_rs_no &&
                this.brand_no.equals(h.brand_no) &&
                this.hrs_rating == h.hrs_rating &&
                this.win_odds_order == h.win_odds_order &&
                Float.compare(this.score, h.score) == 0 &&
                this.group.equals(h.group);
    }
}
