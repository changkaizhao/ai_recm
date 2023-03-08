package com.HKJC.Recommendation;

import com.HKJC.BetPool.BetFilter;
import com.HKJC.BetPool.Pool;
import com.HKJC.Data.*;
import com.HKJC.RaceSelector.RaceSelector;
import com.HKJC.RaceSelector.RaceTimeProvider;
import com.HKJC.RatingCalculator.*;
import com.HKJC.SolMessager.RecomendDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class ScoreResultCmp implements Comparator<ScoreResult> {
    public int compare(ScoreResult s1, ScoreResult s2) {
        if (Float.compare(s1.score , s2.score) == 0){
            // sort bettype order
            int c = s1.type.name().compareToIgnoreCase(s2.type.name());
            if(c == 0 ){
                if(s1.type == BetType.CWA){
                    return c;
                }
                // sort res
                int[] s1_int = new int[s1.selres.length];
                int[] s2_int = new int[s2.selres.length];
                for(int i = 0;i < s1.selres.length;i++){
                    s1_int[i] = Integer.parseInt(s1.selres[i]);
                }
                for(int i = 0;i < s2.selres.length;i++){
                    s2_int[i] = Integer.parseInt(s2.selres[i]);
                }
                Arrays.sort(s1_int);
                Arrays.sort(s2_int);
                StringJoiner s1_res = new StringJoiner("");
                for (int i : s1_int) {
                    s1_res.add(Integer.toString(i));
                }
                StringJoiner s2_res = new StringJoiner("");
                for (int i : s2_int) {
                    s2_res.add(Integer.toString(i));
                }
                return s1_res.toString().compareToIgnoreCase(s2_res.toString());
            }else{
                return c;
            }
        }
        else if (Float.compare(s1.score , s2.score) > 0)
            return -1;
        else
            return 1;
    }
}

public class BetRecommendationCalculator {
    private HorseSelection[] horseSelections;
    private BetTypeSelection[] betTypeSelections;

   private RecomendDataProvider rdp;
    private RaceTimeProvider rtp;
    private RaceSelector rs;

    private String mtg_id;
    private String bet_acc;

    // output top N max score results if -1 return all
    private int TopN = 6;
    private final boolean GlobalFilter = false;

    // construct

    public BetRecommendationCalculator(String mt_id, String bet_acc, RecomendDataProvider rdp) throws Exception{
        try{
            this.horseSelections =  rdp.genHorseSelData();
            this.betTypeSelections = rdp.genBetTypeSelData();
            this.rdp =  rdp;
            this.rtp = rdp.genRaceTimeProvider();
        }catch (Exception e) {
            Logger logger = Logger.getLogger(BetRecommendationCalculator.class);
            logger.error(e);
            throw new Exception("BetRecommendationCalculator constructor failed");
        }
        this.mtg_id = mt_id;
        this.bet_acc = bet_acc;
    }

    private <T> T[] selectLegNo(T[] sel, Class<T> c, int legNo) {
        ArrayList<T> s = new ArrayList<T>();
        for (int i = 0; i < sel.length; i++) {
            Selection selData = (Selection) sel[i];
            if (selData.leg_rs_no == legNo) {
                s.add(sel[i]);
            }
        }
        T[] r = (T[]) Array.newInstance(c, s.size());
        s.toArray(r);
        return r;
    }



//    private BetTypeSelection[] betTypeFilter(BetTypeSelection[] sel, int race_no){
//
//        RaceStatus rs = this.rdp.getRaceStatus(race_no);
//        if(rs == null){
//            return new BetTypeSelection[]{};
//        }
//
//
//        ArrayList<BetTypeSelection> bts = new ArrayList<>();
//        for(int i = 0 ;i < sel.length; i++){
//            BetType bt = sel[i].bet_type;
//            try{
//                Field f = PoolStatus.class.getField(bt.toString());
//                PoolStatusCode psc =  (PoolStatusCode) f.get(rs.pool_status);
//                if(psc == PoolStatusCode.StartSell){
//                    bts.add(sel[i]);
//                }
//            }catch (Exception e){
//                continue;
//            }
//        }
//
//        BetTypeSelection[] bsel = (BetTypeSelection[]) Array.newInstance(BetTypeSelection.class, bts.size());
//        bts.toArray(bsel);
//        return bsel;
//    }

