package com.HKJC.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RaceTimeData {
    @JsonProperty("mtg_id")
    public String mtg_id;

    @JsonProperty("leg_rs_no")
    public int leg_rs_no;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    public Date start_time;

    @JsonProperty("venCode")
    public String venCode;
}
