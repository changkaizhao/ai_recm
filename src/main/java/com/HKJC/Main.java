package com.HKJC;

import com.HKJC.BetPool.BetFilter;
import com.HKJC.Config.Configurator;
import com.HKJC.Exceptions.HKJCException;
import com.HKJC.OjaiDB.db.OjaiDBStorePool;
import com.HKJC.RaceSelector.RaceTime;
import com.HKJC.RatingCalculator.BetType;
import com.HKJC.Recommendation.*;
import com.HKJC.SolMessager.DataProviderType;
import com.HKJC.SolMessager.RecomendDataProvider;
import com.HKJC.SolMessager.Response;
import com.HKJC.SolMessager.SolMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.util.concurrent.CountDownLatch;

import com.HKJC.Utils.DataFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.*;

import javax.jms.*;

import javax.jms.Queue;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.message.SolObjectMessage;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import org.ojai.store.DriverManager;

public class Main implements Runnable {
    final static String QUEUE_NAME = Configurator.getInstance().QUEUE_NAME;

    // hk/g/prdt/wager/evt/01/upd/ai/bet/recomd_req/{instant_id}/{account_no}/recomd/{status}
    final static String TOPIC = Configurator.getInstance().TOPIC;
    final static String QUEUE_JNDI_NAME = Configurator.getInstance().QUEUE_JNDI_NAME;
    final static String CONNECTION_FACTORY_JNDI_NAME = Configurator.getInstance().CONNECTION_FACTORY_JNDI_NAME;
    final static String host = Configurator.getInstance().host;
    final static String username = Configurator.getInstance().username;
    final static String password = Configurator.getInstance().password;
    final static String vpnName = Configurator.getInstance().vpnName;

    private CountDownLatch l;

    public Main(CountDownLatch l) {
        this.l = l;
    }

    @Override
    public void run() {
        Logger logger = Logger.getLogger(Main.class);
        try {
            logger.info("Listener start...");
            this.handleMsg();
        } catch (Exception e) {
            logger.error(e);
        }
        this.l.countDown();
    }

    public static void processBettypeNum(Map<String, Integer> bettypeNum) {
        String betKey = "WIN_PLA_W-P";
        String betKey2 = "W-P";
        for (Map.Entry<String, Integer> entry : bettypeNum.entrySet()) {
            if (entry.getKey().equals(betKey) || entry.getKey().equals(betKey2)) {
                int num = entry.getValue();
                bettypeNum.put("WIN", num);
                bettypeNum.put("PLA", num);
                bettypeNum.put("W-P", num);
                bettypeNum.remove(betKey);
                break;
            }
        }
    }

    private void handleMsg() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        Logger logger = Logger.getLogger(Main.class);
        logger.info("QueueConsumer is connecting to Solace messaging at %s...%n" + host);
        Connection connection;
        MessageConsumer messageConsumer;
        Session session;
        InitialContext initialContext = null;

        if (Configurator.getInstance().useKerberosAuth) {

            SolConnectionFactory factory = SolJmsUtility.createConnectionFactory();
            factory.setHost(Configurator.getInstance().host);
            factory.setVPN(vpnName);
            factory.setAuthenticationScheme(JCSMPProperties.AUTHENTICATION_SCHEME_GSS_KRB);
            factory.setReconnectRetries(600);

            /* create the connection */
            connection = factory.createConnection();

            /* create the session */
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            /* set the exception listener */
            // connection.setExceptionListener(this);

            /* create the destination */
            Queue destination = session.createQueue(QUEUE_JNDI_NAME);

            /* create the consumer */
            messageConsumer = session.createConsumer(destination);

        } else {
            // setup environment variables for creating of the initial context
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            // use the Solace JNDI initial context factory
            env.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");

            // assign Solace message router connection parameters
            env.put(InitialContext.PROVIDER_URL, host);
            env.put(Context.SECURITY_PRINCIPAL, username + '@' + vpnName); // Formatted as user@message-vpn
            env.put(Context.SECURITY_CREDENTIALS, password);

            // Create the initial context that will be used to lookup the JMS Administered
            // Objects.
            initialContext = new InitialContext(env);
            // Lookup the connection factory
            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext
                    .lookup(CONNECTION_FACTORY_JNDI_NAME);

            // Create connection to the Solace router
            connection = connectionFactory.createConnection();

            // Create a non-transacted, client ACK session.
            // session = connection.createSession(false,
            // SupportedProperty.SOL_CLIENT_ACKNOWLEDGE);
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            logger.info("Connected to the Solace Message VPN '%s' with client username '%s'.%n" + vpnName + " " +
                    username);

            // Lookup the queue.
            Queue queue = (Queue) initialContext.lookup(QUEUE_JNDI_NAME);

            // From the session, create a consumer for the destination.
            messageConsumer = session.createConsumer(queue);
        }

