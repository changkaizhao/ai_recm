package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CWAGroup {
    @JsonProperty
    public String mtg_id;

    @JsonProperty
    public int leg_rs_no;

    @JsonProperty
    public String card_no;

    @JsonProperty
    public String group;

}
