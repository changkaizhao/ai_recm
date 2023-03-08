package hkjc.edw.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.message.SolBytesMessage;
import com.solacesystems.jms.message.SolTextMessage;
import HKJC.EDW.Lib.Cryptography;

public class MsgListener implements ExceptionListener {
    /*-----------------------------------------------------------------------
     * Parameters
     *----------------------------------------------------------------------*/
    int envID;

    //// String localMessagePath =
    //// Common.getFileFullPath(Constants.currentDirectory, "\\local\\message");
    //// String localLogPath =
    //// Common.getFileFullPath(Constants.currentDirectory, "\\local\\log");

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/

    public MsgListener(String[] args) throws Throwable {
        ArrayList<ServerConfig> serverConfigs = new ArrayList<ServerConfig>();

        // get env_id
        DbaDbDao dba_db = new DbaDbDao();
        envID = dba_db.getEnvID(Config.getEnvironment());

        // get all active server configurations
        serverConfigs = Config.getServerConfig();

        if (serverConfigs.size() == 0)
            Logger.addNewListenerLog(dba_db, "No active listener configuration!", Constants.log_type_warning_string,
                    null);
        else {
            /*
             * sam Timer listenerProcessLocalMsgLogTimer = new
             * Timer("EDW JMS Listener Process Local Message Timer", false); //
             * schedule a task to process the local message and log
             * listenerProcessLocalMsgLogTimer.schedule(new TimerTask() { public
             * void run() { try { // archive the current local message file
             * Common.ArchiveLocalFile(localMessagePath,
             * Constants.batch_type_message);
             *
             * dba_db.saveMsgFromLocalFile(localMessagePath,
             * Constants.archive_local_message_file + "*",
             * Constants.column_separator, Constants.row_separator); } catch
             * (Throwable e) { Logger.addNewListenerLog(dba_db,
             * "Process local message and log failed! Error message: " +
             * e.toString(), Constants.log_type_error_string, null); } } }, 0,
             * 1000 * 60);
             */

            /*
             * schedule a task to add listener log every 10 minutes, to indicate
             * the listener is running, and check if the thread is available, if
             * not, then start it again
             */
            Timer listenerIntervalTimer = new Timer("EDW JMS Listener Interval Timer", false);
            listenerIntervalTimer.schedule(new TimerTask() {
                                               public void run() {
                                                   try {
                                                       int i = 0;
                                                       int j = 0;
                                                       for (Thread t : Thread.getAllStackTraces().keySet()) {
                                                           if (t.getName().contains(Constants.listener_name)) {
                                                               i += 1;

                                                               if (t.isAlive() == false) {
                                                                   Logger.addNewListenerLog(dba_db,
                                                                           "The listener '" + t.getName() + "' is down, will try to re-launch it.",
                                                                           Constants.log_type_warning_string, null);
                                                                   t.run();
                                                                   if (t.isAlive()) {

                                                                       Logger.addNewListenerLog(dba_db,
                                                                               "Start the listener '" + t.getName() + "' successfully.",
                                                                               Constants.log_type_warning_string, null);
                                                                   }
                                                               } else {
                                                                   // if the thread state is not runnable, then
                                                                   // interrupt the thread
                                                                   Thread.State s = t.getState();
                                                                   if (s.equals(Thread.State.NEW))
                                                                       t.start();
                                                                   else if (s.equals(Thread.State.WAITING) || s.equals(Thread.State.BLOCKED)
                                                                           || s.equals(Thread.State.TERMINATED)) {
                                                                       j += 1;
                                                                       t.interrupt();
                                                                   }

                                                                   s = null;
                                                               }
                                                           }
                                                       }
                                                       String remark = "";
                                                       remark = i + " JMS Listener in total, " + j + " of them will be re-launch.";
                                                       Logger.addNewListenerLog(dba_db, remark, Constants.log_type_normal_string, null);
                                                   } catch (Throwable e) {
                                                       if (e.toString()
                                                               .equalsIgnoreCase("javax.jms.JMSException: Thread has been interrupted") == false)
                                                           Logger.addNewListenerLog(dba_db, "Error message: " + e.toString(),
                                                                   Constants.log_type_warning_string, null);
                                                   }
                                               }
                                           }, Constants.reporting_interval_minutes * 1000 * 60 + 10000,
                    Constants.reporting_interval_minutes * 1000 * 60);

            /*
             * DateTimeFormatter dtf =
             * DateTimeFormatter.ofPattern("yyyy-MM-dd"); String startTimeStr =
             * dtf.format(LocalDateTime.now()) + " 09:20:30"; SimpleDateFormat
             * sdtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); Date d =
             * sdtf.parse(startTimeStr);
             *
             * Timer listenerInterruptTimer = new
             * Timer("EDW JMS Listener Interrupt Timer", false);
             * listenerInterruptTimer.scheduleAtFixedRate(new TimerTask() {
             * public void run() { try { for (Thread t :
             * Thread.getAllStackTraces().keySet()) { if
             * (t.getName().contains(Constants.listener_name)) { if
             * (t.isInterrupted() == false) { t.interrupt();
             * t.Logger.addNewListenerLog(dba_db, "Interrupted the listener '" +
             * t.getName() + "'", Constants.log_type_normal_string, null); } } }
             * } catch (Throwable e) {
             * System.err.println("Interrupt listener failed! Error message: " +
             * e.toString()); } } // }, Constants.reporting_interval_minutes *
             * 1000 * 60, // Constants.reporting_interval_minutes * 1000 * 60);
             * }, d, 1000 * 90);
             */

            Logger.addNewListenerLog(dba_db, "====================================================",
                    Constants.log_type_normal_string, null);
            Logger.addNewListenerLog(dba_db, "Starting EDW JMS Listener", Constants.log_type_normal_string, null);
            String rmk = "";
            if (serverConfigs.size() == 1)
                rmk = "There is one active server configuration.";
            else
                rmk = "There are " + serverConfigs.size() + " active server configurations.";
            Logger.addNewListenerLog(dba_db, rmk, Constants.log_type_normal_string, null);
            for (ServerConfig c : serverConfigs)
                Common.PrintServerConfig(c);
            Logger.addNewListenerLog(dba_db, "====================================================",
                    Constants.log_type_normal_string, null);

            // Start to listen message for each active queue
            int i = 1;
            for (ServerConfig c : serverConfigs) {
                LaunchNewListener(Constants.listener_name + c.getConfigID(), c, i);
                i += 1;
            }

            // Add a timer to check if there is new active configure, if yes,
            // then just launch it
            /*
             * Timer listenerTimer = new
             * Timer("EDW JMS Listener Check New Config Timer", false);
             * listenerTimer.schedule(new TimerTask() { public void run() { //
             * for (Thread t : Thread.getAllStackTraces().keySet()) { // if
             * (t.getName().contains(Constants.listener_name)) //
             * Logger.addNewListenerLog(dba_db, t.getName(), //
             * Constants.log_type_normal_string, null); // }
             *
             * // check if there is new active config, if yes, then // launch it
             * serverConfigs = Config.getServerConfig(); for (ServerConfig c :
             * serverConfigs) { boolean configExistFlag = false; for
             * (ServerConfig c2 : currentConfigs) { if (c2.getConfigID() ==
             * c.getConfigID()) configExistFlag = true; } if (configExistFlag ==
             * false) { Logger.addNewListenerLog(dba_db,
             * "Found a new active listener configuration '" + c.getConfigDesc()
             * + "', will start to launch it.",
             * Constants.log_type_normal_string, null);
             * LaunchNewListener(Constants.listener_name + c.getConfigID(), c,
             * 2); }
             *
             * } } }, 30000, 60000);
             */

        }

    }

