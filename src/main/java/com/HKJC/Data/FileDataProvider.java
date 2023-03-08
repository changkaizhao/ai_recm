package com.HKJC.Data;

import com.HKJC.Config.Configurator;
import com.HKJC.Exceptions.HKJCException;
import com.HKJC.OjaiDB.document.BettypeDocumentService;
import com.HKJC.OjaiDB.document.HorseDocumentService;
import com.HKJC.OjaiDB.document.RaceProximityDocumentService;
import com.HKJC.RaceSelector.RaceSelector;
import com.HKJC.RaceSelector.RaceTime;
import com.HKJC.Recommendation.GlobalAttrRaceInterval;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;


public class FileDataProvider  extends DataProvider{
    private String meeting_id;
    private String getFullName(String fileName){
        return String.format("MTG_%s_0001_%s.json",this.meeting_id, fileName);
    }
    private Path getRacePoolFilePath(){
        return Paths.get(Configurator.getInstance().fileRootPath, this.getFullName("race_pool")) ;
    }
    private Path getRaceTimeFilePath(){
        return Paths.get(Configurator.getInstance().fileRootPath, this.getFullName("race_time")) ;
    }
    private Path getRunnerFilePath(){
        return Paths.get(Configurator.getInstance().fileRootPath, this.getFullName("result_runner")) ;
    }
    private Path getWinOddsFilePath(){
        return Paths.get(Configurator.getInstance().fileRootPath, this.getFullName("win_odds")) ;
    }
    private Path getCWAFilePath(){
        return Paths.get(Configurator.getInstance().fileRootPath, this.getFullName("cwa_grp")) ;
    }

