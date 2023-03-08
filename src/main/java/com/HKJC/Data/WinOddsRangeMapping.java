package com.HKJC.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WinOddsRangeMapping {
    @JsonProperty
    public String odds_range;

    @JsonProperty
    public int win_odds_top1_range;

    private float maxR = -1;
    private float minR = -1;


    private float[] splitRange() throws Exception {
        String[] rs = this.odds_range.split("-");
        if (rs.length != 2) {
            throw new Exception("Range values count is not equal to 2");
        }
        float[] r = new float[rs.length];
        r[0] = Float.parseFloat(rs[0]);
        r[1] = Float.parseFloat(rs[1]);
        return r;
    }

    public float maxV() throws Exception {
        if (this.maxR < 0) {
            float[] r = this.splitRange();
            this.maxR = r[1];
        }
        return this.maxR;

    }

    public float minV() throws Exception {
        if (this.minR < 0) {
            float[] r = this.splitRange();
            this.minR = r[0];
        }
        return this.minR;
    }

    public boolean in(float v) throws Exception {
        if (v <= this.maxV() && v >= this.minV()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "odds_range" + this.odds_range + "\n" + "win_odds_top1_range:" + this.win_odds_top1_range + "\n";
    }
}
