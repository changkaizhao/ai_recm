package com.HKJC.BetPool;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BetFilter implements Serializable {
    @JsonProperty
    public String bet_acc_no;

    @JsonProperty
    public ArrayList<Bet> filter_bets;

    public BetFilter(){
    }
    public BetFilter(String bet_acc_no, ArrayList<Bet> filter_bets) {
        this.bet_acc_no = bet_acc_no;
        this.filter_bets = filter_bets;

    }
}
