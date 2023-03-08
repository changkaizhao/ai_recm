package com.HKJC.OjaiDB.document;

import com.HKJC.Config.Configurator;
import com.HKJC.OjaiDB.db.OjaiDB;
import com.HKJC.OjaiDB.db.OjaiDBStorePool;
import com.HKJC.OjaiDB.db.OjaiDBType;
import com.HKJC.OjaiDB.dto.MaprJsonDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocumentService {
    String tblName;
    Logger logger;
    OjaiDB db;
    ObjectMapper mapper;

    OjaiDBType dbType;

    public DocumentService(OjaiDBType dbt){
        this.logger = LogManager.getLogger(DocumentService.class);
        this.dbType = dbt;
        if(dbt == OjaiDBType.BET_TYPE){
            this.db = OjaiDBStorePool.getInstance().borrowOjaiDBFromBetHorsePool();
        }else if(dbt == OjaiDBType.HORSE){
            this.db = OjaiDBStorePool.getInstance().borrowOjaiDBFromBetHorsePool();
        }else if(dbt == OjaiDBType.RACE_PROXI){
            this.db = OjaiDBStorePool.getInstance().borrowOjaiDBFromRaceProxiPool();
        }
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }



    private String findByID(String queryID) {
        String queryResult = "";
        try {
            queryResult = this.db.findById(queryID);
            // ToDo: logging here
            return queryResult;
        } catch (NullPointerException exec) {
            // ToDo: logging here
            return null;
        }
    }
    public List<String> find(String mtgID, String acctNo){
        String queryID = mtgID + "@" + acctNo;
        String queryResult = "";

        queryResult = this.findByID(queryID);

        // handling null case of query result
        if (queryResult==null) {
            queryID = mtgID + "@0";
            queryResult = this.findByID(queryID);
        }

        if (queryResult==null) {
            return new ArrayList<String>();
        }

        if(this.dbType == OjaiDBType.BET_TYPE){
            OjaiDBStorePool.getInstance().returnOjaiDBToBettypePool(this.db);
        } else if (this.dbType == OjaiDBType.HORSE) {
            OjaiDBStorePool.getInstance().returnOjaiDBToHorsePool(this.db);
        } else if (this.dbType == OjaiDBType.RACE_PROXI) {
            OjaiDBStorePool.getInstance().returnOjaiDBToRaceProxiPool(this.db);
        }

        try {
            /** credit to Plog's answer in https://stackoverflow.com/questions/48343455/spring-boot-convert-complex-json-string-response-to-object */
            MaprJsonDTO json = this.mapper.readValue(queryResult, MaprJsonDTO.class);
            String precomputedStr = json.precomputed_result;
            List<String> precomputedJsonsStr = mapper.readValue(precomputedStr, new TypeReference<List<String>>(){});
            return precomputedJsonsStr;
        } catch (JsonMappingException err) {
            // ToDo: logging here
            return new ArrayList<String>();
        } catch (IOException err) {
            // ToDo: logging here
            return new ArrayList<String>();
        }
    }
}
