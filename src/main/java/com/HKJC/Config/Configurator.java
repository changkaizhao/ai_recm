package com.HKJC.Config;

import com.HKJC.Main;
import com.HKJC.SolMessager.DataProviderType;
import org.apache.log4j.Logger;

public class Configurator {
    // some constants
    private boolean inited = false;
    public final boolean Debug = false;
    public final DataProviderType dataProviderType = DataProviderType.FileData;

    public final String QUEUE_NAME = "recw/evt/rsinfo";

    // hk/g/prdt/wager/evt/01/upd/ai/bet/recomd_req/{instant_id}/{account_no}/recomd/{status}
    public final String TOPIC = "hk/g/prdt/wager/evt/01/upd/ai/bet/recomd_req/%s/%s/recomd/%s";
    public final String QUEUE_JNDI_NAME = "ai_recw/evt/recomd"; // "recw/evt/rsinfo";
    public final String CONNECTION_FACTORY_JNDI_NAME = "apConnectionFactory";
    public String host;
    public String username;
    public  String password;
    public final String vpnName = "intg";

    public int CPUNum = 32;//Runtime.getRuntime().availableProcessors();
    public  boolean useKerberosAuth = true;

    private  String fixture_endpoint;
    private  String mapr_endpoint ;


    public Boolean SSL_VALIDATE_CERTIFICATE = false;
    public String SSL_TRUST_STORE = "";
    public String SSL_TRUST_STORE_FORMAT = "";
    public String SSL_TRUST_STORE_PASSWORD = "";
    
    public String dataIP = "";

    public final String api_racetime = "/api/racetime";
   public final String api_winodds = "/api/winodds";
   public final String api_cwagroup = "/api/cwagroup";
   public final String api_racepool = "/api/raceandpool";
   public final String api_runner = "/api/runner";


    public final String api_horse = "/horse";
    public final String api_bettype = "/bettype";
    public final String api_raceProximity = "/race_proximity";


    public final String tbl_path_bettype = "/exthcp/airecw/rs/table/bettype-selection-precompute";
    public final String tbl_path_horse = "/exthcp/airecw/rs/table/horse-selection-precompute";
    public final String tbl_path_raceproxi = "/exthcp/airecw/rs/table/raceprox-selection-precompute";

    public final String api_saveHorse = "/record/output/horse";
    public final String api_saveBettype = "/record/output/bettype";

    public String mapr_connection = "";
    public String fileRootPath = "data/";
    // singleton
    private static Configurator instance = null;

    public String genAPI(String api){
        return this.mapr_endpoint + api;
    }

    public String genAPI(String api, String meeting_id){
        return this.fixture_endpoint + api + "?meeting_id="+meeting_id;
    }

    public String genAPI(String api, String meeting_id, String acct_id){
        return this.mapr_endpoint + api + "/"+meeting_id+"/"+acct_id;
    }
    private String getMaprConnectionString() throws Exception{
        String hostname = "";
        String port = "";
        String user = "";
        String password = "mapr";
        String certPath = "";
        String nodeFQDN = "";

        try{ hostname = System.getenv("MAPRDB_HOSTNAME");}catch (Exception e){ throw new Error("MAPRDB_HOSTNAME env var is not defined");}
        try{ port = System.getenv("MAPRDB_PORT");}catch (Exception e){throw new Error("MAPRDB_PORT env var is not defined");}
        try{  user = System.getenv("MAPRDB_USER");}catch (Exception e){throw new Error("MAPRDB_USER env var is not defined");}
        try{  certPath = System.getenv("MAPRDB_CERT_PATH");}catch (Exception e){throw new Error("MAPRDB_CERT_PATH env var is not defined");}
        try{  nodeFQDN = System.getenv("MAPRDB_NODE_FQDN");}catch (Exception e){throw new Error("MAPRDB_NODE_FQDN env var is not defined");}
        return "ojai:mapr:@" + hostname + ":" + port + "?auth=basic;user=" + user + ";password=" + password + ";ssl=true;sslCA=" + certPath + ";sslTargetNameOverride=" + nodeFQDN;

    }
    public void init() throws Exception{
        if(this.Debug){ return; }
        Logger logger = Logger.getLogger(Configurator.class);

        try{this.host = System.getenv("CAP_SOLACE");}catch (Exception e){logger.error(new Error("CAP_SOLACE env var is not defined")); throw e;}
        try{this.username = System.getenv("SOL_USR");}catch (Exception e){logger.error(new Error("SOL_USR env var is not defined"));throw e;}
        try{this.password = System.getenv("SOL_PWD");}catch (Exception e){logger.error(new Error("SOL_PWD env var is not defined"));throw e;}
        try{this.fixture_endpoint = System.getenv("FIXTURE_ENDPOINT");}catch (Exception e){logger.error(new Error("FIXTURE_ENDPOINT env var is not defined"));throw e;}
//        try{instance.mapr_endpoint = System.getenv("MAPR_ENDPOINT");}catch (Exception e){logger.error(new Error("MAPR_ENDPOINT env var is not defined"));}

        try{
            this.SSL_VALIDATE_CERTIFICATE = Boolean.parseBoolean(System.getenv("SSL_VALIDATE_CERTIFICATE"));
        }catch (Exception e){
            this.SSL_VALIDATE_CERTIFICATE = false;
            logger.warn("SSL_VALIDATE_CERTIFICATE env var is not defined, use default false.");
            throw e;
        }
        try{this.SSL_TRUST_STORE = System.getenv("SSL_TRUST_STORE");}catch (Exception e){logger.error(new Error("SSL_TRUST_STORE env var is not defined"));throw e;}
        try{this.SSL_TRUST_STORE_FORMAT = System.getenv("SSL_TRUST_STORE_FORMAT");}catch (Exception e){logger.error(new Error("SSL_TRUST_STORE_FORMAT env var is not defined"));throw e;}
        try{this.SSL_TRUST_STORE_PASSWORD = System.getenv("SSL_TRUST_STORE_PASSWORD");}catch (Exception e){logger.error(new Error("SSL_TRUST_STORE_PASSWORD env var is not defined"));throw e;}

        try{this.fileRootPath = System.getenv("FILE_ROOT_PATH");}catch (Exception e){logger.error(new Error("FILE_ROOT_PATH env var is not defined"));throw e;}

        try{
            this.useKerberosAuth = Boolean.parseBoolean(System.getenv("USE_KERBEROS"));
        }catch (Exception e){
            this.useKerberosAuth = false;
            logger.warn("USE_KERBEROS env var is not defined, use default false.");
        }

        try{
            this.CPUNum = Integer.parseInt(System.getenv("THREAD_SIZE"));
        }catch (Exception e){
            this.CPUNum = 2;
            logger.warn("THREAD_SIZE env var is not defined, use default 2");
        }

        try{this.mapr_connection = this.getMaprConnectionString();}catch (Exception e){
            logger.error(e);
            throw e;
        }

        this.inited = true;
    }
    public static Configurator getInstance() {
        if (instance == null) {
            instance = new Configurator();
        }
        return instance;
    }

}