        // Use the anonymous inner class for receiving messages asynchronously
        messageConsumer.setMessageListener((MessageListener) new MessageListener() {
            @Override
            public void onMessage(Message message) {

                Logger logger = Logger.getLogger(Main.class);
                String messageID = "";
                String betAcc = "";

                try {
                    long starttime = System.nanoTime();

                    Destination d = message.getJMSDestination();
                    messageID = Main.parseInstanceID(d.toString());
                    betAcc = Main.parseAcctID(d.toString());

                    String logPrefix = "[MessageID]:" + messageID;

                    logger.info(logPrefix + " Received Topic:" + d + " betAccNo:" + betAcc);
                    if (messageID == null || betAcc == null) {
                        Exception e = new Exception("No valid info of instanceID or betAcc from topic" + d);
                        logger.error(logPrefix + " " + e);
                        message.acknowledge();
                        return;
                    }
                    if (message instanceof SolObjectMessage) {
                        // message).message);
                        SolObjectMessage msg = (SolObjectMessage) message;

                        logger.info(logPrefix + " ObjextMessage received: '%s'%n" + msg.getObject().toString());

                        SolMessage slm = (SolMessage) msg.getObject();
                        // instanceID = Main.parseInstanceID(slm.header.event_prefix_topic);

                        logger.info(logPrefix + " SolMessage received: '%s'" + slm);

                        RecommendRequest rr = new RecommendRequest();
                        BetFilter bf = new BetFilter(slm.bet_acc_no, slm.betfilter);
                        rr.betfilter = bf;
                        rr.bettingID = slm.bet_acc_no;
                        rr.meetingID = bf.filter_bets.get(0).meeting_id;

                        logger.info(rr.bettingID + " " + rr.raceNo + " " + rr.meetingID);

                        if (slm.bettype_display_num == null) {
                            rr.betconfirmation_display_num = slm.betconfirmation_display_num;
                            rr.betslip_display_num = slm.betslip_display_num;
                            rr.startTime = slm.start_time;
                            rr.race_id = slm.race_id;
                            Response r = Main.recommend(rr);
                            Main.publish(session, r.data, r.message, 200, messageID, slm.bet_acc_no, false,
                                    slm.request_id);
                            logger.info(r);
                        } else {
                            rr.bettype_display_num = slm.bettype_display_num;
                            Main.processBettypeNum(rr.bettype_display_num);
                            rr.raceNo = slm.race_no;
                            rr.race_id = slm.race_id;
                            Response r = Main.allScore(rr);
                            Main.publish(session, r.data, r.message, 200, messageID, slm.bet_acc_no, true,
                                    slm.request_id);
                            logger.info(r);
                        }

                    } else if (message instanceof TextMessage) {
                        logger.info(logPrefix + " TextMessage received: '%s'%n" + ((TextMessage) message).getText());

                    } else if (message instanceof BytesMessage) {
                        logger.info(logPrefix + " BytesMessage received." + message.getPropertyNames());
                        SolMessage slm = null;
                        try {
                            BytesMessage msg = (BytesMessage) message;
                            byte[] payload = new byte[(int) msg.getBodyLength()];
                            msg.readBytes(payload);
                            String data = new String(payload, StandardCharsets.UTF_8);

                            logger.info(logPrefix + " Data Received: " + data);

                            if (Configurator.getInstance().dataProviderType == DataProviderType.JSONFileData) {
                                // replace all meeting id and bet acc and race_id
                                data = DataFilter.replace(data, "meeting_id", "MTG_20210314_0001", "bet_acc_no",
                                        "12345678");
                                if (Configurator.getInstance().Debug) {
                                    logger.info(logPrefix + " Data Replaced: " + data);
                                }
                            }

                            ObjectMapper objectMapper = new ObjectMapper();
                            slm = objectMapper.readValue(data, SolMessage.class);

                            slm.bet_acc_no = betAcc;
                            RecommendRequest rr = new RecommendRequest();
                            BetFilter bf = new BetFilter(slm.bet_acc_no, slm.betfilter);
                            rr.betfilter = bf;
                            rr.bettingID = slm.bet_acc_no;
                            rr.meetingID = slm.meeting_id;
                            float end = (System.nanoTime() - starttime) / (float) 1000000.0;
                            logger.info(logPrefix + " ⏰[ListenerInitRequest]:" + end + "ms");
                            // TOBE Fixed here ...
                            if (slm.bettype_display_num == null) {
                                logger.info(logPrefix + " Request BetSlip and BetConfirmation API");
                                rr.betconfirmation_display_num = slm.betconfirmation_display_num;
                                rr.betslip_display_num = slm.betslip_display_num;
                                rr.startTime = slm.start_time;
                                rr.race_id = slm.race_id;
                                rr.messageID = messageID;
                                Response r = Main.recommend(rr);
                                logger.info(logPrefix + " Successfully processed BetConfirmation API");
                                Main.publish(session, r.data, r.message, r.code, messageID, betAcc, false,
                                        slm.request_id);

                            } else {
                                logger.info(logPrefix + " Request Bettype API");
                                rr.bettype_display_num = slm.bettype_display_num;
                                Main.processBettypeNum(rr.bettype_display_num);
                                rr.raceNo = slm.race_no;
                                rr.race_id = slm.race_id;
                                rr.messageID = messageID;
                                Response r = Main.allScore(rr);
                                logger.info(logPrefix + " Successfully processed bettype API" + r);
                                Main.publish(session, r.data, r.message, r.code, messageID, betAcc, true,
                                        slm.request_id);
                            }

                        } catch (Exception e) {
                            StackTraceElement ste = e.getStackTrace()[0];
                            logger.error(e + " " + ste.getClassName() + " " + ste.getMethodName() + " "
                                    + ste.getLineNumber());
                            Main.publish(session, null, e.getMessage(), 500, messageID, betAcc, false, slm.request_id);
                        }
                        float time = (System.nanoTime() - starttime) / (float) 1000000.0;
                        logger.info(logPrefix + " ⏰[OneMsgProcessingTotal]:" + time + "ms");
                    } else {
                        logger.info(logPrefix + "Other type Message received.");
                        logger.info(logPrefix + "Message Content:%n%s%n" + SolJmsUtility.dumpMessage(message));
                    }

                    // ACK the received message manually because of the set
                    // SupportedProperty.SOL_CLIENT_ACKNOWLEDGE above
                    message.acknowledge();

                    // latch.countDown(); // unblock the main thread
                } catch (JMSException ex) {
                    logger.error("[MessageID]:" + messageID + "Error processing incoming message.");
                    ex.printStackTrace();
                    try {
                        message.acknowledge();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                    latch.countDown(); // unblock the thread
                }
            }
        });

        // Start receiving messages
        connection.start();
        logger.info("Awaiting message...");
        // the main thread blocks at the next statement until a message received
        latch.await();

        connection.stop();
        // Close everything in the order reversed from the opening order
        // NOTE: as the interfaces below extend AutoCloseable,
        // with them it's possible to use the "try-with-resources" Java statement
        // see details at
        // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        messageConsumer.close();
        session.close();
        connection.close();
        // The initial context needs to be close; it does not extend AutoCloseable
        if (!Configurator.getInstance().useKerberosAuth && initialContext != null) {
            initialContext.close();
        }
    }

