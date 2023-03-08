package com.HKJC.RaceSelector;

import java.util.Calendar;
import java.util.Date;

import com.HKJC.Config.Configurator;
import com.HKJC.Exceptions.HKJCException;

public class RaceTimeProvider {
    // sorted race date times
    public RaceTime[] data;

    public RaceTimeProvider(RaceTime[] d) {
        this.data = new RaceTime[d.length];
        // sort race time by time
        for (int i = 0; i < d.length; i++) {
            RaceTime curD = d[i];
            for (int j = 0; j < data.length; j++) {
                if (data[j] == null) {
                    data[j] = curD;
                    break;
                }
                if (curD.start_time.before(data[j].start_time)) {
                    // renew value
                    for (int k = data.length - 1; k > j; k--) {
                        data[k] = data[k - 1];
                    }
                    data[j] = curD;
                    break;
                }
            }
        }
    }

    public String getVenue(String mtg, int race_no) throws Exception {
        for (int i = 0; i < this.data.length; i++) {
            if (this.data[i].mtg_id.equals(mtg) && this.data[i].leg_rs_no == race_no) {
                return this.data[i].venCode;
            }
        }
        throw new Exception("Venue not found, in mtg:" + mtg + " race: " + race_no);
    }



    public int getInterval(Date d) throws Exception {
        // if (Configurator.getInstance().Debug) {
        // Calendar today = Calendar.getInstance();
        // // modify each ract time to today
        // for (int i = 0; i < this.data.length; i++) {
        // Calendar startTime = Calendar.getInstance();
        // startTime.setTime(this.data[i].start_time);
        // startTime.set(Calendar.YEAR, today.get(Calendar.YEAR));
        // startTime.set(Calendar.MONTH, today.get(Calendar.MONTH));
        // startTime.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));
        // this.data[i].start_time = startTime.getTime();
        // }
        // }
        int len = this.data.length;
        Calendar c = Calendar.getInstance();
        c.setTime(this.data[0].start_time);
        // Perform addition/subtraction
        c.add(Calendar.MINUTE, -30);
        // Convert calendar back to Date

        // for interval 0 , ahead first race 30min
        if (d.before(c.getTime())) {
            return 0;
        }

        for (int i = 0; i < len; i++) {
            if (d.before(this.data[i].start_time)) {
                return this.data[i].leg_rs_no;
            }
        }
        throw new HKJCException(400,
                "Start time are not scheduled, please input a date before " + this.data[len - 1].start_time);
    }
}
