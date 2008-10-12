package org.composer.communication;

import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ClientSession;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 11, 2008
 * Time: 4:04:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSONListener implements ChannelListener, Serializable {
    public void receivedMessage(Channel channel, ClientSession clientSession, ByteBuffer jsonObject) {
        System.out.println(" received json object: "+jsonObject.toString()+" from "+clientSession+" on channel "+channel);
    }
}
