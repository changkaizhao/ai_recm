package com.HKJC.RatingCalculator;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum BetType {
    @JsonEnumDefaultValue
    UNDEFINED,
    @JsonProperty("WIN_PLA_W-P")
    WINPLAWP,
    @JsonProperty("WIN")
    WIN,
    @JsonProperty("PLA")
    PLA,
    @JsonProperty("W-P")
    WP,
    @JsonProperty("QIN")
    QIN,
    @JsonProperty("QPL")
    QPL,
    @JsonProperty("QQP")
    QQP,
    @JsonProperty("FCT")
    FCT,
    @JsonProperty("TCE")
    TCE,
    @JsonProperty("TRI")
    TRI,
    @JsonProperty("F-F")
    F_F,
    @JsonProperty("QTT")
    QTT,
    @JsonProperty("CWA")
    CWA
}