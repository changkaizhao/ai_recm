package com.HKJC.OjaiDB.db;

import com.HKJC.Config.Configurator;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OjaiDBStorePool {
    private ConcurrentLinkedQueue<OjaiDB> bettype_deque;
    private ConcurrentLinkedQueue<OjaiDB> horse_deque;
    private ConcurrentLinkedQueue<OjaiDB> race_proxi_deque;
    private String connStr;

    private static OjaiDBStorePool storePool;

    private OjaiDBStorePool(String connStr) {
        //this.db = new OjaiDB(connStr, tablePath);
        this.connStr = connStr;
        this.bettype_deque = new ConcurrentLinkedQueue<OjaiDB>();
        this.horse_deque = new ConcurrentLinkedQueue<OjaiDB>();
        this.race_proxi_deque = new ConcurrentLinkedQueue<OjaiDB>();

    }

    public static OjaiDBStorePool getInstance(){
        if (storePool == null) {
            storePool = new OjaiDBStorePool(Configurator.getInstance().mapr_connection);
        }
        return storePool;
    }

    private void createOneOjaiDBForBetTypePool() {
        OjaiDB db = new OjaiDB(this.connStr, Configurator.getInstance().tbl_path_bettype);
        this.bettype_deque.add(db);
    }

    public void createOjaiDBsForBetTypePool(int num) {
        for (int i=0; i<num; i++){
            createOneOjaiDBForBetTypePool();
        }
    }

    public OjaiDB borrowOjaiDBFromBetTypePool() {
        if (this.bettype_deque.isEmpty()) {
            this.createOneOjaiDBForBetTypePool();
        }

        OjaiDB db = this.bettype_deque.poll();
        return db;
    }

    public void returnOjaiDBToBettypePool(OjaiDB db) {
        this.bettype_deque.add(db);
    }

    //===================================================================================
    private void createOneOjaiDBForHorsePool() {
        OjaiDB db = new OjaiDB(this.connStr, Configurator.getInstance().tbl_path_horse);
        this.horse_deque.add(db);
    }

    public void createOjaiDBsForHorsePool(int num) {
        for (int i=0; i<num; i++){
            createOneOjaiDBForHorsePool();
        }
    }

    public OjaiDB borrowOjaiDBFromBetHorsePool() {
        if (this.horse_deque.isEmpty()) {
            this.createOneOjaiDBForHorsePool();
        }

        OjaiDB db = this.horse_deque.poll();
        return db;
    }

    public void returnOjaiDBToHorsePool(OjaiDB db) {
        this.horse_deque.add(db);
    }

    //===================================================================================
    private void createOneOjaiDBForRaceProxiPool() {
        OjaiDB db = new OjaiDB(this.connStr, Configurator.getInstance().tbl_path_raceproxi);
        this.race_proxi_deque.add(db);
    }

    public void createOjaiDBsForRaceProxiPool(int num) {
        for (int i=0; i<num; i++){
            createOneOjaiDBForRaceProxiPool();
        }
    }

    public OjaiDB borrowOjaiDBFromRaceProxiPool() {
        if (this.race_proxi_deque.isEmpty()) {
            this.createOneOjaiDBForRaceProxiPool();
        }

        OjaiDB db = this.race_proxi_deque.poll();
        return db;
    }

    public void returnOjaiDBToRaceProxiPool(OjaiDB db) {
        this.race_proxi_deque.add(db);
    }
}