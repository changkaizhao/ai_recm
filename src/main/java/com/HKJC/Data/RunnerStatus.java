package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RunnerStatus {
    @JsonEnumDefaultValue
    UNDEFINED,

    @JsonProperty
    Declared,
}
