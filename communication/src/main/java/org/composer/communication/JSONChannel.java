package org.composer.communication;

import com.sun.sgs.app.*;

import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 11, 2008
 * Time: 4:12:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSONChannel implements Serializable, AppListener {
    String[]  channelNames = new String[] {"composer", "cafepress::ecommerce"};
    Vector<Channel> channels = new Vector<Channel>();

    public void initialize(Properties init) {

        ChannelManager cm = AppContext.getChannelManager();
        channels.add(cm.createChannel(channelNames[0], new JSONListener(), Delivery.RELIABLE));
    }

    public ClientSessionListener loggedIn(ClientSession session)
    {
        return new JSONChannelClient(session, channels);
    }
}
