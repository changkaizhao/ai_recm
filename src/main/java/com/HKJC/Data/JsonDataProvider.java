package com.HKJC.Data;

import com.HKJC.Main;
import com.HKJC.RaceSelector.RaceTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

class JsonData {
        @JsonProperty
        public String WinOddsRangeMappingFilePath;

        @JsonProperty
        public String AcctIdMappingPath;

        @JsonProperty
        public String BetTypeOddPath;

        @JsonProperty
        public String CWAGroupPath;

        @JsonProperty
        public String HorseOddPath;

        @JsonProperty
        public String RaceOddPath;

        @JsonProperty
        public String RaceTimePath;

        @JsonProperty
        public String WinOddPath;

        @JsonProperty
        public String RacePoolPath;

        @JsonProperty
        public String RunnerPath;
}

public class JsonDataProvider extends DataProvider{
        private final String dataPath = "data/data.json";
        private JsonData dataConfig;



        // More data to be read...

        public JsonDataProvider(int race_no) {
                try {
                        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        DateFormat df = new SimpleDateFormat(RaceTime.TimePattern);
                        objectMapper.setDateFormat(df);

                        this.dataConfig = objectMapper.readValue(new File(this.dataPath), JsonData.class);

                        this.worMapping = Arrays
                                .asList((WinOddsRangeMapping[]) objectMapper.readValue(
                                        new File(this.dataConfig.WinOddsRangeMappingFilePath),
                                        WinOddsRangeMapping[].class));

                        this.acctIDMapping = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.AcctIdMappingPath),
                                        AcctIdMapping[].class));
                        this.betTypeOdds = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.BetTypeOddPath),
                                        BetTypeOdd[].class));
                        this.horseOdds = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.HorseOddPath),
                                        HorseOdd[].class));
                        this.raceOdds = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.RaceOddPath),
                                        RaceOdd[].class));

                        this.cwaGroup = Arrays.asList(objectMapper.readValue(new File(this.dataConfig.CWAGroupPath),
                                CWAGroup[].class));
                        this.raceTimes = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.RaceTimePath),
                                        RaceTimeData[].class));
                        this.winOdds = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.WinOddPath),
                                        WinOdd[].class));

                        this.racepool = Arrays
                                .asList(objectMapper.readValue(new File(this.dataConfig.RacePoolPath),
                                        RaceStatus[].class));

                        this.runners = Arrays.asList(objectMapper.readValue(new File(this.dataConfig.RunnerPath),
                                Runner[].class));

                } catch (Exception e) {
                        Logger logger = Logger.getLogger(JsonDataProvider.class);
                        logger.error(e);
                }
        }
}