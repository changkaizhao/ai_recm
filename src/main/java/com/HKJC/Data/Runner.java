package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Runner {
    @JsonProperty
    public String mtg_id;

    @JsonProperty
    public int leg_rs_no;

    @JsonProperty
    public RunnerStatus hrs_status;

    @JsonProperty
    public String card_no;

    @JsonProperty
    public String barrier_draw_no;

    @JsonProperty
    public int rating;

    @JsonProperty
    public String brand_no;

    @JsonProperty
    public String jky_code;

    @JsonProperty
    public String trn_code;
}
