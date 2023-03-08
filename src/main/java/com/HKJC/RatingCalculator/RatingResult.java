package com.HKJC.RatingCalculator;

import java.util.Arrays;
import java.util.List;

public class RatingResult {
    public String meeting_id;
    public int leg_rs_no;
    public BetType bet_type;
    public float score;
    public HorseSelection[] selections;

    public RatingResult(String mid, int lid, BetType bt, float s, HorseSelection[] hs) {
        this.meeting_id = mid;
        this.leg_rs_no = lid;
        this.bet_type = bt;
        this.score = s;
        this.selections = hs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < this.selections.length; i++) {
            str.append("\n         ");
            str.append(this.selections[i].toString());
        }
        return "Meeting_id:" + this.meeting_id + "\nLeg_no:" + this.leg_rs_no + "\nBet type:" + this.bet_type
                + "\nScore:" + this.score + "\n  Selections:" + str;
    }

    private boolean equalHorseSelection(HorseSelection[] hs) {
        if (hs.length != this.selections.length) {
            return false;
        }

        List<HorseSelection> tmp = Arrays.asList(hs);
        for (int i = 0; i < this.selections.length; i++) {
            boolean found = false;
            for (int j = 0; j < tmp.size(); j++) {
                if (this.selections[i].equals(tmp.get(j))) {
                    found = true;
                    // remove item
                    tmp.remove(j);
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode(){
        return this.leg_rs_no;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof RatingResult)) {
            return false;
        }

        RatingResult h = (RatingResult) o;
        return this.meeting_id.equals(h.meeting_id) &&
                this.leg_rs_no == h.leg_rs_no &&
                this.bet_type == h.bet_type &&
                Float.compare(this.score, h.score) == 0 &&
                this.equalHorseSelection(h.selections);
    }
}