    private void addToGroup(ArrayList<HorseSelection> a, ArrayList<HorseSelection[]> group) {
        a.sort(new HorseSelectionCmp());
        HorseSelection[] A1 = (HorseSelection[]) Array.newInstance(HorseSelection.class, a.size());
        a.toArray(A1);
        group.add(A1);
    }

    private ArrayList<HorseSelection[]> group(HorseSelection[] hs) {
        String[] groupNames = new String[] { "A1", "A2", "A3" };
        ArrayList<HorseSelection[]> groups = new ArrayList<HorseSelection[]>();
        ArrayList<HorseSelection> a1 = new ArrayList<>();
        ArrayList<HorseSelection> a2 = new ArrayList<>();
        ArrayList<HorseSelection> a3 = new ArrayList<>();

        for (int i = 0; i < hs.length; i++) {
            if (hs[i].group.equals(groupNames[0])) {
                a1.add(hs[i]);
            } else if (hs[i].group.equals(groupNames[1])) {
                a2.add(hs[i]);
            } else if (hs[i].group.equals(groupNames[2])) {
                a3.add(hs[i]);
            }
        }
        if(a1.size() > 0){
            this.addToGroup(a1, groups);
        }
        if(a2.size() > 0){
            this.addToGroup(a2, groups);
        }
        if(a3.size() > 0){
            this.addToGroup(a3, groups);
        }

        return groups;
    }

    private ArrayList<ScoreResult> calcOneHorse(HorseSelection[] hs, BetTypeSelection[] bsel) {
        ArrayList<ScoreResult> results = new ArrayList<>();

        for (int i = 0; i < bsel.length; i++) {
            BetType b = bsel[i].bet_type;
            if (b == BetType.WIN || b == BetType.PLA || b == BetType.WP) {
                for (int j = 0; j < hs.length; j++) {
                    float score = hs[j].score * bsel[i].score;
                    results.add(new ScoreResult(score, b, new HorseSelection[] { hs[j] }, false));
                }
            }
        }
        return results;
    }

