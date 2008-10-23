package org.composer.interfaces;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

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
    public void publish(URL destination, Resource resource);
}
