package org.composer.server.core;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 17, 2008
 * Time: 10:31:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Messenger {
    private final String hostName;
    private final int portNumber;
    private final String userName;
    private final String userPassword;
    private final String virtualHost;
    private Connection connection;
    private Channel channel;

    private Messenger(Builder builder) {
        hostName = builder.hostName;
        portNumber = builder.portNumber;
        userName = builder.userName;
        userPassword = builder.userPassword;
        virtualHost = builder.virtualHost;

        ConnectionParameters params = new ConnectionParameters();
        params.setUsername(userName);
        params.setPassword(userPassword);

        params.setVirtualHost(virtualHost);
        params.setRequestedHeartbeat(0);

        ConnectionFactory factory = new ConnectionFactory(params);
        try {
            Connection conn = factory.newConnection(hostName, portNumber);
            this.connection = conn;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static class Builder {
        private String hostName;
        private int portNumber;
        private String userName;
        private String userPassword;
        private String virtualHost;


        public Builder(String hostName, int portNumber, String userName, String userPassword) {
            this.hostName = hostName;
            this.portNumber = portNumber;
            this.userName = userName;
            this.userPassword = userPassword;
        }

        public Builder virtualHost(String value) {
            virtualHost = value;
            return this;
        }

        public Messenger build() {
            return new Messenger(this);
        }
    }

    public Channel createChannel() throws IOException {
        return connection.createChannel();
    }

    public void closeChannel(Channel channel) throws IOException {
        channel.close(AMQP.REPLY_SUCCESS, "Goodbye");
        connection.close(AMQP.REPLY_SUCCESS);
    }
}
