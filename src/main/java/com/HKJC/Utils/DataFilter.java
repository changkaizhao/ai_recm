package com.HKJC.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataFilter {
    public static String metid;
    public static String betacc;

    public static String find(String content, String  reg){
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while (m.find()){
            return  m.group(1);
        }
        return null;
    }
    public static String replace(String content, String mtgid_Key, String mtg_id_val, String betaccKey,String betacc_val){
        String mtgReg = "(?<="+mtgid_Key+"\":\")(\\w+)(?=\")";
        String betReg = "(?<="+betaccKey+"\":\")(\\w+)(?=\")";
        DataFilter.metid = DataFilter.find(content, mtgReg);
        DataFilter.betacc = DataFilter.find(content, betReg);

        content = content.replaceAll(mtgReg, mtg_id_val);
        content = content.replaceAll(betReg, betacc_val);
        return content;
    }
    public static String parseMtg(String mtg){
        String pattern = "(?<=MTG_)(\\w+)(?=_)";
        return DataFilter.find(mtg, pattern);
    }

    public static String reverse(String content){
        return DataFilter.replace(content, "meeting_id", DataFilter.metid, "bet_acc_no", DataFilter.betacc);
    }
}
