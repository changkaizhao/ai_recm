package com.HKJC.SolMessager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.HKJC.Data.*;

import com.HKJC.RaceSelector.RaceTime;
import com.HKJC.RaceSelector.RaceTimeProvider;
import com.HKJC.RatingCalculator.BetType;
import com.HKJC.RatingCalculator.BetTypeSelection;
import com.HKJC.RatingCalculator.HorseSelection;
import com.HKJC.Config.Configurator;
import org.apache.log4j.Logger;

public class RecomendDataProvider {

    public DataProvider dp;

    public RecomendDataProvider(String meeting_id, String acc_id, String messageID, int race_no) throws Exception {
        DataProviderType dpt = Configurator.getInstance().dataProviderType;
        switch (dpt) {
            case JSONFileData: {
                this.dp = new JsonDataProvider(race_no);
                break;
            }
            case API: {
                this.dp = new APIDataProvider(meeting_id, acc_id, messageID, race_no);
                break;
            }

            case FileData: {
                this.dp = new FileDataProvider(meeting_id, acc_id, messageID, race_no);
                break;
            }
            case Default: {

            }
        }
    }

    public HorseSelection[] genHorseSelData() {
        HorseSelection[] hs;
        switch (Configurator.getInstance().dataProviderType) {
            case API:
            case FileData:
            case JSONFileData: {
                Logger logger = Logger.getLogger(RecomendDataProvider.class);
                HashMap<Integer, HashMap<String, String>> group = new HashMap<Integer, HashMap<String, String>>();

                if (this.dp.cwaGroup == null || this.dp.cwaGroup.size() == 0) {
                    group = null;
                } else {
                    for (int i = 0; i < this.dp.cwaGroup.size(); i++) {
                        int race_no = this.dp.cwaGroup.get(i).leg_rs_no;
                        if (!group.containsKey(race_no)) {
                            HashMap<String, String> groupInfo = new HashMap<String, String>();
                            group.put(race_no, groupInfo);
                        }
                        group.get(race_no).put(this.dp.cwaGroup.get(i).card_no, this.dp.cwaGroup.get(i).group);
                    }
                }

                ArrayList<HorseSelection> hsList = new ArrayList<>();

                for (int i = 0; i < this.dp.horseOdds.size(); i++) {
                    String card_no = this.dp.horseOdds.get(i).card_no;
                    int race_no = this.dp.horseOdds.get(i).leg_rs_no;
                    int winOddOrder = this.dp.horseOdds.get(i).win_odds_order;

                    WinOdd wo = null;
                    for (int j = 0; j < this.dp.winOdds.size(); j++) {
                        if (this.dp.winOdds.get(j).card_no == Integer.parseInt(card_no)
                                && this.dp.winOdds.get(j).leg_rs_no == race_no) {
                            wo = this.dp.winOdds.get(j);
                            break;
                        }
                    }
                    if (wo != null && wo.win_odds_order == winOddOrder) {
                        String gs = "";
                        if (group != null) {
                            // [TODO]roby
                            // fix this hardcode in the future....
                            try {
                                gs = group.get(race_no).get(card_no);
                            } catch (Exception e) {
                                gs = "";
                            }
                        }
                        // filter out horses with undeclared
                        RunnerStatus rs = RunnerStatus.UNDEFINED;
                        for (int ir = 0; ir < this.dp.runners.size(); ir++) {
                            if (this.dp.runners.get(ir).leg_rs_no == race_no
                                    && this.dp.runners.get(ir).card_no.equals(card_no)) {
                                rs = this.dp.runners.get(ir).hrs_status;
                                break;
                            }
                        }
                        if (rs == RunnerStatus.Declared) {
                            hsList.add(new HorseSelection(this.dp.horseOdds.get(i).mtg_id,
                                    card_no,
                                    race_no,
                                    this.dp.horseOdds.get(i).brand_no,
                                    -1,
                                    winOddOrder,
                                    this.dp.horseOdds.get(i).predict,
                                    gs));
                        }

                    }

                }
                hs = (HorseSelection[]) Array.newInstance(HorseSelection.class, hsList.size());
                hsList.toArray(hs);
                logger.info("hs size after generation:" + hs.length);
                break;
            }
            case Default: {
            }
            default: {
                hs = new HorseSelection[16];
                hs[0] = new HorseSelection("20201029", "5", 1, "A316", 48, 5, 0.5f, "A1");
                hs[1] = new HorseSelection("20201029", "4", 1, "A317", 37, 3, 0.7f, "A1");
                hs[2] = new HorseSelection("20201029", "3", 1, "A318", 60, 1, 0.3f, "A2");
                hs[3] = new HorseSelection("20201029", "2", 1, "A319", 55, 2, 0.4f, "A2");
                hs[4] = new HorseSelection("20201029", "1", 1, "A320", 50, 4, 0.5f, "A3");
                hs[5] = new HorseSelection("20201029", "1", 2, "A321", 44, 1, 0.6f, "A1");
                hs[6] = new HorseSelection("20201029", "2", 2, "A322", 39, 2, 0.3f, "A1");
                hs[7] = new HorseSelection("20201029", "3", 2, "A323", 50, 3, 0.5f, "A2");
                hs[8] = new HorseSelection("20201029", "4", 2, "A324", 50, 4, 0.5f, "A3");
                hs[9] = new HorseSelection("20201029", "5", 2, "A325", 54, 5, 0.5f, "A3");
                hs[10] = new HorseSelection("20201029", "1", 3, "A326", 41, 5, 0.6f, "A1");
                hs[11] = new HorseSelection("20201029", "2", 3, "A327", 51, 4, 0.7f, "A1");
                hs[12] = new HorseSelection("20201029", "3", 3, "A328", 49, 6, 0.2f, "A2");
                hs[13] = new HorseSelection("20201029", "4", 3, "A329", 50, 1, 0.4f, "A2");
                hs[14] = new HorseSelection("20201029", "5", 3, "A330", 50, 2, 0.4f, "A3");
                hs[15] = new HorseSelection("20201029", "6", 3, "A331", 49, 3, 0.5f, "A3");
            }
        }

        return hs;

    }