    private ArrayList<ScoreResult> calcMultiHorses(HorseSelection[] hsel, BetTypeSelection[] bsel) {
        ArrayList<ScoreResult> results = new ArrayList<>();
        int Len = hsel.length;
        for (int i = 0; i < bsel.length; i++) {
            BetType b = bsel[i].bet_type;
            if (b == BetType.QIN || b == BetType.QPL || b == BetType.QQP
                    || b == BetType.FCT) {
                int N = 2;
                for (int c1 = 0; c1 < Len - 1; c1++) {
                    for (int c2 = c1 + 1; c2 < Len; c2++) {
                        float newScore = (hsel[c1].score + hsel[c2].score) * bsel[i].score / (float) N;
                        results.add(new ScoreResult(newScore, b, new HorseSelection[] { hsel[c1], hsel[c2] }, false));
                    }
                }
            } else if (b == BetType.TCE || b == BetType.TRI) {
                int N = 3;
                for (int c1 = 0; c1 < Len - 2; c1++) {
                    for (int c2 = c1 + 1; c2 < Len - 1; c2++) {
                        for (int c3 = c2 + 1; c3 < Len; c3++) {
                            float newScore = (hsel[c1].score + hsel[c2].score + hsel[c3].score) * bsel[i].score
                                    / (float) N;
                            results.add(new ScoreResult(newScore, b,
                                    new HorseSelection[] { hsel[c1], hsel[c2], hsel[c3] }, false));

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
                                results.add(new ScoreResult(newScore, b,
                                        new HorseSelection[] { hsel[c1], hsel[c2], hsel[c3], hsel[c4] }, false));
                            }
                        }
                    }
                }
            }

        }
        return results;
    }

    private ArrayList<ScoreResult> calcAllHorses(HorseSelection[] hs, BetTypeSelection[] bsel) {
        Logger logger = Logger.getLogger(BetRecommendationCalculator.class);

        ArrayList<ScoreResult> results = new ArrayList<>();
        ArrayList<HorseSelection[]> groups = this.group(hs);
        for (int i = 0; i < bsel.length; i++) {
            BetType b = bsel[i].bet_type;
            if (b == BetType.CWA) {
                for (int j = 0; j < groups.size(); j++) {
                    try{
                        float score = groups.get(j)[0].score * bsel[i].score;
                        results.add(new ScoreResult(score, b, groups.get(j), false));
                    }catch (Exception e){
                        logger.error(e);
                        continue;
                    }
                }
            }
        }
        return results;
    }

    private void logHorseSelAndBetSel(Logger logger, String messageID, HorseSelection[] hs, BetTypeSelection[] bs, Exception e){
        try{
            ObjectMapper mapper = new ObjectMapper();
            String hs_str = mapper.writeValueAsString(hs);
            String bs_str = mapper.writeValueAsString(bs);
            logger.info("[MessageID]:"+ messageID + " horseSelections:" + hs_str + " betSel:" + bs_str);
        }catch (Exception err){
            logger.error(err);
        }
        logger.error(e);
    }

    private ArrayList<ScoreResult> calcScore(int race_no, String messageID) {
        Logger logger = Logger.getLogger(BetRecommendationCalculator.class);

        ArrayList<ScoreResult> results = new ArrayList<>();
        if(this.horseSelections == null || this.betTypeSelections == null){
            return results;
        }
//        HorseSelection[] hs = null;
//        BetTypeSelection[] bsl = null;
//        try{
//           hs = this.selectLegNo(this.horseSelections, HorseSelection.class, race_no);
//        }catch (Exception e){
//            logger.info("[MessageID]:"+ messageID + " horseSelections data  size:" + this.horseSelections.length);
//            logger.error(e);
//            return results;
//        }
//        try{
//           bsl = this.selectLegNo(this.betTypeSelections, BetTypeSelection.class, race_no);
//        }catch (Exception e){
//            logger.info("[MessageID]:"+ messageID + " betTypeSelections data  size:" + this.betTypeSelections.length);
//            logger.error(e);
//            return results;
//        }
        HorseSelection[] hs = this.horseSelections;
        BetTypeSelection[] bs = this.betTypeSelections;
        if(hs.length == 0 ){
            logger.info("[MessageID]:"+ messageID + " horseSelections data is empty");
            return results;
        }

        logger.info("[MessageID]:"+ messageID + " horseSelections data  size:" + hs.length);
        logger.info("[MessageID]:"+ messageID + " Bettype data  selection size:" + bs.length + " race_no:"+race_no);
//        BetTypeSelection[] bs = this.betTypeFilter(bsl, race_no);
//        logger.info("[MessageID]:"+ messageID + " Bettype data after type filter size:" + bs.length);


        ExecutorService file_load_executor = Executors.newFixedThreadPool(3);

        Callable<ArrayList<ScoreResult>> oneHorseTask = () -> {
            try{
                ArrayList<ScoreResult> r = this.calcOneHorse(hs, bs);
                return r;
            }catch (Exception e){
                logger.info("[MessageID]:"+ messageID + " cal one horse result exception" );
                logHorseSelAndBetSel(logger, messageID, hs, bs, e);
                return null;
            }
        };
        Callable<ArrayList<ScoreResult>> multiHorseTask = () -> {
            try{
                ArrayList<ScoreResult> r = this.calcMultiHorses(hs, bs);
                return r;
            }catch(Exception e){
                logger.info("[MessageID]:"+ messageID + " cal mul horse result exception" );
                logHorseSelAndBetSel(logger, messageID, hs, bs, e);
                return null;
            }
        };
        Callable<ArrayList<ScoreResult>> allHorseTask = () -> {
            try{
                ArrayList<ScoreResult> r  = this.calcAllHorses(hs, bs);
                return r;
            }catch (Exception e) {
                logger.info("[MessageID]:"+ messageID + " cal all horse result exception" );
                logHorseSelAndBetSel(logger, messageID, hs, bs, e);
                return null;
            }
        };


        List<Callable<ArrayList<ScoreResult>>> fileLoadingTasks = new ArrayList<>();
        fileLoadingTasks.add(oneHorseTask);
        fileLoadingTasks.add(multiHorseTask);
        fileLoadingTasks.add(allHorseTask);
        try{
            List<Future<ArrayList<ScoreResult>>> futures =  file_load_executor.invokeAll(fileLoadingTasks);
            Future<ArrayList<ScoreResult>> oneFuture = futures.get(0);
            if(oneFuture.get() != null){
                results.addAll(oneFuture.get());
            }
            Future<ArrayList<ScoreResult>> multiFuture = futures.get(1);
            if(multiFuture.get() != null){
                results.addAll(multiFuture.get());
            }
            Future<ArrayList<ScoreResult>> allFuture = futures.get(2);
            if(allFuture.get() != null){
                results.addAll(allFuture.get());
            }

        }catch (Exception e){
            logger.error(e);
        }

        file_load_executor.shutdown();

        return results;
    }

    private boolean checkEqualBetSel(String[] b, HorseSelection[] hs) {
        for (int i = 0; i < hs.length; i++) {
            // ignore orders...
            // add order filter in future
            if (!Arrays.asList(b).contains(hs[i].card_no)) {
                return false;
            }
        }
        return true;
    }

    private void filterOut(ArrayList<ScoreResult> results, BetFilter bf, int race_no) {
        if(bf == null) return;
        // 过滤的每一条pool里的race_no 假定一样
        // 如果不一样需要过滤掉 race_no
        for (int k = 0; k < bf.filter_bets.size(); k++) {
            if(race_no != bf.filter_bets.get(k).race_no){
                continue;
            }
            Pool p = bf.filter_bets.get(k).pool;
            Iterator<ScoreResult> iter = results.iterator();
            while (iter.hasNext()) {
                ScoreResult sr = iter.next();
                BetType b = p.pool_type;
                if (p.pool_type == sr.type) {
                    if (b == BetType.WIN || b == BetType.WP || b == BetType.PLA) {
                        if (Arrays.asList(p.sel).contains(sr.sel[0].card_no)) {
                            iter.remove();
                        }
                    } else if (b == BetType.QIN || b == BetType.QPL || b == BetType.QQP
                            || b == BetType.FCT || b == BetType.TRI || b == BetType.TCE || b == BetType.F_F || b == BetType.QTT) {
                        boolean inFilter = true;
                        for(HorseSelection a: sr.sel){
                            if(!Arrays.asList(p.sel).contains(a.card_no)){
                                inFilter = false;
                                break;
                            }
                        }
                        if(inFilter){
                            iter.remove();
                        }
                    } else if (b == BetType.CWA) {
                        String betGroup = sr.sel[0].group;
                        if (Arrays.asList(p.sel).contains(betGroup)) {
                            iter.remove();
                        }
                    }
                } else {
                    // check not same type filter
                    if (p.pool_type == BetType.WP && (sr.type == BetType.WIN || sr.type == BetType.PLA)) {
                        if (Arrays.asList(p.sel).contains(sr.sel[0].card_no)) {
                            iter.remove();
                        }
                    } else if (p.pool_type == BetType.QQP && (sr.type == BetType.QIN || sr.type == BetType.QPL)) {
                        boolean inFilter = true;
                        for(HorseSelection a: sr.sel){
                            if(!Arrays.asList(p.sel).contains(a.card_no)){
                                inFilter = false;
                                break;
                            }
                        }
                        if(inFilter){
                            iter.remove();
                        }
                    }
                }
            }
        }
    }

    private void globalMaxN(ArrayList<ScoreResult> results) {
        results.sort(new ScoreResultCmp());
        results.subList(this.TopN, results.size()).clear();
    }

    // get max score item of each type
    private void betTypeMaxN(ArrayList<ScoreResult> results) {
        Map<BetType, List<ScoreResult>> groups = results.stream().collect(Collectors.groupingBy(ScoreResult::getType));

        results.clear();
        // sort result by different bet type
        for (Map.Entry<BetType, List<ScoreResult>> entry : groups.entrySet()) {
            entry.getValue().sort(new ScoreResultCmp());
            results.add(entry.getValue().get(0));
        }
        results.sort(new ScoreResultCmp());
    }

    private Map<BetType, ScoreResult[]> betTypeTopN(ArrayList<ScoreResult> results, Map<String, Integer> betType_num) {
        Map<BetType, List<ScoreResult>> groups = results.stream().collect(Collectors.groupingBy(ScoreResult::getType));

        Map<BetType, ScoreResult[]> result = new HashMap<>();
        ObjectMapper om = new ObjectMapper();
        // sort result by different bet type
        Logger logger = Logger.getLogger(BetRecommendationCalculator.class);

        try{
            for (Map.Entry<BetType, List<ScoreResult>> entry : groups.entrySet()) {
                entry.getValue().sort(new ScoreResultCmp());
                ArrayList<ScoreResult> r = new ArrayList<ScoreResult>();
                int size = entry.getValue().size();

                String k = om.writeValueAsString(entry.getKey());
                k = k.substring(1, k.length() -1 );
                try{
                    int n = betType_num.get(k);
                    int m = size < n ? size : n;
                    for (int i = 0; i < m; i++) {
                        r.add(entry.getValue().get(i));
                    }
                    result.put(entry.getKey(), r.toArray(ScoreResult[]::new));
                }catch (NullPointerException e){
                    // catch betType_num_get error
                    logger.warn("not found bettype " + k + "in request bettypefilter num array");
                    continue;
                }
            }

        }catch (Exception e){
            logger.error(e);
        }

        return result;
    }

    private void filterCardNoZero(ArrayList<ScoreResult> results){
        Iterator<ScoreResult> iter = results.iterator();
        while (iter.hasNext()) {
            ScoreResult sr = iter.next();
            for(int i=0;i < sr.sel.length; i++){
                if(sr.sel[i].card_no.equals("0")){
                    iter.remove();
                    break;
                }
            }
        }
    }
    private void sortCardNo(ArrayList<ScoreResult> sr){

        for(ScoreResult _sr : sr){
            if(_sr.selres.length <= 1) continue;
            Arrays.sort(_sr.sel, new Comparator<HorseSelection>() {
                @Override
                public int compare(HorseSelection o1, HorseSelection o2) {
                    if(o1.score < o2.score){
                        return 1;
                    } else if (o1.score > o2.score) {
                        return -1;
                    }else{
                        return 0;
                    }
                }
            });

            for(int i = 0; i < _sr.sel.length;i++){
                _sr.selres[i] = _sr.sel[i].card_no;
            }
        }
    }
    public Recommendation recommendate(RecommendRequest messageObj, Date startTime) {
        try{
//            int interval = this.rtp.getInterval(startTime);
            Logger logger = Logger.getLogger(BetRecommendationCalculator.class);
            long start = System.nanoTime();
            String messageID = messageObj.messageID;
            int betslip_num = messageObj.betslip_display_num;
            int confirm_num = messageObj.betconfirmation_display_num;
            BetFilter bf = messageObj.betfilter;
            String race_id = messageObj.race_id;

            int race_no = this.rdp.dp.race_no;
            // calc all type score
            ArrayList<ScoreResult> results = this.calcScore(race_no, messageID);
            float time = (System.nanoTime() - start)/(float)1000000.0;
            logger.info("[MessageID]:" + messageID +  " ⏰[CalScore]:" + time + "ms");

            logger.info("[MessageID]:"+ messageID + " Result Size: " + results.size() + " betSlipNum: "+ betslip_num + " confirm_num: "+ confirm_num);

            start = System.nanoTime();
            this.filterCardNoZero(results);
            // filter out already brought
            this.filterOut(results, bf, race_no);

            if(betslip_num >= results.size()){
                betslip_num = results.size();
            }

            if(confirm_num >= results.size()){
                confirm_num = results.size();
            }
            // get top 6 results
            // 2 types: -global max -type max
            this.TopN = betslip_num > confirm_num ? betslip_num : confirm_num;
            logger.info("[MessageID]:"+ messageID + " After filter Result Size: " + results.size());

            if (this.GlobalFilter) {
                this.globalMaxN(results);
            } else {
                this.betTypeMaxN(results);
            }
            logger.info("[MessageID]:"+ messageID + " Top Result Size: " + results.size());

            this.sortCardNo(results);
            time = (System.nanoTime() - start)/(float)1000000.0;
            logger.info("[MessageID]:" + messageID +  " ⏰[ScoreFiltering]:" + time + "ms");

            // top3 in betslip and rest in bet_confirmation
            return Recommendation.constructBetSlipAndConfirmReco(this.mtg_id, this.bet_acc, race_no, results, this.rtp,
                    betslip_num, confirm_num,race_id);
        }catch (Exception e){
            Logger logger = Logger.getLogger(BetRecommendationCalculator.class);
            logger.error(e);
            return null;
        }
    }

    public Recommendation recommendate(RecommendRequest messageObj) throws Exception {
        // get the raceNo by startTime and rtp
        Date time = new Date(System.currentTimeMillis());
        return this.recommendate(messageObj, time);
    }

    // get all score list
    public Recommendation getAllScores(RecommendRequest messageObj)  {
        try{
            Logger logger = Logger.getLogger(BetRecommendationCalculator.class);
            long start = System.nanoTime();

            BetFilter bf= messageObj.betfilter;
            int raceNo = messageObj.raceNo;
            Map<String, Integer> bettype_num = messageObj.bettype_display_num;
            String race_id = messageObj.race_id;
            // calc all type score
            ArrayList<ScoreResult> results = this.calcScore(raceNo, messageObj.messageID);
            float time = (System.nanoTime() - start)/(float)1000000.0;
            logger.info("[MessageID]:" + messageObj.messageID +  " ⏰[CalScore]:" + time + "ms");
            start = System.nanoTime();

            // filter out already brought
            this.filterOut(results, bf, raceNo);
            Map<BetType, ScoreResult[]> topNtypes = this.betTypeTopN(results, bettype_num);
            ArrayList<ScoreResult> sr = new ArrayList<>();
            List<BetType> bettypes = new ArrayList<>(topNtypes.keySet());
            for (BetType bt : bettypes) {
                ScoreResult[] one_sr = topNtypes.get(bt);
                for (int i = 0; i < one_sr.length; i++) {
                    sr.add(one_sr[i]);
                }
            }
            this.sortCardNo(sr);
            time = (System.nanoTime() - start)/(float)1000000.0;
            logger.info("[MessageID]:" + messageObj.messageID +  " ⏰[ScoreFiltering]:" + time + "ms");
            return Recommendation.constructBetTypeReco(this.mtg_id, this.bet_acc, raceNo, sr, this.rtp, race_id);
        }catch (Exception e){
            Logger logger = Logger.getLogger(BetRecommendationCalculator.class);
            logger.error(e);
            return null;
        }
    }
}
