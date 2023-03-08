package com.HKJC.RatingCalculator;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Selection {
    @JsonIgnore
    public String meeting_id = "";

    @JsonIgnore
    public int leg_rs_no = -1;
}
