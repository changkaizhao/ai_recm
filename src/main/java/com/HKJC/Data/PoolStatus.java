package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PoolStatus {
    @JsonProperty("WIN")
    public PoolStatusCode WIN;
    @JsonProperty("PLA")
    public PoolStatusCode PLA;
    @JsonProperty("W-P")
    public PoolStatusCode WP;
    @JsonProperty("QIN")
    public PoolStatusCode QIN;
    @JsonProperty("QPL")
    public PoolStatusCode QPL;
    @JsonProperty("QQP")
    public PoolStatusCode QQP;
    @JsonProperty("FCT")
    public PoolStatusCode FCT;
    @JsonProperty("TCE")
    public PoolStatusCode TCE;
    @JsonProperty("TRI")
    public PoolStatusCode TRI;
    @JsonProperty("F-F")
    public PoolStatusCode F_F;
    @JsonProperty("QTT")
    public PoolStatusCode QTT;
    @JsonProperty("CWA")
    public PoolStatusCode CWA;
}