    public BetTypeSelection[] genBetTypeSelData() throws Exception {
        BetTypeSelection[] bs;
        Logger logger = Logger.getLogger(RecomendDataProvider.class);

        switch (Configurator.getInstance().dataProviderType) {
            case API:
            case FileData:
            case JSONFileData: {
                // read win odds and find the range
                HashMap<Integer, Float> minOdd = new HashMap<>();
                List<WinOdd> winOdds = this.dp.winOdds;
                for (int i = 0; i < winOdds.size(); i++) {
                    int race_no = winOdds.get(i).leg_rs_no;
                    float odd = winOdds.get(i).win_odds;
                    if (!minOdd.containsKey(race_no)) {
                        minOdd.put(race_no, odd);
                    } else {
                        if (minOdd.get(race_no) > odd) {
                            minOdd.put(race_no, odd);
                        }
                    }
                }

                // find range from min win odd
                HashMap<Integer, Integer> rangeMap = new HashMap<>();
                for (int key : minOdd.keySet()) {
                    float odd = minOdd.get(key);
                    for (int k = 0; k < this.dp.worMapping.size(); k++) {
                        if (this.dp.worMapping.get(k).in(odd)) {
                            rangeMap.put(key, this.dp.worMapping.get(k).win_odds_top1_range);
                            break;
                        }
                    }
                }

                int race_range = rangeMap.get(race_no);
                logger.info("get win_odd_top1_range:" + race_range + " with race_no:" + race_no + " from data winodds");
                // find range and raceNo matched bettype
                ArrayList<BetTypeSelection> btList = new ArrayList<>();
                for (int i = 0; i < this.dp.betTypeOdds.size(); i++) {
                    BetTypeOdd bto = this.dp.betTypeOdds.get(i);

                    int race_no = bto.leg_rs_no;
                    int range = bto.win_odds_top1_range;
                    if (race_range == range) {
                        btList.add(new BetTypeSelection(bto.mtg_id, bto.leg_rs_no, bto.bet_type, bto.predict));
                    }
                }

                bs = (BetTypeSelection[]) Array.newInstance(BetTypeSelection.class, btList.size());
                btList.toArray(bs);
                break;
            }
            case Default: {

            }
            default: {
                bs = new BetTypeSelection[31];
                bs[0] = new BetTypeSelection("20201029", 1, BetType.WIN, 0.42f);
                bs[1] = new BetTypeSelection("20201029", 1, BetType.PLA, 0.61f);
                bs[2] = new BetTypeSelection("20201029", 1, BetType.WP, 0.5f);
                bs[3] = new BetTypeSelection("20201029", 1, BetType.QIN, 0.49f);
                bs[4] = new BetTypeSelection("20201029", 1, BetType.QPL, 0.55f);
                bs[5] = new BetTypeSelection("20201029", 1, BetType.QQP, 0.4f);
                bs[6] = new BetTypeSelection("20201029", 1, BetType.FCT, 0.5f);
                bs[7] = new BetTypeSelection("20201029", 1, BetType.TCE, 0.33f);
                bs[8] = new BetTypeSelection("20201029", 1, BetType.TRI, 0.4f);
                bs[9] = new BetTypeSelection("20201029", 1, BetType.F_F, 0.5f);
                bs[10] = new BetTypeSelection("20201029", 1, BetType.QTT, 0.4f);
                bs[11] = new BetTypeSelection("20201029", 1, BetType.CWA, 0.5f);
                bs[12] = new BetTypeSelection("20201029", 2, BetType.WIN, 0.11f);
                bs[13] = new BetTypeSelection("20201029", 2, BetType.PLA, 0.67f);
                bs[14] = new BetTypeSelection("20201029", 2, BetType.WP, 0.47f);
                bs[15] = new BetTypeSelection("20201029", 2, BetType.QIN, 0.55f);
                bs[16] = new BetTypeSelection("20201029", 2, BetType.QPL, 0.35f);
                bs[17] = new BetTypeSelection("20201029", 2, BetType.QQP, 0.28f);
                bs[18] = new BetTypeSelection("20201029", 2, BetType.FCT, 0.52f);
                bs[19] = new BetTypeSelection("20201029", 2, BetType.TCE, 0.33f);
                bs[20] = new BetTypeSelection("20201029", 2, BetType.TRI, 0.55f);
                bs[21] = new BetTypeSelection("20201029", 2, BetType.F_F, 0.44f);
                bs[22] = new BetTypeSelection("20201029", 2, BetType.QTT, 0.48f);
                bs[23] = new BetTypeSelection("20201029", 2, BetType.CWA, 0.61f);
                bs[24] = new BetTypeSelection("20201029", 3, BetType.WIN, 0.49f);
                bs[25] = new BetTypeSelection("20201029", 3, BetType.PLA, 0.7f);
                bs[26] = new BetTypeSelection("20201029", 3, BetType.WP, 0.51f);
                bs[27] = new BetTypeSelection("20201029", 3, BetType.QIN, 0.63f);
                bs[28] = new BetTypeSelection("20201029", 3, BetType.QPL, 0.56f);
                bs[29] = new BetTypeSelection("20201029", 3, BetType.QQP, 0.58f);
                bs[30] = new BetTypeSelection("20201029", 3, BetType.FCT, 0.6f);
            }
        }

        return bs;
    }