    /*---------------------------------------------------------------------
     * onException
     *---------------------------------------------------------------------*/
    public void onException(JMSException e) {
        /* print the connection exception status */
        System.err.println("CONNECTION EXCEPTION: " + e.getMessage());
    }

    /*-----------------------------------------------------------------------
     * run
     *----------------------------------------------------------------------*/

    void lanchSolaceListener(Timer timer, ServerConfig c) throws Throwable {
        boolean continueFlag = true;
        Message msg = null;

        int serverConfigID = 0;
        String serverConfigDesc = "";
        String host = "";
        String queue = "";
        String username = "";
        String password = null;
        String vpn = "";
        String kerberosServer = "";
        boolean useKerberosAuth = false;
        boolean saveDBLog = true;

        javax.jms.Connection connection = null;
        Session session = null;
        MessageConsumer msgConsumer = null;
        Destination destination = null;
        FlowReceiver cons = null;

        try {
            serverConfigID = c.getConfigID();
            serverConfigDesc = c.getConfigDesc();
            host = c.getServerUrl();
            queue = c.getQueue();
            username = c.getUser();
            password = c.getPwd();
            vpn = c.getVpn();
            kerberosServer = c.getKerberosServer();
            useKerberosAuth = c.getUseKerberosAuth();
            saveDBLog = c.getSaveDBLog();

            SolConnectionFactory factory;

            if (useKerberosAuth == false)
                factory = SolJmsUtility.createConnectionFactory(host, username, password, vpn, null);
            else {

                factory = SolJmsUtility.createConnectionFactory();

                factory.setHost(kerberosServer);
                factory.setVPN(vpn);
                factory.setAuthenticationScheme(JCSMPProperties.AUTHENTICATION_SCHEME_GSS_KRB);
            }

            factory.setReconnectRetries(600);

            /* create the connection */
            connection = factory.createConnection();

            /* create the session */
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            /* set the exception listener */
            connection.setExceptionListener(this);

            /* create the destination */
            destination = session.createQueue(queue);

            /* create the consumer */
            msgConsumer = session.createConsumer(destination);

            /* start the connection */
            connection.start();
        } catch (Exception e) {
            System.out.print("Error: " + e.toString());
            continueFlag = false;
            Logger.addNewListenerLog(null,
                    "Got error as launching JMS listener to listen the queue '" + queue + "'! Error message: "
                            + e.toString() + ". Will retry after " + Constants.retry_interval + " seconds.",
                    Constants.log_type_error_string, null);
        }

        // if got any error as launch JMS listener, it will wait a while, then
        // retry
        if (continueFlag == false) {
            try {
                TimeUnit.SECONDS.sleep(Constants.retry_interval);

                // get the latest configuration
                ServerConfig c2 = Common.GetServerConfig(serverConfigID);
                if (c2 != null)
                    c = c2;
            } catch (Exception e) {
                Logger.addNewListenerLog(null, "Error message: " + e.toString(), Constants.log_type_warning_string,
                        null);
            }

            // re-launch the listener
            lanchSolaceListener(timer, c);
        } else {
            DbaDbDao dba_db = new DbaDbDao();
            String defaultBusinessDate = "";
            try {
                defaultBusinessDate = Common.getBusinessDate(null);

                Logger.addNewListenerLog(dba_db,
                        "Connected to the Solace server '" + host + "', start listening JMS messages from '" + queue
                                + "' for server configuration '" + serverConfigDesc + ".",
                        Constants.log_type_normal_string, null);
            } catch (Exception e) {
                Logger.addNewListenerLog(null, "Error message: " + e.toString(), Constants.log_type_warning_string,
                        null);
            }

            /* read messages */
            while (continueFlag) {
                try {
                    msg = msgConsumer.receive();

                    if (msg != null) {
                        if (msg.propertyExists("CDF*EventType")) {
                            // check if it's heart-beat message, if
                            // heart-beat
                            // message, then do nothing
                            String eventType = "";

                            eventType = msg.getStringProperty("CDF*EventType");
                            if (eventType.equalsIgnoreCase("HeartbeatMsg"))
                                msg.acknowledge();
                        }

                        else {
                            String errMsg = "";
                            String msg_id = "";

                            String SourceSID = "CSA";
                            String TargetSID = "EDW";
                            String ContentID = queue;
                            String SourceContentType = "";
                            String TargetChannel = "Solace";

                            int msgLen = msg.toString().length();
                            String log = "";
                            if (msgLen > Constants.max_msg_length)
                                log = "Receive message from '" + queue + "': "
                                        + msg.toString().substring(1, Constants.max_msg_length - 10) + "..."
                                        + msg.toString().substring(msgLen - 10);
                            else
                                log = "Receive message from '" + queue + "': " + msg.toString();

                            if (saveDBLog)
                                Logger.addNewListenerLog(dba_db, log, Constants.log_type_normal_string, null);
                            else
                                Logger.addNewLog(log, Constants.log_type_normal_string);

                            String BusinessDate = defaultBusinessDate;
                            String OriginalBody = "";

                            if (msg.getClass().getName()
                                    .equalsIgnoreCase("com.solacesystems.jms.message.SolBytesMessage")) {

                                SourceContentType = "BinaryAttachment";
                                SolBytesMessage byteMsg = (SolBytesMessage) msg;
                                if (byteMsg.getBodyLength() > 0) {
                                    byte[] bytes = new byte[(int) byteMsg.getBodyLength()];
                                    byteMsg.readBytes(bytes);
                                    OriginalBody = new String(bytes);
                                }
                            } else if (msg.getClass().getName()
                                    .equalsIgnoreCase("com.solacesystems.jms.message.SolTextMessage")) {
                                SourceContentType = "XmlContent";
                                SolTextMessage textMsg = (SolTextMessage) msg;
                                OriginalBody = textMsg.getText();
                            }

                            String DecryptedBody = "";

                            // save the message to database directly
                            msg_id = dba_db.saveMessage(serverConfigID, SourceSID, TargetSID, ContentID,
                                    SourceContentType, TargetChannel, BusinessDate, msg.toString(), OriginalBody,
                                    DecryptedBody);

                            if (msg_id.isEmpty()) {
                                errMsg = "Save the message to EDW database failed!";
                                Logger.addNewListenerLog(dba_db, errMsg, Constants.log_type_error_string, null);
                            } else
                                msg.acknowledge();
                        }
                    }

                } catch (Exception je) {
                    try {
                        Logger.addNewListenerLog(dba_db, "Error message: " + je.toString(),
                                Constants.log_type_error_string, null);

                        TimeUnit.SECONDS.sleep(Constants.retry_interval);

                        // release the source
                        if (msgConsumer != null) {
                            msgConsumer.close();
                            msgConsumer = null;
                        }
                        if (session != null) {
                            session.close();
                            session = null;
                        }
                        if (connection != null) {
                            connection.close();
                            connection = null;
                        }
                        dba_db.finalize();
                        dba_db = null;

                        // get the latest configuration
                        ServerConfig c2 = Common.GetServerConfig(serverConfigID);
                        if (c2 != null) {
                            c = c2;
                            c2 = null;
                        }

                    } catch (Exception e) {
                        Logger.addNewListenerLog(null, "Error message: " + e.toString(),
                                Constants.log_type_warning_string, null);
                    }

                    // re-launch the listener
                    lanchSolaceListener(timer, c);
                }
            }

            timer.cancel();
            timer.purge();

            Logger.addNewListenerLog(dba_db, "Stop listening JMS messages from '" + queue
                            + "' for server configuration '" + serverConfigDesc + "' successfully.",
                    Constants.log_type_normal_string, null);
        }

    }

