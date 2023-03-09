package com.HKJC.Data;

import com.HKJC.Config.Configurator;
import com.HKJC.Exceptions.HKJCException;
import com.HKJC.RaceSelector.RaceTime;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class APIDataProvider extends DataProvider {

    public APIDataProvider(String mtg_id, String acc_id, String messageID, int race_no) throws Exception {
        try {
            Logger logger = Logger.getLogger(APIDataProvider.class);
            long starttime = System.nanoTime();

            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            DateFormat df = new SimpleDateFormat(RaceTime.TimePattern);
            objectMapper.setDateFormat(df);

            this.setupWorMapping();

            String meeting_id = DataProvider.parseMtg(mtg_id);

            String racePool = DataProvider.HttpGet(Configurator.getInstance().api_racepool, meeting_id, null,
                    messageID);
            this.racepool = Arrays.asList(objectMapper.readValue(racePool, RaceStatus[].class));
            this.checkRacepool(logger, meeting_id);

            // bettype
            String bettypeData = DataProvider.HttpGet(Configurator.getInstance().api_bettype, meeting_id, acc_id,
                    messageID);
            JSONArray bettypeDataA = DataProvider.getContent(bettypeData);
            this.betTypeOdds = Arrays.asList(objectMapper.readValue(bettypeDataA.toString(), BetTypeOdd[].class));
            if (this.betTypeOdds == null || this.betTypeOdds.size() == 0) {
                throw new HKJCException(501, "No betTypeOdds get from " + Configurator.getInstance().api_bettype
                        + " meetingID:" + meeting_id + " betAcc:" + acc_id);
            }

            String horseData = DataProvider.HttpGet(Configurator.getInstance().api_horse, meeting_id, acc_id,
                    messageID);
            JSONArray horseDataA = DataProvider.getContent(horseData);
            this.horseOdds = Arrays.asList(objectMapper.readValue(horseDataA.toString(), HorseOdd[].class));
            if (this.horseOdds == null || this.horseOdds.size() == 0) {
                throw new HKJCException(501, "No horseOdds get from " + Configurator.getInstance().api_horse
                        + " meetingID:" + meeting_id + " betAcc:" + acc_id);
            }

            String raceProximityData = DataProvider.HttpGet(Configurator.getInstance().api_raceProximity, meeting_id,
                    acc_id, messageID);
            JSONArray raceProximityDataA = DataProvider.getContent(raceProximityData);
            this.raceOdds = Arrays.asList(objectMapper.readValue(raceProximityDataA.toString(), RaceOdd[].class));
            if (this.raceOdds == null || this.raceOdds.size() == 0) {
                throw new HKJCException(501, "No raceOdds get from " + Configurator.getInstance().api_raceProximity
                        + " meetingID:" + meeting_id + " betAcc:" + acc_id);
            }

            String raceTimeData = DataProvider.HttpGet(Configurator.getInstance().api_racetime, meeting_id, null,
                    messageID);
            this.raceTimes = Arrays.asList(objectMapper.readValue(raceTimeData, RaceTimeData[].class));
            if (this.raceTimes == null || this.raceTimes.size() == 0) {
                throw new HKJCException(501, "No raceTimes get from " + Configurator.getInstance().api_racetime
                        + " meetingID:" + meeting_id);
            }

            String winOddsData = DataProvider.HttpGet(Configurator.getInstance().api_winodds, meeting_id, null,
                    messageID);
            this.winOdds = Arrays.asList(objectMapper.readValue(winOddsData, WinOdd[].class));
            if (this.winOdds == null || this.winOdds.size() == 0) {
                throw new HKJCException(501,
                        "No winOdds get from " + Configurator.getInstance().api_winodds + " meetingID:" + meeting_id);
            }

            String cwaGroupData = DataProvider.HttpGet(Configurator.getInstance().api_cwagroup, meeting_id, null,
                    messageID);
            this.cwaGroup = Arrays.asList(objectMapper.readValue(cwaGroupData, CWAGroup[].class));
            // if(this.cwaGroup == null || this.cwaGroup.size() == 0){
            // throw new HKJCException(501, "No cwaGroups get from " +
            // Configurator.getInstance().api_cwagroup+ " meetingID:" + meeting_id);
            // }

            String runnersData = DataProvider.HttpGet(Configurator.getInstance().api_runner, meeting_id, null,
                    messageID);
            this.runners = Arrays.asList(objectMapper.readValue(runnersData, Runner[].class));

            float time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info("[MessageID]:" + messageID + " all apis request time used:" + time + "ms");
        } catch (HKJCException e) {
            Logger logger = Logger.getLogger(APIDataProvider.class);
            logger.error("[MessageID]:" + messageID + e);
            throw e;
        } catch (Exception e) {
            Logger logger = Logger.getLogger(APIDataProvider.class);
            logger.error("[MessageID]:" + messageID + e);
            throw e;
        }

    }
}
