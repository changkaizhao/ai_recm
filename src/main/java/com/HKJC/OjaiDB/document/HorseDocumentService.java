package com.HKJC.OjaiDB.document;

import com.HKJC.Data.HorseOdd;
import com.HKJC.OjaiDB.db.OjaiDBType;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HorseDocumentService extends DocumentService {

    public HorseDocumentService() {
        super( OjaiDBType.HORSE);
    }


    public List<HorseOdd> findDTO(String mtgID, String acctNo){
        List<String> queryJSONs = super.find(mtgID, acctNo);

        List<HorseOdd> responseJsonsList = new ArrayList<HorseOdd>();
        if (queryJSONs.size()==0) {
            return responseJsonsList;
        }

        try {
            for(String queryJSON: queryJSONs){
                responseJsonsList.add(mapper.readValue(queryJSON, HorseOdd.class));
            }
            return responseJsonsList;
        } catch (JsonMappingException err) {
            // ToDo: logging here
            return new ArrayList<HorseOdd>();
        } catch (IOException err) {
            // ToDo: logging here
            return new ArrayList<HorseOdd>();
        }
    }
}
