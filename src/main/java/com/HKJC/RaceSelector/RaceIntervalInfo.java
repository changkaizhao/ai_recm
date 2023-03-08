package com.HKJC.RaceSelector;

public class RaceIntervalInfo {
    public  String mtg_id;
    public  String betting_id;
    public  int interval;
    public  int leg_rs_no;
    public  double probability;

    public RaceIntervalInfo(String mtg_id, String betting_id, int interval, int leg_rs_no, double probability){
        this.mtg_id = mtg_id;
        this.betting_id = betting_id;
        this.interval = interval;
        this.leg_rs_no = leg_rs_no;
        this.probability = probability;
    }
}
