package com.HKJC.OjaiDB.document;

import com.HKJC.Data.RaceOdd;
import com.HKJC.OjaiDB.db.OjaiDBType;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class RaceProximityDocumentService extends DocumentService{
    public RaceProximityDocumentService() {
        super(OjaiDBType.RACE_PROXI);
    }


    public List<RaceOdd> findDTO(String mtgID, String acctNo){
        List<String> queryJSONs = super.find(mtgID, acctNo);

        List<RaceOdd> responseJsonsList = new ArrayList<RaceOdd>();
        if (queryJSONs.size()==0) {
            return responseJsonsList;
        }

        try {
            for(String queryJSON: queryJSONs){
                responseJsonsList.add(mapper.readValue(queryJSON, RaceOdd.class));
            }
            return responseJsonsList;
        } catch (JsonMappingException err) {
            // ToDo: logging here
            return new ArrayList<RaceOdd>();
        } catch (IOException err) {
            // ToDo: logging here
            return new ArrayList<RaceOdd>();
        }
    }
}
