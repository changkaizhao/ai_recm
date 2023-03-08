package com.HKJC.OjaiDB.db;

import org.ojai.Document;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;

public class OjaiDB {
    private String connStr;
    private String tablePath;
    private Connection connection;
    private DocumentStore store;

    public String getConnStr() {
        return this.connStr;
    }

    public OjaiDB(String connStr, String tablePath) {
        this.connStr = connStr;
        this.tablePath = tablePath;
        this.connect();
    }

    private void connect() {
        this.connection = DriverManager.getConnection(this.connStr);
        this.store = this.connection.getStore(this.tablePath);
    }

    public void reconnect() {
        this.close();
        this.connect();
    }

    public String findById(String id) throws NullPointerException {
        //StringBuffer stringBuffer = new StringBuffer();
        Document doc = this.store.findById(id);
        return doc.asJsonString();
    }

    public void close() {
        this.store.close();
        this.connection.close();
    }
}
