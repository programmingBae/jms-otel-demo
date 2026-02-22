package com.example;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.util.concurrent.CountDownLatch;

public class JmsConsumer {

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    public static void main(String[] args) throws Exception {
        String host = getenv("SOLACE_HOST", "tcp://host.docker.internal:55557");
        String vpn  = getenv("SOLACE_VPN", "default");
        String user = getenv("SOLACE_USERNAME", "default");
        String pass = getenv("SOLACE_PASSWORD", "default");

        String queueName = getenv("QUEUE_NAME", "Q.DEMO.OTEL.JMS");

        System.out.println("Consumer connecting:");
        System.out.println("  host=" + host);
        System.out.println("  vpn=" + vpn);
        System.out.println("  user=" + user);
        System.out.println("  queue=" + queueName);

        SolConnectionFactory cf = SolJmsUtility.createConnectionFactory();
        cf.setHost(host);
        cf.setVPN(vpn);
        cf.setUsername(user);
        cf.setPassword(pass);

        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        CountDownLatch keepAlive = new CountDownLatch(1);

        try {
            connection = cf.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue(queueName);
            consumer = session.createConsumer(queue);

            consumer.setMessageListener(message -> onMessage(message));

            connection.start();
            System.out.println("Consumer started. Waiting for messages...");

            // Keep process alive
            keepAlive.await();
        } finally {
            safeClose(consumer);
            safeClose(session);
            safeClose(connection);
        }
    }

    private static void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                System.out.println("Received: " + ((TextMessage) message).getText());
            } else {
                System.out.println("Received message type=" + message.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safeClose(MessageConsumer c) {
        if (c != null) {
            try { c.close(); } catch (Exception ignored) {}
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