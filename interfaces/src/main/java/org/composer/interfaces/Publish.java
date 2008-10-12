package org.composer.interfaces;

import java.net.URL;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 9, 2008
 * Time: 6:15:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Publish {
    public void to(Collection<URL> destinations);
}
