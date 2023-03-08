package com.HKJC.RaceSelector;

import com.HKJC.Utils.DataFilter;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class RaceSelector {
    private List<RaceIntervalInfo> data;

    public RaceSelector(List<RaceIntervalInfo> data) {
        this.data = data;
    }

    public int select(String mtg, String betting_id, int interval, List<Integer> raceFilter) {
        try {
            return RaceSelector.select(this.data, interval, raceFilter);
        } catch (Exception e) {
            Logger logger = Logger.getLogger(RaceSelector.class);
            logger.error(e + " mtg:" + mtg + " betting_co:" + betting_id + " interval:"+interval);
            throw e;
        }
    }


    public static int select(List<RaceIntervalInfo> data, int interval, List<Integer> raceFilter) {
        List<RaceIntervalInfo> r = data.stream()
                .filter((RaceIntervalInfo ri) -> ri.interval == interval)
                .collect(Collectors.toList());

        int s = r.size();

        List<Integer> valueArr = new ArrayList<>();
        List<Double> probArr = new ArrayList<>();

        for (int i = 0; i < s; i++) {
            int rn = r.get(i).leg_rs_no;
            if(raceFilter.contains(rn)){
                valueArr.add(rn);
                probArr.add(r.get(i).probability);
            }
        }

        int[] values = valueArr.stream().mapToInt(i->i).toArray();
        double[] prob = probArr.stream().mapToDouble(i->i).toArray();

        EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(values, prob);
        return dist.sample();
    }
}