    public static String parseInstanceID(String topic) {
        String instancePattern = "(?<=recomd_req/)(\\w+)(?=/)";
        return DataFilter.find(topic, instancePattern);
    }

    public static String parseAcctID(String topic) {
        String acctP = "(?<=recomd_req/\\w+/)(\\w+)(?=/)";
        return DataFilter.find(topic, acctP);
    }

    public static void publish(Session session, Serializable data, String err_message, int status, String messageID,
            String betacc, boolean isBettype, String request_id) {
        long starttime = System.nanoTime();

        Logger logger = Logger.getLogger(Main.class);
        String logPrefix = "[MessageID]:" + messageID;
        try {

            // // Programmatically create the connection factory using default settings
            // SolConnectionFactory connectionFactory =
            // SolJmsUtility.createConnectionFactory();
            //
            // if(Configurator.getInstance().useKerberosAuth){
            //// connectionFactory.setSSLKeyStore(Configurator.getInstance().SSL_TRUST_STORE);
            //// connectionFactory.setSSLKeyStoreFormat(Configurator.getInstance().SSL_TRUST_STORE_FORMAT);
            //// connectionFactory.setSSLKeyStorePassword(Configurator.getInstance().SSL_TRUST_STORE_PASSWORD);
            //// connectionFactory.setSSLValidateCertificate(Configurator.getInstance().SSL_VALIDATE_CERTIFICATE);
            //
            // connectionFactory.setHost(Configurator.getInstance().host);
            // connectionFactory.setVPN(vpnName);
            // connectionFactory.setAuthenticationScheme(JCSMPProperties.AUTHENTICATION_SCHEME_GSS_KRB);
            // connectionFactory.setReconnectRetries(600);
            // }else{
            // connectionFactory.setHost(host);
            // connectionFactory.setVPN(vpnName);
            // connectionFactory.setUsername(username);
            // connectionFactory.setPassword(password);
            // }
            //
            //
            // // Create connection to the Solace router
            // Connection connection = connectionFactory.createConnection();
            //
            // // Create a non-transacted, auto ACK session.
            // Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            logger.info(logPrefix + " Connected to the Solace Message VPN '%s' with client username " + vpnName + " " +
                    username);
            float endConnectiontime = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + " ⏰[SolaceConnectionTime]:" + endConnectiontime + "ms");

