package org.composer.communication;

import com.sun.sgs.app.*;
import com.sun.sgs.nio.channels.Channels;

import java.io.Serializable;
import java.util.Vector;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 11, 2008
 * Time: 4:18:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSONChannelClient implements Serializable, ClientSessionListener
{
    protected ClientSession session;
    protected Vector<Channel> subscriptions;

    public JSONChannelClient(ClientSession session,Vector<Channel> channels)
    {
         this.session = session;
         this.subscriptions = channels;
    }

    public Channel getChannel(String name) {
         try {
            return AppContext.getChannelManager().getChannel(name);
         } catch (NameNotBoundException ex) {
            return null;
         }
    }

    public void receivedMessage(byte[] message)
    {
        String msg = new String(message);
        if (msg.startsWith("join")) {
            // format is "join <channel-name>"
            String channelName = msg.substring(5);
            Channel c = getChannel(channelName);
            if (c != null) {
                // Join this user
                c.join(session);

                session.send(ByteBuffer.wrap("OK".getBytes()));
                subscriptions.add(c);
            }
            else {
                session.send(ByteBuffer.wrap("No such channel".getBytes()));
            }
        }
        else if (msg.startsWith("leave")) {
            // format is "leave <channel-name>"
            String channelName = msg.substring(6);
            Channel c = getChannel(channelName);
            if (c != null) {
                c.leave(session);

                session.send(ByteBuffer.wrap("OK".getBytes()));
                subscriptions.remove(c);
            }
            else {
                session.send(ByteBuffer.wrap("No such channel".getBytes()));
            }
        }
        else if (msg.startsWith("create")) {
            String channelName = msg.substring(7);
            try {
                ChannelManager cm = AppContext.getChannelManager();
                cm.createChannel(channelName, null, Delivery.RELIABLE);
                session.send(ByteBuffer.wrap("OK".getBytes()));
            } catch (NameExistsException ex) {
                session.send(ByteBuffer.wrap("Channel exists.".getBytes()));
            }
        }
        else {
            session.send(ByteBuffer.wrap("Unknown message".getBytes()));
        }
    }

    public void receivedMessage(ByteBuffer byteBuffer) {

    }

    public void disconnected(boolean graceful) {
        String disconnect;
        if (graceful)
            disconnect = "Graceful disconnect.";
        else
            disconnect = "Hard disconnect";
        System.out.println(session+" disconnected: "+disconnect);
    }
}