    private RaceTime[] genRaceTimeData() throws Exception {
        switch (Configurator.getInstance().dataProviderType) {
            case API:
            case FileData:
            case JSONFileData: {
                RaceTime[] rt = new RaceTime[this.dp.raceTimes.size()];
                for (int i = 0; i < this.dp.raceTimes.size(); i++) {
                    RaceTimeData rtd = this.dp.raceTimes.get(i);
                    rt[i] = new RaceTime(rtd.mtg_id, rtd.leg_rs_no, rtd.start_time, rtd.venCode);
                }
                return rt;
            }
            case Default: {

            }
            default: {
                RaceTime[] testD = {
                        new RaceTime("20201029", "1", "2021-06-09T14:40:00+08:00", "ST"),
                        new RaceTime("20201029", "2", "2021-06-09T14:50:00+08:00", "ST"),
                        new RaceTime("20201029", "6", "2021-06-09T15:30:00+08:00", "ST"),
                        new RaceTime("20201029", "4", "2021-06-09T15:10:00+08:00", "ST"),
                        new RaceTime("20201029", "5", "2021-06-09T15:20:00+08:00", "ST"),
                        new RaceTime("20201029", "7", "2021-06-09T15:40:00+08:00", "ST"),
                        new RaceTime("20201029", "3", "2021-06-09T15:00:00+08:00", "ST"),
                        new RaceTime("20201029", "8", "2021-06-09T15:50:00+08:00", "ST") };

                return testD;
            }
        }

    }

    public List<AcctIdMapping> getAcctMappingData() {
        switch (Configurator.getInstance().dataProviderType) {
            case API:
            case FileData:
            case JSONFileData: {
                return this.dp.acctIDMapping;

            }
            case Default: {
            }
            default: {
                return null;
            }
        }
    }

    public RaceTimeProvider genRaceTimeProvider() throws Exception {

        // new Timer().schedule(new TimerTask() {
        // @Override
        // public void run() {
        // LocalDate current_date = LocalDate.now();

        // for (int i = 0; i < RecomendDataProvider.rtp.data.length; i++) {
        // Calendar cal = Calendar.getInstance();
        // cal.setTime(RecomendDataProvider.rtp.data[i].start_time);
        // cal.set(current_date.getYear(), current_date.getMonthValue() - 1,
        // current_date.getDayOfMonth());

        // RecomendDataProvider.rtp.data[i].start_time = cal.getTime();

        // }
        // }
        // }, 0, 1000 * 60 * 60 * 24);

        return new RaceTimeProvider(this.genRaceTimeData());
    }

    public RaceStatus getRaceStatus(int raceNo) {
        for (int i = 0; i < this.dp.racepool.size(); i++) {
            if (this.dp.racepool.get(i).race_no == raceNo) {
                return this.dp.racepool.get(i);
            }
        }
        return null;
    }

}
