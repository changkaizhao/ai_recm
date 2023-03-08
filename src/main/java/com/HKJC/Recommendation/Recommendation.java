package com.HKJC.Recommendation;

import com.HKJC.Main;
import com.HKJC.RaceSelector.RaceTimeProvider;
import com.HKJC.RatingCalculator.BetType;
import com.HKJC.RatingCalculator.HorseSelection;
import com.HKJC.Utils.DataFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Recommendation implements Serializable {
    @JsonProperty
    public String bet_acc_no;

    @JsonProperty
    public RecommendationInternalData recommendations;

    public static String mapper(HorseSelection h) {
        return h.card_no;
    }

    public Recommendation(String betacc) {
        this.bet_acc_no = betacc;
    }

    private boolean checkEqualSel(String[] a, String[] b){
        if(a.length != b.length) return false;
        for(int i = 0; i < a.length; i++){
            if(!Arrays.asList(b).contains(a[i])){
                return false;
            }
        }
        return true;
    }

    private String parseMtg(String mtg){
        String pattern = "(?<=MTG_)(\\w+)(?=_)";
        return DataFilter.find(mtg, pattern);
    }

    private ArrayList<RecoBet> convertRecoBet(String meetid, int race_no, String race_id, ArrayList<ScoreResult> hs,
            RaceTimeProvider rtp) {
        //remove duplicated...
        ArrayList<ScoreResult> cache = new ArrayList<>();
        Iterator<ScoreResult> it = hs.iterator();
        while(it.hasNext()){
            ScoreResult ele = it.next();
            if(ele.type == BetType.WIN || ele.type == BetType.PLA || ele.type == BetType.WP){
                AtomicBoolean found = new AtomicBoolean(false);
                cache.forEach(e->{
                    if(this.checkEqualSel(e.selres, ele.selres)){
                        found.set(true);
                    }
                });
                if(!found.get()){
                    cache.add(ele);
                }else{
                    it.remove();
                }
            }
        }

        ArrayList<RecoBet> betdatas = new ArrayList<>();
        for (int i = 0; i < hs.size(); i++) {
            String[] sel;
            if (hs.get(i).type == BetType.CWA) {
                sel = new String[] { hs.get(i).sel[0].group };
            } else {
                List<HorseSelection> sss = Arrays.asList(hs.get(i).sel);
                sel = sss.stream().map(e -> Recommendation.mapper(e)).toArray(String[]::new);
            }

            RecoPool p = new RecoPool(hs.get(i).type, sel);
            RecoBet bet;
            try{
               bet = new RecoBet(meetid, rtp.getVenue(this.parseMtg(meetid), race_no), race_id, false, race_no, p);
               betdatas.add(bet);
            }catch(Exception e){
                Logger logger = Logger.getLogger(Recommendation.class);
                logger.error(e);
            }
        }
        return betdatas;
    }

    public static Recommendation constructBetSlipAndConfirmReco(String meetid, String betacc, int race_no,
            ArrayList<ScoreResult> hs, RaceTimeProvider rtp, int betslip_num, int confirm_num, String race_id) throws Exception {
        Logger logger  = Logger.getLogger(Recommendation.class);
        long start = System.nanoTime();

        Recommendation rec = new Recommendation(betacc);
        ArrayList<RecoBet> betdatas = rec.convertRecoBet(meetid, race_no,race_id, hs, rtp);
        RecoBet[] betslip = betdatas.subList(0, betslip_num).toArray(RecoBet[]::new);
        // Bet[] bet_confirm = betdatas.subList(3, betdatas.size()).toArray(Bet[]::new);
        // currently we use same result on betslip and bet_confirm
        RecoBet[] bet_confirm = betdatas.subList(0, confirm_num).toArray(RecoBet[]::new);
        rec.recommendations = new RecommendationInternalData(betslip, bet_confirm);
        float time = (System.nanoTime() - start)/(float)1000000.0;
        logger.info("meetid:" + meetid +  " ⏰[ConstructionResponseTime]:" + time + "ms");
        return rec;
    }

    public static Recommendation constructBetTypeReco(String meetid, String betacc, int race_no,
            ArrayList<ScoreResult> hs, RaceTimeProvider rtp, String race_id)  {
        Logger logger  = Logger.getLogger(Recommendation.class);
        long start = System.nanoTime();
        Recommendation rec = new Recommendation(betacc);
        ArrayList<RecoBet> betdatas = rec.convertRecoBet(meetid, race_no, race_id, hs, rtp);
        RecoBet[] bettypes = betdatas.toArray(RecoBet[]::new);
        rec.recommendations = new RecommendationInternalData(bettypes);
        float time = (System.nanoTime() - start)/(float)1000000.0;
        logger.info("meetid:" + meetid +  " ⏰[ConstructionResponseTime]:" + time + "ms");
        return rec;
    }
}
