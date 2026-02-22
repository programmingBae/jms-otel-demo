package com.example;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import javax.jms.*;

public class JmsPublisher {

    public static void main(String[] args) throws Exception {
        String host = getenv("SOLACE_HOST", "tcp://solace-broker:55555");
        String vpn  = getenv("SOLACE_VPN", "default");
        String user = getenv("SOLACE_USERNAME", "default");
        String pass = getenv("SOLACE_PASSWORD", "default");

        String topicName = getenv("TOPIC_NAME", "demo/otel/jms");
        int count = Integer.parseInt(getenv("MSG_COUNT", "10"));

        SolConnectionFactory cf = SolJmsUtility.createConnectionFactory();
        cf.setHost(host);
        cf.setVPN(vpn);
        cf.setUsername(user);
        cf.setPassword(pass);

        Connection conn = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            conn = cf.createConnection();
            conn.start();

            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(topicName);
            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            for (int i = 1; i <= count; i++) {
                TextMessage msg = session.createTextMessage("hello #" + i);
                msg.setStringProperty("app", "jms-publisher");
                producer.send(msg);
                System.out.println("Published topic=" + topicName + " payload=" + msg.getText());
                Thread.sleep(300);
            }
        } finally {
            // close order: producer -> session -> connection
            safeClose(producer);
            safeClose(session);
            safeClose(conn);
        }
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static void safeClose(MessageProducer p) {
        if (p != null) {
            try { p.close(); } catch (Exception ignored) {}
        }
    }

    private static void safeClose(Session s) {
        if (s != null) {
            try { s.close(); } catch (Exception ignored) {}
        }
    }

    private static void safeClose(Connection c) {
        if (c != null) {
            try { c.close(); } catch (Exception ignored) {}
        }
    }
}