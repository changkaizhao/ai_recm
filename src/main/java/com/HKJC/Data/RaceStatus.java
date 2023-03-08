package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RaceStatus {
    @JsonProperty
    public int race_no;

    @JsonProperty
    public RaceStatusCode status;

    @JsonProperty
    public PoolStatus pool_status;
}