    private void loadDataAsync(ObjectMapper objectMapper, String messageID, String acc_id, boolean useProxi) throws Exception{
        Logger logger = Logger.getLogger(FileDataProvider.class);

        ExecutorService file_load_executor = Executors.newFixedThreadPool(8);
        Callable<Void> racepoolTask = () -> {
            try{
                long start = System.nanoTime();
                this.racepool = Arrays
                        .asList(objectMapper.readValue(this.getRacePoolFilePath().toFile(),
                                RaceStatus[].class));
                this.checkRacepool(logger, messageID);
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[RacePool]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };
        Callable<Void> raceTimesTask = () -> {
            try{
                long start = System.nanoTime();
                this.raceTimes = Arrays
                        .asList(objectMapper.readValue(this.getRaceTimeFilePath().toFile(),
                                RaceTimeData[].class));
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[RaceTimes]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };
        Callable<Void> winOddsTask = () -> {
            try{
                long start = System.nanoTime();
                this.winOdds = Arrays
                        .asList(objectMapper.readValue(this.getWinOddsFilePath().toFile(),
                                WinOdd[].class));
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[WinOdds]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };
        Callable<Void> cwaGroupTask = () -> {
            try{
                long start = System.nanoTime();
                this.cwaGroup = Arrays.asList(objectMapper.readValue(this.getCWAFilePath().toFile(),
                        CWAGroup[].class));
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[CWAGroup]:" + end + "ms");
            }catch(Exception e){
                logger.error(e);
            }

            return null;
        };
        Callable<Void> runnerTask = () -> {
            try{
                long start = System.nanoTime();
                this.runners = Arrays.asList(objectMapper.readValue(this.getRunnerFilePath().toFile(),
                        Runner[].class));
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[Runner]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };


        Callable<Void> raceProximityDataTask = () -> {
            try{
                long start = System.nanoTime();
//                String raceProximityData =  DataProvider.HttpGet(Configurator.getInstance().api_raceProximity, meeting_id, acc_id, messageID);
//                float end = (System.nanoTime() - start)/(float)1000000.0;
//                logger.info("[MessageID]:"+messageID + "  ⏰[FetchingRaceProximityData]:" + end + "ms");
//                start = System.nanoTime();
//
//                JSONArray raceProximityArray = DataProvider.getContent(raceProximityData);
//                raceProximityData = raceProximityArray.toString();
//                this.raceOdds = Arrays.asList(objectMapper.readValue(raceProximityData,RaceOdd[].class));
                RaceProximityDocumentService rpds = new RaceProximityDocumentService();
                this.raceOdds = rpds.findDTO(meeting_id, acc_id);
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[FetchingRaceProximityData]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }

            return null;
        };
        Callable<Void> bettypeDataTask = () -> {
            try{
                long start = System.nanoTime();
//                String bettypeData = DataProvider.HttpGet(Configurator.getInstance().api_bettype, meeting_id, acc_id, messageID);

                BettypeDocumentService btds = new BettypeDocumentService();
                this.betTypeOdds = btds.findDTO(meeting_id, acc_id);
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[FetchingBettypeData]:" + end + "ms");
//                JSONArray bettypeDataArray = DataProvider.getContent(bettypeData);
//                return bettypeDataArray;
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };
        Callable<Void> horseDataTask = () -> {
            try{
                long start = System.nanoTime();
//                String horseData =  DataProvider.HttpGet(Configurator.getInstance().api_horse, meeting_id, acc_id, messageID);

                HorseDocumentService hds = new HorseDocumentService();
                this.horseOdds = hds.findDTO(meeting_id, acc_id);

                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[FetchingHorseData]:" + end + "ms");
//                JSONArray horseDataArray = DataProvider.getContent(horseData);
//                return horseDataArray;
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };

        List<Callable<Void>> fileLoadingTasks = new ArrayList<>();
        fileLoadingTasks.add(bettypeDataTask);
        fileLoadingTasks.add(horseDataTask);
        if(useProxi){
            fileLoadingTasks.add(raceProximityDataTask);
        }
        fileLoadingTasks.add(raceTimesTask);
        fileLoadingTasks.add(winOddsTask);
        fileLoadingTasks.add(cwaGroupTask);
        fileLoadingTasks.add(runnerTask);
        fileLoadingTasks.add(racepoolTask);

    try{
        file_load_executor.invokeAll(fileLoadingTasks);
    }catch (Exception e){
        logger.error(e);
        file_load_executor.shutdown();
        throw new Exception("invoke concurrent tasks failed");
    }

        file_load_executor.shutdown();
    }

    // filter plain json data with no more embeded { } structure
    private void asyncFilterObjByRaceNo(String messageID) throws Exception{
        Logger logger = Logger.getLogger(FileDataProvider.class);
        ExecutorService file_load_executor = Executors.newFixedThreadPool(4);

        Callable<Void> filterWinOdds = () -> {
            try{
                long start = System.nanoTime();
                List<WinOdd> n = new ArrayList<WinOdd>();
                Iterator<WinOdd> it = this.winOdds.iterator();
                while(it.hasNext()) {
                    WinOdd w = it.next();
                    if(w.leg_rs_no == this.race_no) {
                        n.add(w);  // cannot use it.remove, because of Arrays.aslist make fixed size array
                    }
                }
                this.winOdds = n;
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[filterWinOdds]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }
            return null;
        };
        Callable<Void> fitlerRunners = () -> {
            try{
                long start = System.nanoTime();
                List<Runner> n = new ArrayList<>();
                Iterator<Runner> it = this.runners.iterator();
                while(it.hasNext()) {
                    Runner r = it.next();
                    if(r.leg_rs_no == this.race_no) {
                          n.add(r);// 删除
                    }
                }
                this.runners = n;
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[fitlerRunners]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }

            return null;
        };
        Callable<Void> filterHorse = () -> {
            try{
                long start = System.nanoTime();
                //fitler
//                JSONArray horseData = this.filterDataStr(horseObj, this.race_no);
//                float end = (System.nanoTime() - start)/(float)1000000.0;
//                logger.info("[MessageID]:"+messageID + "  ⏰[filterHorse]:" + end + "ms");
//                start = System.nanoTime();
//                this.horseOdds = Arrays.asList(objectMapper.readValue(horseData.toString(),HorseOdd[].class));
                List<HorseOdd> n = new ArrayList<>();
                Iterator<HorseOdd> it = this.horseOdds.iterator();
                while(it.hasNext()) {
                    HorseOdd r = it.next();
                    if(r.leg_rs_no == this.race_no) {
                        n.add(r);// 删除
                    }
                }
                this.horseOdds = n;
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[filterHorse]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }

            return null;
        };
        Callable<Void> filterBettype = () -> {
            try{
                long start = System.nanoTime();
                // filter
//                JSONArray bettypeData = this.filterBettypeDataStr(bettypeObj, this.race_no);
//                float end = (System.nanoTime() - start)/(float)1000000.0;
//                logger.info("[MessageID]:"+messageID + "  ⏰[filterBettype]:" + end + "ms");
//                start = System.nanoTime();
//                this.betTypeOdds = Arrays.asList(objectMapper.readValue(bettypeData.toString(), BetTypeOdd[].class));
                List<BetTypeOdd> n = new ArrayList<>();
                Iterator<BetTypeOdd> it = this.betTypeOdds.iterator();
                while(it.hasNext()) {
                    BetTypeOdd r = it.next();
                    if(r.leg_rs_no == this.race_no) {
                        n.add(r);// 删除
                    }
                }
                this.betTypeOdds = n;
                float end = (System.nanoTime() - start)/(float)1000000.0;
                logger.info("[MessageID]:"+messageID + "  ⏰[filterBettype]:" + end + "ms");
            }catch (Exception e){
                logger.error(e);
            }

            return null;
        };

        List<Callable<Void>> fileLoadingTasks = new ArrayList<>();
        fileLoadingTasks.add(filterWinOdds);
        fileLoadingTasks.add(fitlerRunners);
        fileLoadingTasks.add(filterHorse);
        fileLoadingTasks.add(filterBettype);
        try{
            file_load_executor.invokeAll(fileLoadingTasks);
        }catch (Exception e){
            logger.error(e);
            file_load_executor.shutdown();
            throw new Exception("filter concurrent tasks failed");
        }

        file_load_executor.shutdown();

    }
    public FileDataProvider(String mtg_id, String acc_id, String messageID, int race_no) throws Exception{

        Logger logger = Logger.getLogger(FileDataProvider.class);

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DateFormat df = new SimpleDateFormat(RaceTime.TimePattern);
        objectMapper.setDateFormat(df);

        this.setupWorMapping();
        String meeting_id = DataProvider.parseMtg(mtg_id);
        this.meeting_id = meeting_id;

        // if found in global cache
        // if not load again
        boolean useProxi = race_no < 0 ? true : false;

       this.loadDataAsync(objectMapper, messageID, acc_id, useProxi);

        if(this.racepool == null || this.racepool.size() == 0){
            throw  new HKJCException(501, "No race status get from " + Configurator.getInstance().api_racepool + " meetingID:" + meeting_id);
        }
        if(this.raceTimes == null || this.raceTimes.size() == 0){
            throw  new HKJCException(501, "No raceTimes get from " + Configurator.getInstance().api_racetime+ " meetingID:" + meeting_id);
        }
        if(this.winOdds == null || this.winOdds.size() == 0){
            throw  new HKJCException(501, "No winOdds get from " + Configurator.getInstance().api_winodds+ " meetingID:" + meeting_id);
        }

        if(this.runners == null || this.runners.size() == 0){
            throw  new HKJCException(501, "No runners get from " + Configurator.getInstance().api_runner+ " meetingID:" + meeting_id);
        }

        if(useProxi && (this.raceOdds == null || this.raceOdds.size() == 0)){
            throw  new HKJCException(501, "No raceOdds get from " + Configurator.getInstance().api_raceProximity+ " meetingID:" + meeting_id+ " betAcc:" + acc_id);
        }

        //=====================================================================================
        if(useProxi){
            // cal race_no
            int interval = this.getInterval();
            //save interval
            GlobalAttrRaceInterval.getInstance().race_interval = interval;
            RaceSelector rs = this.genRaceSelector();
            this.race_no = rs.select(mtg_id, acc_id, interval, this.getAvailableRaceNo());
            logger.info("[MessageID]:"+ messageID + " interval: " + interval + " raceNo: "+ this.race_no);
        }else{
            this.race_no = race_no;
            logger.info("[MessageID]:"+ messageID +  " raceNo: "+ this.race_no);
        }

//        JSONArray betTypeStr = null;
//        JSONArray horseStr = null;
//        if(resultList != null){
//            for (int i = 0; i < resultList.size(); i++) {
//                Future<JSONArray> future = resultList.get(i);
//                try {
//                    if(i == 0){
//                        betTypeStr = future.get();
//                    }else if(i == 1){
//                        horseStr = future.get();
//                    }else{
//                        break;
//                    }
//                } catch (InterruptedException | ExecutionException e) {
//                    throw  new HKJCException(501, "cannot get horse and bettype data from api " + " meetingID:" + meeting_id);
//                }
//            }
//        }else{
//            throw  new HKJCException(501, "cannot get horse and bettype data from api " + " meetingID:" + meeting_id);
//        }

        // all steps can be implemented in concurrently below  .....
        // filter winodds  runner by race_no
        // filter horse bettype strin by race_no
        //obj mapping bettype and horse
//        this.asyncFilterObjByRaceNo(messageID, objectMapper, horseStr, betTypeStr);

        //========================================================================================
        if(this.winOdds == null || this.winOdds.size() == 0){
            throw  new HKJCException(501, "No winOdds after filter with raceNo: "+ this.race_no  + " meetingID:" + meeting_id);
        }
        if(this.runners == null || this.runners.size() == 0){
            throw  new HKJCException(501, "No runners after filter with raceNo: "+ this.race_no   + " meetingID:" + meeting_id);
        }
        //==================================== API from model ===================================================
        if(this.betTypeOdds == null || this.betTypeOdds.size() == 0){
            throw  new HKJCException(501, "No betTypeOdds get from " + Configurator.getInstance().api_bettype+ " meetingID:" + meeting_id + " betAcc:" + acc_id);
        }

        if(this.horseOdds == null || this.horseOdds.size() == 0){
            throw  new HKJCException(501, "No horseOdds get from " + Configurator.getInstance().api_horse+ " meetingID:" + meeting_id+ " betAcc:" + acc_id);
        }

    }
}