            String dataString = "";
            if (data instanceof String) {
                dataString = (String) data;
            } else if (data == null && status != 200) {
                dataString = err_message;

            } else if (data == null) {
                dataString = err_message;

            } else {
                ObjectMapper mapper = new ObjectMapper();
                dataString = mapper.writeValueAsString(data);

                try {
                    Recommendation r = (Recommendation) data;
                    if (r.recommendations.bet_type.length != 0 || r.recommendations.betslip.length != 0
                            || r.recommendations.bet_confirmation.length != 0) {
                        // if empty not save
                        String api = isBettype ? Configurator.getInstance().api_saveBettype
                                : Configurator.getInstance().api_saveHorse;
                        String dataStringToSave = dataString;
                        if (!isBettype) {
                            // recommendate add race interval data
                            dataStringToSave = StringUtils.chop(dataStringToSave);
                            dataStringToSave = dataStringToSave + "," + GlobalAttrRaceInterval.getInstance().toString()
                                    + "}";
                        }
                        Main.saveToServer(dataStringToSave, api, messageID);
                    }
                } catch (Exception e) {
                    logger.info(logPrefix + " can't save to server, error:" + e);
                }
            }

            // Create the publishing topic programmatically
            String topic_name = String.format(TOPIC, messageID, betacc, status);

            Topic topic = session.createTopic(topic_name);

            // Create the message producer for the created topic
            MessageProducer messageProducer = session.createProducer(topic);

            // Create the message
            // TextMessage message = session.createTextMessage("Hello world!");
            // ObjectMessage message = session.createObjectMessage(data);
            BytesMessage message = session.createBytesMessage();

            if (Configurator.getInstance().dataProviderType == DataProviderType.JSONFileData) {
                // replace all meeting id and bet acc and race_id
                dataString = DataFilter.reverse(dataString);

            }
            logger.info(logPrefix + " Reply topic: " + topic_name);

            if (dataString.equals("OK")) {
                Recommendation r = Main.emptyReco(isBettype, betacc);
                ObjectMapper mapper = new ObjectMapper();
                dataString = mapper.writeValueAsString(r);
            }

            dataString = StringUtils.chop(dataString);
            dataString = dataString + "," + "\"request_id\":\"" + request_id + "\"}";

