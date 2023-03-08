package com.HKJC.Data;

import com.HKJC.Config.Configurator;
import com.HKJC.Exceptions.HKJCException;
import com.HKJC.RaceSelector.RaceIntervalInfo;
import com.HKJC.RaceSelector.RaceSelector;
import com.HKJC.RaceSelector.RaceTime;
import com.HKJC.RatingCalculator.BetType;
import com.HKJC.Utils.DataFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DataProvider {
    public List<WinOddsRangeMapping> worMapping;
    public List<AcctIdMapping> acctIDMapping;
    public List<CWAGroup> cwaGroup;
    public List<RaceTimeData> raceTimes;
    public List<WinOdd> winOdds;
    public List<RaceStatus> racepool;
    public List<Runner> runners;
    public List<BetTypeOdd> betTypeOdds;
    public List<HorseOdd> horseOdds;
    public List<RaceOdd> raceOdds;

    public int race_no;


    protected static String HttpGet(String urlAdress, String meeting_id, String acc_id, String messageID) throws Exception{
        long starttime = System.nanoTime();
        URL url ;
        if(acc_id != null){
            url =  new URL(Configurator.getInstance().genAPI(urlAdress, meeting_id, acc_id));
        }else{
            url =  new URL(Configurator.getInstance().genAPI(urlAdress, meeting_id));
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = con.getResponseCode();
        Logger logger = Logger.getLogger(DataProvider.class);
        if(status != 200){
            con.disconnect();
            logger.error("[MessageID]:"+messageID + " HttpRequest Race data error, Status:" + status + " URL:" + url + " meeting_id:" + meeting_id+ " acc_id:" + acc_id);
            throw new HKJCException(status, "HttpRequest Race data error: URL:" + urlAdress + " meeting_id:" + meeting_id);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while((inputLine = in.readLine()) != null){
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        float time = (System.nanoTime() - starttime)/(float)1000000.0;
        logger.info("[MessageID]:"+messageID + " Request:"+ urlAdress + " Status:" + status + " time:"+time+"ms");

        return content.toString();
    }

    protected static JSONArray getContent(String data){
        JSONObject json = (JSONObject) JSONSerializer.toJSON(data);
        return json.getJSONArray( "content" );
    }
    protected static String parseMtg(String mtg){
        String pattern = "(?<=MTG_)(\\w+)(?=_)";
        return DataFilter.find(mtg, pattern);
    }

    protected  List<Integer> getAvailableRaceNo(){
        List<Integer> r = new ArrayList<Integer>();
        for(RaceStatus rs : this.racepool){
            r.add(rs.race_no);
        }
        return r;
    }
    protected  void checkRacepool(Logger logger, String messageID) throws Exception{
        List<RaceStatus> nR = new ArrayList<RaceStatus>();
        for(int i = 0 ; i < this.racepool.size(); i++){
            RaceStatus rs = this.racepool.get(i);
            if(rs.status == RaceStatusCode.Declared || rs.status == RaceStatusCode.Named){
                for(Field f : rs.pool_status.getClass().getDeclaredFields()){
                    PoolStatusCode psc = (PoolStatusCode) f.get(rs.pool_status);
                    if(psc == PoolStatusCode.StartSell){
                        nR.add(rs);
                        break;
                    }
                }
            }
        }

        if(nR.size() == 0){
            // print racepool
            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DateFormat df = new SimpleDateFormat(RaceTime.TimePattern);
            objectMapper.setDateFormat(df);
            String rpStr = objectMapper.writeValueAsString(this.racepool);
            logger.info("[MessageID]:"+messageID + "RacePool: " + rpStr);
            logger.info("[MessageID]:"+messageID +"No race status is startSell ");
        }
        this.racepool = nR;
    }

    protected void setupWorMapping(){
        // temperarily hardcore worMapping data
        this.worMapping = new ArrayList<>();
        WinOddsRangeMapping wor1 = new WinOddsRangeMapping();
        wor1.odds_range = "0-2.0";
        wor1.win_odds_top1_range =1;
        this.worMapping.add(wor1);

        WinOddsRangeMapping wor2 = new WinOddsRangeMapping();
        wor2.odds_range = "2.1-4.0";
        wor2.win_odds_top1_range =2;
        this.worMapping.add(wor2);

        WinOddsRangeMapping wor3 = new WinOddsRangeMapping();
        wor3.odds_range = "4.1-999.0";
        wor3.win_odds_top1_range = 3;
        this.worMapping.add(wor3);
    }

    protected int getInterval() throws Exception{
        int r = Integer.MAX_VALUE;
        for(int i = 0; i< this.racepool.size(); i++) {
            RaceStatus rs = this.racepool.get(i);
            if((rs.status == RaceStatusCode.Declared || rs.status == RaceStatusCode.Named) && rs.race_no < r){
                r = rs.race_no;
            }
        }
        if(r == Integer.MAX_VALUE){
            throw new Exception("Not found earliest race in race status list");
        }
        return r;
    }

    protected RaceSelector genRaceSelector() throws Exception {
        return new RaceSelector(this.genRaceIntervalData());
    }
    private List<RaceIntervalInfo> genRaceIntervalData() {

        List<RaceIntervalInfo> data = new ArrayList<RaceIntervalInfo>();

        for (int i = 0; i < this.raceOdds.size(); i++) {
            RaceOdd ro = this.raceOdds.get(i);
            // [TODO?] maybe need to filter win_odd_range out
            data.add(new RaceIntervalInfo(ro.mtg_id, ro.betting_acct_no, ro.prox_intvl, ro.leg_rs_no, ro.predict));
        }
        return data;
    }
    private RaceStatus getRaceStatus(int raceNo){
        for(int i = 0; i < this.racepool.size() ; i++){
            if(this.racepool.get(i).race_no == raceNo){
                return this.racepool.get(i);
            }
        }
        return null;
    }
    protected JSONArray filterDataStr(JSONArray data, int race_no) {
        JSONArray r = new JSONArray();
        for(int i = 0; i < data.size();i++){
            JSONObject obj = data.getJSONObject(i);
            int rn = obj.getInt("leg_rs_no");
            if(rn == race_no){
                r.add(obj);
            }
        }
        return r;
    }

    protected JSONArray filterBettypeDataStr(JSONArray data, int race_no) {
        RaceStatus rs = this.getRaceStatus(race_no);
        JSONArray r = new JSONArray();
        if(rs == null){
            return r;
        }
        for(int i = 0; i < data.size();i++){
            JSONObject obj = data.getJSONObject(i);
            int rn = obj.getInt("leg_rs_no");
            String bt = obj.getString("bet_type");

            if(rn == race_no){
                try{
                    Field f = PoolStatus.class.getField(bt);
                    PoolStatusCode psc =  (PoolStatusCode) f.get(rs.pool_status);
                    if(psc == PoolStatusCode.StartSell){
                        r.add(obj);
                    }
                }catch (Exception e){
                    // no status ignored
                    continue;
                }
            }
        }
        return r;
    }


    protected String filterDataStr(String data, int race_no){
        StringBuilder sb = new StringBuilder("[");
        int l = data.length();

        int lr = -1;
        Pattern p = Pattern.compile("\"leg_rs_no\":\\s*(\\d+)");
        for(int i = 0; i < l - 1; i++){
            String c = data.substring(i, i+1);
            if( c.equals("{") ){
                System.out.println("found { at " + i);
                lr = i;
                i += 50;
            }else if(c.equals("}")){
                System.out.println("found } at " + i);
                int rr = i;
                // search race_no
                String s = data.substring(lr, rr);
                Matcher matcher = p.matcher(s);
                if(matcher.find()){
                    int rn = Integer.parseInt(matcher.group(1));
                    if(rn == race_no){
                        sb.append(data.substring(lr, rr + 1));
                        if(rr < l - 10){
                            sb.append(",");
                        }
                    }
                }

                System.out.println(s);
            }// end if
        }// end for

        sb.append("]");
        return sb.toString();
    }
}
