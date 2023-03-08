package com.HKJC.Recommendation;


public class GlobalAttrRaceInterval {
    private static GlobalAttrRaceInterval instance = null;

    public int race_interval;

    public static GlobalAttrRaceInterval getInstance() {
        if (instance == null) {
            instance = new GlobalAttrRaceInterval();
        }
        return instance;
    }

    public String toString(){
        return String.format("\"race_interval\":%d", this.race_interval);
    }
}
