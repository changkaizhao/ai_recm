package com.HKJC.OjaiDB.document;

import com.HKJC.Data.BetTypeOdd;
import com.HKJC.OjaiDB.db.OjaiDBType;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BettypeDocumentService extends DocumentService{

    public BettypeDocumentService() {
        super( OjaiDBType.BET_TYPE);
    }


    public List<BetTypeOdd> findDTO(String mtgID, String acctNo){
        List<String> queryJSONs = super.find(mtgID, acctNo);

        List<BetTypeOdd> responseJsonsList = new ArrayList<BetTypeOdd>();
        if (queryJSONs.size()==0) {
            return responseJsonsList;
        }

        try {
            for(String queryJSON: queryJSONs){
                responseJsonsList.add(mapper.readValue(queryJSON, BetTypeOdd.class));
            }
            return responseJsonsList;
        } catch (JsonMappingException err) {
            // ToDo: logging here
            return new ArrayList<BetTypeOdd>();
        } catch (IOException err) {
            // ToDo: logging here
            return new ArrayList<BetTypeOdd>();
        }
    }
}