            if (status == 200) {
                logger.info(logPrefix + " Reply data: " + dataString);
                // message.writeUTF(dataString);
                message.writeBytes(dataString.getBytes(StandardCharsets.UTF_8));
            } else {
                logger.info(logPrefix + " Error data not replyed:" + status + " " + dataString);
            }

            // System.out.printf("Sending message '%s' to topic '%s'...%n",

            // Send the message
            // NOTE: JMS Message Priority is not supported by the Solace Message Bus
            messageProducer.send(topic, message, DeliveryMode.NON_PERSISTENT,
                    Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
            logger.info(logPrefix + " Sent successfully. Exiting...");

            // Close everything in the order reversed from the opening order
            // NOTE: as the interfaces below extend AutoCloseable,
            // with them it's possible to use the "try-with-resources" Java statement
            // see details at
            // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
            messageProducer.close();
            // session.close();
            // connection.close();
        } catch (Exception e) {
            StackTraceElement ste = e.getStackTrace()[0];
            logger.error(logPrefix + " " + e + " " + ste.getClassName() + " " + ste.getMethodName() + " "
                    + ste.getLineNumber());
        }

        float time = (System.nanoTime() - starttime) / (float) 1000000.0;
        logger.info(logPrefix + " ⏰[MsgPublishingTotal]:" + time + "ms");

    }

    public static Response recommend(RecommendRequest messageObj) {
        String logPrefix = "[MessageID]:" + messageObj.messageID;
        Logger logger = Logger.getLogger(Main.class);

        try {
            String betAcc = messageObj.bettingID;
            String meetingID = messageObj.meetingID;
            long starttime = System.nanoTime();
            RecomendDataProvider rdp = new RecomendDataProvider(meetingID, betAcc, messageObj.messageID, -1);
            float time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + "  ⏰[DataPrepareTotalTime]:" + time + "ms");

            starttime = System.nanoTime();
            BetRecommendationCalculator brc = new BetRecommendationCalculator(meetingID, betAcc, rdp);
            Recommendation rec;
            time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + " ⏰[CalculationInitTime]:" + time + "ms");

            if (messageObj.startTime != null) {
                // if request has start time, use it
                rec = brc.recommendate(messageObj, RaceTime.convertDate(messageObj.startTime));
            } else {
                // if not, use current time
                rec = brc.recommendate(messageObj);
            }

            time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + " ⏰[CalculationTotalTime]:" + time + "ms");
            return Response.success(rec);
        } catch (HKJCException e) {
            StackTraceElement ste = e.getStackTrace()[0];
            logger.error(logPrefix + " " + e + " " + ste.getClassName() + " " + ste.getMethodName() + " "
                    + ste.getLineNumber());
            if (e.type == 501 || e.type == 500) {
                Recommendation rec = Main.emptyReco(false, messageObj.bettingID);
                return Response.success(rec);
            }
            return Response.error(e.type, e.getMessage());
        } catch (Exception e) {
            StackTraceElement ste = e.getStackTrace()[0];
            logger.error(logPrefix + " " + e + " " + ste.getClassName() + " " + ste.getMethodName() + " "
                    + ste.getLineNumber());
            return Response.error(500, "Server error: " + e.toString());
        }

    }

    public static Recommendation emptyReco(boolean isBettype, String bettingID) {
        Recommendation rec = new Recommendation(bettingID);
        RecommendationInternalData rid = null;
        if (isBettype) {
            rid = new RecommendationInternalData(new RecoBet[] {});
        } else {
            rid = new RecommendationInternalData(new RecoBet[] {}, new RecoBet[] {});
        }
        rec.recommendations = rid;
        return rec;
    }

    public static Response allScore(RecommendRequest messageObj) {
        String logPrefix = "[MessageID]:" + messageObj.messageID;
        Logger logger = Logger.getLogger(Main.class);

        try {
            String betAcc = messageObj.bettingID;
            String meetingID = messageObj.meetingID;
            long starttime = System.nanoTime();
            RecomendDataProvider rdp = new RecomendDataProvider(meetingID, betAcc, messageObj.messageID,
                    messageObj.raceNo);
            float time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + "  ⏰[DataPrepareTotalTime]:" + time + "ms");

            starttime = System.nanoTime();
            BetRecommendationCalculator brc = new BetRecommendationCalculator(meetingID, betAcc, rdp);
            time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + " ⏰[CalculationInitTime]:" + time + "ms");

            Recommendation rec = brc.getAllScores(messageObj);

            time = (System.nanoTime() - starttime) / (float) 1000000.0;
            logger.info(logPrefix + " ⏰[CalculationTotalTime]:" + time + "ms");
            return Response.success(rec);
        } catch (HKJCException e) {
            StackTraceElement ste = e.getStackTrace()[0];
            logger.error(logPrefix + " " + e + " " + ste.getClassName() + " " + ste.getMethodName() + " "
                    + ste.getLineNumber());
            if (e.type == 501 || e.type == 500) {
                Recommendation rec = Main.emptyReco(true, messageObj.bettingID);
                return Response.success(rec);
            }
            return Response.error(e.type, e.getMessage());
        } catch (Exception e) {
            StackTraceElement ste = e.getStackTrace()[0];
            logger.error(logPrefix + " " + e + " " + ste.getClassName() + " " + ste.getMethodName() + " "
                    + ste.getLineNumber());
            return Response.error(500, "Server error: " + e.toString());
        }

    }

    // save calculator results to backup server
    public static void saveToServer(String data, String api, String messageID) {
        Logger logger = Logger.getLogger(Main.class);
        long starttime = System.nanoTime();
        try {
            // URL url = new URL(Configurator.getInstance().genAPI(api));
            // URLConnection con = url.openConnection();
            // HttpURLConnection http = (HttpURLConnection) con;
            // http.setRequestMethod("POST");
            // http.setDoOutput(true);
            //
            // byte[] out = data.getBytes(StandardCharsets.UTF_8);
            // http.setFixedLengthStreamingMode(out.length);
            // http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // http.connect();
            // try(OutputStream os = http.getOutputStream()){
            // os.write(out);
            // }
            //
            // http.disconnect();
            // logger.info("[MessageID]:"+ messageID + " success save result to server.");

        } catch (Exception e) {
            logger.error(e);
        }

        float time = (System.nanoTime() - starttime) / (float) 1000000.0;
        logger.info("[MessageID]:" + messageID + " ⏰[SaveToServerTime]:" + time + "ms");
    }

    public static void initLogger() {
        System.setProperty("java.security.auth.login.config", "SolaceGSSConfig/jaas.conf");
        String hostName = System.getenv("HOSTNAME");
        if (hostName != null) {
            System.setProperty("HOSTNAME", hostName);
        }
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);

        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("[%-5p] %d(%r) HOST:" + hostName + " --> [%t] %l: %m %x %n");
        try {
            DailyRollingFileAppender drfa = new DailyRollingFileAppender(layout, "./log/airecw-" + hostName + ".log",
                    "'.'yyyy-MM-dd");
            drfa.setAppend(true);
            drfa.setImmediateFlush(true);
            rootLogger.addAppender(drfa);
        } catch (Exception e) {
            rootLogger.error(e);
            rootLogger.error(new Exception("File logger set not successfully."));
            return;
        }
    }

    public static void initOjaiDBPool(int pool_size) {
        OjaiDBStorePool.getInstance().createOjaiDBsForBetTypePool(pool_size);
        OjaiDBStorePool.getInstance().createOjaiDBsForHorsePool(pool_size);
        OjaiDBStorePool.getInstance().createOjaiDBsForRaceProxiPool(pool_size);
    }

    public static void main(String[] args) {
        Main.initLogger();
        Logger logger = Logger.getLogger(Main.class);
        try {
            Configurator.getInstance().init();
        } catch (Exception e) {
            logger.error(new Error("one or more env not set up"));
            return;
        }
        int tNo = Configurator.getInstance().CPUNum;

        Main.initOjaiDBPool(tNo);

        final CountDownLatch mainLatch = new CountDownLatch(tNo);
        logger.info("Start App with " + tNo + " threads...");
        for (int i = 0; i < tNo; i++) {
            Main listener = new Main(mainLatch);
            Thread t = new Thread(listener);
            t.start();
        }
        logger.info("Listener running...");
        try {
            mainLatch.await();
        } catch (InterruptedException e) {
            logger.error(e);
            Thread.currentThread().interrupt();
        }
    }
}
