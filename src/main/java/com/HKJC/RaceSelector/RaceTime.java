package com.HKJC.RaceSelector;

// Race Time info
// format as below
//        {
//        "mtg_id": "20210609",
//        "leg_rs_no": "1",
//        "start_time": "2021-06-09T14:40:00+08:00"
//        }

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;

public class RaceTime {
        @JsonProperty("mtg_id")
        public String mtg_id;

        @JsonProperty("leg_rs_no")
        public int leg_rs_no;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
        public Date start_time;

        @JsonProperty("venCode")
        public String venCode;

        public static final String TimePattern = "yyyy-MM-dd'T'HH:mm:ssX";

        public static Date convertDate(String s) throws Exception {
                SimpleDateFormat d = new SimpleDateFormat(RaceTime.TimePattern);
                return d.parse(s);
        }

        // all kinds of constructors
        public RaceTime(String mtg_id, String leg_rs_no, Date start_time, String venCode) {
                this.mtg_id = mtg_id;
                this.leg_rs_no = Integer.valueOf(leg_rs_no);
                this.start_time = start_time;
                this.venCode = venCode;
        }

        public RaceTime(String mtg_id, int leg_rs_no, Date start_time, String venCode) {
                this(mtg_id, String.valueOf(leg_rs_no), start_time, venCode);
        }

        public RaceTime(String mtg_id, int leg_rs_no, String start_time, String venCode) throws Exception {
                this(mtg_id, String.valueOf(leg_rs_no), RaceTime.convertDate(start_time), venCode);
        }

        public RaceTime(String mtg_id, String leg_rs_no, String start_time, String venCode) throws Exception {
                this(mtg_id, leg_rs_no, RaceTime.convertDate(start_time), venCode);
        }

        @Override
        public String toString() {
                return this.mtg_id + " " + this.leg_rs_no + " " + this.start_time + " " + this.venCode;
        }
}
