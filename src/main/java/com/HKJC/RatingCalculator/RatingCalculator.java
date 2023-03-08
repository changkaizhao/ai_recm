package com.HKJC.RatingCalculator;

import com.HKJC.BetPool.BetFilter;
import com.HKJC.BetPool.Pool;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 *
 */
public class RatingCalculator {
    private HorseSelection[] horseSelections;
    private BetTypeSelection[] betTypeSelections;
    public String meeting_id;
    // public int leg_rs_no;

    public RatingCalculator(HorseSelection[] hs, BetTypeSelection[] bs) {
        this.meeting_id = hs[0].meeting_id;
        this.horseSelections = hs;
        this.betTypeSelections = bs;
    }

    // for one horse situation
    private RatingResult oneHorseRating(HorseSelection[] hsel, BetTypeSelection[] bsel, BetFilter bf, Boolean filter,
            int legNo) throws Exception {
        float maxScore = 0;
        BetType maxType = BetType.UNDEFINED;
        HorseSelection[] hs = new HorseSelection[1];
        try {
            ArrayList<BetTypeSelection> tps = new ArrayList<BetTypeSelection>();
            for (int i = 0; i < bsel.length; i++) {
                BetType b = bsel[i].bet_type;
                if (b == BetType.WIN || b == BetType.PLA || b == BetType.WP) {
                    tps.add(bsel[i]);
                }
            }
            for (int i = 0; i < hsel.length; i++) {
                for (int j = 0; j < tps.size(); j++) {
                    BetType t = tps.get(j).bet_type;
                    float newScore = hsel[i].score * tps.get(j).score;
                    if (newScore > maxScore) {
                        if (filter) {
                            Boolean found = false;
                            for (int k = 0; k < bf.filter_bets.size(); k++) {
                                Pool p = bf.filter_bets.get(k).pool;
                                if (p.pool_type == t) {
                                    for (int pi = 0; pi < p.sel.length; pi++) {
                                        if (p.sel[pi] == hsel[i].card_no) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (found) {
                                continue;
                            }
                        }
                        maxScore = newScore;
                        maxType = tps.get(j).bet_type;
                        hs[0] = hsel[i];
                    }
                }
            }

            return new RatingResult(this.meeting_id, legNo, maxType, maxScore, hs);
        } catch (Exception e) {
            throw e;
        }
    }

    // some bet type need to consider orders.
    private Boolean filterCheck(HorseSelection[] hsel, BetFilter bf, BetType b, int[] indexes) {
        for (int k = 0; k < bf.filter_bets.size(); k++) {
            Pool p = bf.filter_bets.get(k).pool;
            if (p.pool_type == b) {
                for (int pi = 0; pi < p.sel.length; pi++) {
                    // break selection
                    String[] sel = p.sel[pi].split("-");

                    Boolean contain = true;
                    for (int i = 0; i < indexes.length; i++) {
                        // ignore orders...
                        // add order filter in future
                        if (!Arrays.asList(sel).contains(hsel[indexes[i]])) {
                            contain = false;
                            break;
                        }
                    }
                    if (contain) {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }

    // for two - four horses situation
    private RatingResult multiHorseRating(HorseSelection[] hsel, BetTypeSelection[] bsel, BetFilter bf, Boolean filter,
            int legNo) throws Exception {
        float maxScore = 0;
        BetType maxType = BetType.UNDEFINED;
        List<HorseSelection> hs = new ArrayList<HorseSelection>();
        int Len = hsel.length;

        try {
            // ArrayList<BetTypeSelection> tps = new ArrayList<BetTypeSelection>();
            for (int i = 0; i < bsel.length; i++) {
                BetType b = bsel[i].bet_type;
                if (b == BetType.QIN || b == BetType.QPL || b == BetType.QQP
                        || b == BetType.FCT) {
                    int N = 2;
                    for (int c1 = 0; c1 < Len - 1; c1++) {
                        for (int c2 = c1 + 1; c2 < Len; c2++) {
                            float newScore = (hsel[c1].score + hsel[c2].score) * bsel[i].score / (float) N;
                            if (newScore > maxScore) {
                                if (filter) {
                                    int[] indexes = { c1, c2 };
                                    if (this.filterCheck(hsel, bf, b, indexes)) {
                                        continue;
                                    }
                                }
                                maxScore = newScore;
                                maxType = b;
                                hs.clear();
                                hs.add(hsel[c1]);
                                hs.add(hsel[c2]);
                            }
                        }
                    }
                } else if (b == BetType.TCE || b == BetType.TRI) {
                    int N = 3;
                    for (int c1 = 0; c1 < Len - 2; c1++) {
                        for (int c2 = c1 + 1; c2 < Len - 1; c2++) {
                            for (int c3 = c2 + 1; c3 < Len; c3++) {
                                float newScore = (hsel[c1].score + hsel[c2].score + hsel[c3].score) * bsel[i].score
                                        / (float) N;
                                if (newScore > maxScore) {
                                    if (filter) {
                                        int[] indexes = { c1, c2, c3 };
                                        if (this.filterCheck(hsel, bf, b, indexes)) {
                                            continue;
                                        }
                                    }
                                    maxScore = newScore;
                                    maxType = b;
                                    hs.clear();
                                    hs.add(hsel[c1]);
                                    hs.add(hsel[c2]);
                                    hs.add(hsel[c3]);
                                }
                            }
                        }
                    }
                } else if (b == BetType.F_F || b == BetType.QTT) {
                    int N = 4;
                    for (int c1 = 0; c1 < Len - 3; c1++) {
                        for (int c2 = c1 + 1; c2 < Len - 2; c2++) {
                            for (int c3 = c2 + 1; c3 < Len - 1; c3++) {
                                for (int c4 = c3 + 1; c4 < Len; c4++) {
                                    float newScore = (hsel[c1].score + hsel[c2].score + hsel[c3].score + hsel[c4].score)
                                            * bsel[i].score / (float) N;
                                    if (newScore > maxScore) {
                                        if (filter) {
                                            int[] indexes = { c1, c2, c3, c4 };
                                            if (this.filterCheck(hsel, bf, b, indexes)) {
                                                continue;
                                            }
                                        }
                                        maxScore = newScore;
                                        maxType = b;
                                        hs.clear();
                                        hs.add(hsel[c1]);
                                        hs.add(hsel[c2]);
                                        hs.add(hsel[c3]);
                                        hs.add(hsel[c4]);
                                    }
                                }

                            }
                        }
                    }
                }

            }

            return new RatingResult(this.meeting_id, legNo, maxType, maxScore, hs.toArray(new HorseSelection[0]));
        } catch (Exception e) {
            throw e;
        }
    }

    // for all horse situation
    private RatingResult allHorseRating(HorseSelection[] hsel, BetTypeSelection[] bsel, BetFilter bf, Boolean filter,
            int legNo) throws Exception {
        float maxScore = 0;
        BetType maxType = BetType.UNDEFINED;
        String group = "";
        try {
            ArrayList<BetTypeSelection> tps = new ArrayList<BetTypeSelection>();

            for (int i = 0; i < bsel.length; i++) {
                BetType b = bsel[i].bet_type;
                if (b == BetType.CWA) {
                    tps.add(bsel[i]);
                }
            }
            for (int i = 0; i < hsel.length; i++) {
                for (int j = 0; j < tps.size(); j++) {
                    float newScore = hsel[i].score * tps.get(j).score;
                    if (newScore > maxScore) {
                        if (filter) {
                            Boolean found = false;
                            for (int k = 0; k < bf.filter_bets.size(); k++) {
                                Pool p = bf.filter_bets.get(k).pool;
                                if (p.pool_type == tps.get(j).bet_type) {
                                    for (int pi = 0; pi < p.sel.length; pi++) {
                                        if (p.sel[pi] == hsel[i].group) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (found) {
                                continue;
                            }
                        }
                        maxScore = newScore;
                        maxType = tps.get(j).bet_type;
                        group = hsel[i].group;
                    }
                }
            }
            List<HorseSelection> hs = new ArrayList<HorseSelection>();
            for (int i = 0; i < hsel.length; i++) {
                if (hsel[i].group == group) {
                    hs.add(hsel[i]);
                }
            }
            return new RatingResult(this.meeting_id, legNo, maxType, maxScore, hs.toArray(new HorseSelection[0]));
        } catch (Exception e) {
            throw e;
        }
    }

    private <T> T[] selectLegNo(T[] sel, Class<T> c, int legNo) {
        ArrayList<T> s = new ArrayList<T>();
        for (int i = 0; i < sel.length; i++) {
            if (((Selection) sel[i]).leg_rs_no == legNo) {
                s.add(sel[i]);
            }
        }
        T[] r = (T[]) Array.newInstance(c, s.size());
        s.toArray(r);
        return r;
    }

    public RatingResult maxScore(int leg_no, BetFilter bf, Boolean prefilter) throws Exception {
        try {
            HorseSelection[] hs = this.selectLegNo(this.horseSelections, HorseSelection.class, leg_no);
            BetTypeSelection[] bs = this.selectLegNo(this.betTypeSelections, BetTypeSelection.class, leg_no);
            RatingResult oneR = this.oneHorseRating(hs, bs, bf, prefilter, leg_no);
            RatingResult mulR = this.multiHorseRating(hs, bs, bf, prefilter, leg_no);
            RatingResult allR = this.allHorseRating(hs, bs, bf, prefilter, leg_no);
            RatingResult r = oneR.score > mulR.score ? (oneR.score > allR.score ? oneR : allR)
                    : (mulR.score > allR.score ? mulR : allR);

            // now we don't need post-filtering
            // if(!prefilter){
            // // not implement
            // }
            return r;
        } catch (Exception e) {
            throw e;
        }
    }
}