    void lanchEMSListener(Timer timer, ServerConfig c) throws Throwable {
        boolean continueFlag = true;
        Message msg = null;

        int serverConfigID = 0;
        String serverConfigDesc = "";
        String emsServerUrl = "";
        String emsQueue = "";
        String emsUser = "";
        String emsPwd = null;
        String sftpServerIP = "";
        int sftpServerPort = 0;
        String sftpServerUser = "";
        String sftpServerKey = "";
        String ftpServerIP = "";
        int ftpServerPort = 0;
        String ftpServerUser = "";
        String ftpServerPwd = "";
        boolean checkMsg = true;
        boolean isEncrypted = false;
        String algorithm = "";
        String encryptionKey = "";
        String encryptionIV = "";
        String msgFilter = "";
        boolean saveDBLog = true;

        javax.jms.Connection connection = null;
        Session session = null;
        MessageConsumer msgConsumer = null;
        Destination destination = null;

        try {
            serverConfigID = c.getConfigID();
            serverConfigDesc = c.getConfigDesc();
            emsServerUrl = c.getServerUrl();
            emsQueue = c.getQueue();
            emsUser = c.getUser();
            emsPwd = c.getPwd();
            sftpServerIP = c.getSFTPServerIP();
            sftpServerPort = c.getSFTPServerPort();
            sftpServerUser = c.getSFTPServerUser();
            sftpServerKey = c.getSFTPServerKey();
            ftpServerIP = c.getFTPServerIP();
            ftpServerPort = c.getFTPServerPort();
            ftpServerUser = c.getFTPServerUser();
            ftpServerPwd = c.getFTPServerPwd();
            checkMsg = c.getCheckMsg();
            isEncrypted = c.getIsEncrypted();
            algorithm = c.getAlgorithm();
            encryptionKey = c.getEncryptionKey();
            encryptionIV = c.getEncryptionIV();
            msgFilter = c.getMsgFilter();
            saveDBLog = c.getSaveDBLog();

            ConnectionFactory factory = new com.tibco.tibjms.TibjmsConnectionFactory(emsServerUrl);

            /* create the connection */
            connection = factory.createConnection(emsUser, emsPwd);

            /* create the session */
            session = connection.createSession(Session.CLIENT_ACKNOWLEDGE);

            /* set the exception listener */
            connection.setExceptionListener(this);

            /* create the destination */
            destination = session.createQueue(emsQueue);

            /* create the consumer */
            if (msgFilter.equalsIgnoreCase(""))
                msgConsumer = session.createConsumer(destination);
            else
                msgConsumer = session.createConsumer(destination, msgFilter);

            /* start the connection */
            connection.start();

        } catch (Exception e) {
            continueFlag = false;
            Logger.addNewListenerLog(null,
                    "Got error as launching JMS listener to listen the queue '" + emsQueue + "'! Error message: "
                            + e.toString() + ". Will retry after " + Constants.retry_interval + " seconds.",
                    Constants.log_type_error_string, null);
        }

        // if got any error as launch JMS listener, it will wait a while, then
        // retry
        if (continueFlag == false) {
            try {
                TimeUnit.SECONDS.sleep(Constants.retry_interval);

                // get the latest configuration
                ServerConfig c2 = Common.GetServerConfig(serverConfigID);
                if (c2 != null)
                    c = c2;
            } catch (Exception e) {
                Logger.addNewListenerLog(null, "Error message: " + e.toString(), Constants.log_type_warning_string,
                        null);
            }

            // re-launch the listener
            lanchEMSListener(timer, c);
        } else {
            DbaDbDao dba_db = new DbaDbDao();
            String defaultBusinessDate = "";
            try {
                defaultBusinessDate = Common.getBusinessDate(null);

                Logger.addNewListenerLog(dba_db,
                        "Connected to the EMS server '" + emsServerUrl + "', start listening JMS messages from '"
                                + emsQueue + "' for server configuration '" + serverConfigDesc + "'.",
                        Constants.log_type_normal_string, null);
            } catch (Exception e) {
                Logger.addNewListenerLog(null, "Error message: " + e.toString(), Constants.log_type_warning_string,
                        null);
            }

            /* read messages */
            while (continueFlag) {
                try {
                    msg = msgConsumer.receive();

                    if (msg != null) {
                        boolean execResult = false;
                        String errMsg = "";
                        String msg_id = "";
                        int interface_group_id;
                        String interface_group_desc;
                        String sync_method;
                        String sync_operation;
                        String external_uri;
                        String local_wrk_uri;
                        int success_code;
                        int retry_interval_in_minute;
                        int no_of_retry;
                        int unzip_flag;

                        // parse the message
                        String SourceSID = "";
                        String TargetSID = "";
                        String ContentID = "";
                        String SourceContentType = "";
                        String TargetChannel = "";

                        if (msg.propertyExists("SourceSID"))
                            SourceSID = msg.getStringProperty("SourceSID");
                        if (msg.propertyExists("TargetSID"))
                            TargetSID = msg.getStringProperty("TargetSID");
                        if (msg.propertyExists("ContentID"))
                            ContentID = msg.getStringProperty("ContentID");
                        if (msg.propertyExists("SourceContentType"))
                            SourceContentType = msg.getStringProperty("SourceContentType");
                        if (msg.propertyExists("TargetChannel"))
                            TargetChannel = msg.getStringProperty("TargetChannel");

                        if (SourceSID == null)
                            SourceSID = "";
                        if (TargetSID == null)
                            TargetSID = "";
                        if (ContentID == null)
                            ContentID = "";
                        if (SourceContentType == null)
                            SourceContentType = "";
                        if (TargetChannel == null)
                            TargetChannel = "";

                        int msgLen = msg.toString().length();
                        String log = "";
                        if (msgLen > Constants.max_msg_length)
                            log = "Receive message from '" + emsQueue + "': "
                                    + msg.toString().substring(1, Constants.max_msg_length - 10) + "..."
                                    + msg.toString().substring(msgLen - 10);
                        else
                            log = "Receive message from '" + emsQueue + "': " + msg.toString();

                        if (saveDBLog)
                            Logger.addNewListenerLog(dba_db, log, Constants.log_type_normal_string, null);
                        else
                            Logger.addNewLog(log, Constants.log_type_normal_string);

                        String BusinessDate;
                        if (ContentID == null || ContentID == "")
                            BusinessDate = defaultBusinessDate;
                        else
                            BusinessDate = Common.getBusinessDate(ContentID);

                        String OriginalBody = "";
                        String DecryptedBody = "";

                        if (isEncrypted) {
                            OriginalBody = msg.getBody(String.class);
                            if (algorithm.equalsIgnoreCase(Constants.algorithm_aes128))
                                DecryptedBody = Cryptography.AESDecrypt(OriginalBody, encryptionKey, encryptionIV);
                        }

                        // save the message to database directly
                        msg_id = dba_db.saveMessage(serverConfigID, SourceSID, TargetSID, ContentID, SourceContentType,
                                TargetChannel, BusinessDate, msg.toString(), OriginalBody, DecryptedBody);

                        /*
                         * // save the message to local file execSuccess =
                         * Common.SaveMessageToLocal(localMessagePath,
                         * serverConfigID, df.format(new Date()).toString(),
                         * SourceSID, TargetSID, ContentID, SourceContentType,
                         * TargetChannel, BusinessDate, msg.toString(),
                         * OriginalBody, DecryptedBody);
                         */
                        // sam
                        // TimeUnit.SECONDS.sleep(5);
                        // startTime = LocalDateTime.now();
                        // save the message to EDW database
                        // msg_id = dba_db.saveMessage(serverConfigID,
                        // SourceSID,
                        // TargetSID, ContentID, SourceContentType,
                        // TargetChannel, BusinessDate, msg.toString(),
                        // OriginalBody, DecryptedBody);

                        // endTime = LocalDateTime.now();
                        // duration = (int) ChronoUnit.MILLIS.between(startTime,
                        // endTime);

                        if (msg_id.isEmpty()) {
                            errMsg = "Save the message to EDW database failed!";
                            Logger.addNewListenerLog(dba_db, errMsg, Constants.log_type_error_string, null);
                        } else {
                            /*
                             * Logger.addNewListenerLog(dba_db,
                             * "Save the message " + duration,
                             * Constants.log_type_normal_string, null);
                             */
                            // not save whole message if the length is over 800

                            if (checkMsg) {
                                // check jms_interface table, to find if there
                                // is
                                // corresponding
                                // interface record
                                execResult = dba_db.chkInterface(msg_id);

                                if (execResult == false) {
                                    errMsg = "Couldn't find corresponding interface in jms_interface table!";
                                    Logger.addNewInterfaceLog(dba_db, errMsg, Constants.log_type_error_string, msg_id,
                                            0);
                                } else {
                                    // get the check_msg flag, if it's false,
                                    // then
                                    // do
                                    // NOT continue the following checks
                                    execResult = dba_db.getCheckMsg(msg_id, Constants.check_msg_flag);
                                    if (execResult) {
                                        boolean checkDuplicatedMsg = dba_db.getCheckMsg(msg_id,
                                                Constants.check_duplicated_msg_flag);
                                        boolean unzip = dba_db.getCheckMsg(msg_id, Constants.check_unzip_flag);

                                        if (checkDuplicatedMsg)
                                            // check if there are duplicated
                                            // messages
                                            execResult = dba_db.chkDuplicatedMessages(msg_id);

                                        if (checkDuplicatedMsg && execResult) {
                                            msg.acknowledge();
                                            String interface_name_pattern = dba_db.getInterfaceNamePattern(msg_id);
                                            errMsg = "Found duplicated messages for interface " + interface_name_pattern
                                                    + " for business date (" + BusinessDate + ")";
                                            Logger.addNewListenerLog(dba_db, errMsg, Constants.log_type_warning_string,
                                                    null);
                                        } else {

                                            // get interface information from
                                            // msg_id
                                            ArrayList<Interface> interfaces = dba_db.getInterface(msg_id);
                                            if (interfaces.size() != 1) {
                                                errMsg = "Get interface failed!";
                                                Logger.addNewInterfaceLog(dba_db, errMsg,
                                                        Constants.log_type_error_string, msg_id, 0);
                                            } else {
                                                Interface i = interfaces.get(0);
                                                interface_group_id = i.getInterfaceGroupID();
                                                interface_group_desc = i.getInterfaceGroupDesc();
                                                sync_method = i.getSyncMethod().trim();
                                                sync_operation = i.getSyncOperation().trim();
                                                external_uri = ContentID;
                                                local_wrk_uri = i.getLocalWrkURI().trim();
                                                success_code = i.getSuccessCode();
                                                retry_interval_in_minute = i.getRetryInterval();
                                                no_of_retry = i.getNoOfRetry();
                                                unzip_flag = i.getUnzip();

                                                /*
                                                 * if interface_group_id is
                                                 * equal to 0, which means no
                                                 * need to wait other interface
                                                 * messages, could start
                                                 * FTP/SFTP download immediately
                                                 */
                                                if (interface_group_id == 0) {
                                                    Logger.addNewListenerLog(dba_db,
                                                            "Start to process the " + sync_operation + " task.",
                                                            Constants.log_type_normal_string, msg_id);

                                                    DownloadTask t = new DownloadTask(msg_id, sftpServerIP,
                                                            sftpServerPort, sftpServerUser, sftpServerKey, ftpServerIP,
                                                            ftpServerPort, ftpServerUser, ftpServerPwd,
                                                            interface_group_id, ContentID, SourceSID, TargetSID,
                                                            sync_method, sync_operation, external_uri, local_wrk_uri,
                                                            success_code, retry_interval_in_minute, no_of_retry,
                                                            unzip_flag, BusinessDate);
                                                    Common.processDownloadTask(t);
                                                    Logger.addNewListenerLog(dba_db,
                                                            "Run done the " + sync_operation + " task successfully.",
                                                            Constants.log_type_normal_string, msg_id);
                                                }
                                                /*
                                                 * otherwise, need to wait for
                                                 * received all interfaces
                                                 */

                                                else {
                                                    // check if received all
                                                    // messages
                                                    execResult = dba_db.chkReceivedAllMessages(interface_group_id,
                                                            BusinessDate);
                                                    if (execResult == false) {
                                                        Logger.addNewListenerLog(dba_db,
                                                                "Not received all messages for interface group '"
                                                                        + interface_group_desc + "' for business date '"
                                                                        + BusinessDate + "'!",
                                                                Constants.log_type_warning_string, msg_id);
                                                    } else {
                                                        // if received all
                                                        // messages,
                                                        // then start
                                                        // to process
                                                        // them
                                                        // one by one
                                                        // get the download/put
                                                        // task
                                                        ArrayList<DownloadTask> tasks = dba_db.getInterfaceGroupTask(
                                                                envID, interface_group_id, BusinessDate, 0);
                                                        Logger.addNewListenerLog(dba_db,
                                                                "Start to process the tasks belonging to the interface group '"
                                                                        + interface_group_desc + "' (" + tasks.size()
                                                                        + " tasks)",
                                                                Constants.log_type_normal_string, msg_id);
                                                        // start to process the
                                                        // tasks
                                                        // one by one
                                                        String business_date = "";
                                                        boolean finalResult = true;
                                                        List<String> l = new ArrayList<String>();
                                                        for (DownloadTask t : tasks) {
                                                            business_date = t.getBusinessDate();
                                                            l.add(Common.getFileName(t.getContentID()));
                                                            execResult = Common.processDownloadTask(t);
                                                            if (execResult == false) {
                                                                finalResult = false;
                                                                break;
                                                            }
                                                        }

                                                        if (finalResult) {
                                                            String local_path = "";
                                                            String local_path_in = "";
                                                            String local_path_out = "";
                                                            String local_path_archive = "";

                                                            if (unzip) {
                                                                if (unzip) {
                                                                    finalResult = false;
                                                                    local_path = dba_db.getCDXWrkFolder();
                                                                    local_path_in = local_path + "in2\\";
                                                                    local_path_out = local_path + "out2\\";
                                                                    local_path_archive = local_path + "archive\\";
                                                                    String sourceFiles[] = new String[l.size()];
                                                                    sourceFiles = l.toArray(sourceFiles);
                                                                    int no_of_unzip_files = 0;
                                                                    // start to
                                                                    // unzip
                                                                    no_of_unzip_files = Zip.unzip(local_path_in,
                                                                            sourceFiles, local_path_out, 1, 1);
                                                                    if (no_of_unzip_files > 0)
                                                                        finalResult = true;
                                                                }
                                                            }

                                                            if (finalResult) {
                                                                dba_db.updMsgStatus(business_date,
                                                                        Constants.CDX_cap_hash_id_report_interface_group_id);
                                                                /*
                                                                 * move those
                                                                 * processed
                                                                 * files to
                                                                 * archive
                                                                 * folder
                                                                 */
                                                                if (unzip)
                                                                    Common.MoveFiles(local_path_in, local_path_archive);
                                                                Logger.addNewListenerLog(dba_db,
                                                                        "Run done the " + tasks.size()
                                                                                + " tasks belonging to the interface group '"
                                                                                + interface_group_desc
                                                                                + "' successfully.",
                                                                        Constants.log_type_normal_string, msg_id);
                                                            } else {
                                                                if (unzip)
                                                                    Logger.addNewListenerLog(dba_db,
                                                                            "Run done the " + tasks.size()
                                                                                    + " tasks belonging to the interface group '"
                                                                                    + interface_group_desc
                                                                                    + "' failed! unzip failed!",
                                                                            Constants.log_type_warning_string, msg_id);
                                                                else
                                                                    Logger.addNewListenerLog(dba_db,
                                                                            "Some tasks belonging to the interface group '"
                                                                                    + interface_group_desc + "' ("
                                                                                    + tasks.size() + " tasks) failed!",
                                                                            Constants.log_type_warning_string, msg_id);
                                                            }
                                                        } else
                                                            Logger.addNewListenerLog(dba_db,
                                                                    "Some tasks belonging to the interface group '"
                                                                            + interface_group_desc + "' ("
                                                                            + tasks.size() + " tasks) failed!",
                                                                    Constants.log_type_warning_string, msg_id);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            msg.acknowledge();
                        }
                    }
                } catch (InterruptedException ie) {
                    // do nothing
                } catch (Exception je) {
                    // if other exception, wait a while, then retry
                    if (je.toString()
                            .equalsIgnoreCase("javax.jms.JMSException: Thread has been interrupted") == false) {
                        Logger.addNewListenerLog(dba_db, "Error message: " + je.toString(),
                                Constants.log_type_error_string, null);
                        TimeUnit.SECONDS.sleep(Constants.retry_interval);
                    }

                    try {
                        // release the source
                        if (msgConsumer != null) {
                            msgConsumer.close();
                            msgConsumer = null;
                        }
                        if (session != null) {
                            session.close();
                            session = null;
                        }
                        if (connection != null) {
                            connection.close();
                            connection = null;
                        }
                        dba_db.finalize();
                        dba_db = null;

                        // get the latest configuration
                        ServerConfig c2 = Common.GetServerConfig(serverConfigID);
                        if (c2 != null) {
                            c = c2;
                            c2 = null;
                        }

                    } catch (Exception e) {
                        Logger.addNewListenerLog(null, "Error message: " + e.toString(),
                                Constants.log_type_warning_string, null);
                    }

                    // re-launch the listener
                    lanchEMSListener(timer, c);
                }
            }

            timer.cancel();
            timer.purge();

            Logger.addNewListenerLog(dba_db, "Stop listening JMS messages from '" + emsQueue
                            + "' for server configuration '" + serverConfigDesc + "' successfully.",
                    Constants.log_type_normal_string, null);
        }
    }

    public boolean LaunchNewListener(String timerName, ServerConfig c, int delayInSec) {
        boolean returnVal = false;

        Timer listenerTimer = new Timer(timerName, false);
        listenerTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    if (c.getIsSolace() == false)
                        lanchEMSListener(listenerTimer, c);
                    else
                        lanchSolaceListener(listenerTimer, c);
                } catch (Throwable e) {
                    System.err.println(e.toString());
                }
            }
        }, delayInSec * 1000);

        returnVal = true;

        return returnVal;
    }

    // add a warning log as closing the listener program
    static class CloseListener extends Thread {
        public void run() {
            try {
                Logger.addNewListenerLog(null, "EDW JMS listener will be closed if terminate the batch job.",
                        Constants.log_type_warning_string, null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /*-----------------------------------------------------------------------
     * main
     *----------------------------------------------------------------------*/
    public static void main(String[] args) throws Throwable {
        new EDWScheduleTask();
        Runtime listener = Runtime.getRuntime();
        // register CloseListener as shutdown hook
        listener.addShutdownHook(new CloseListener());
        new MsgListener(args);
    }

}
