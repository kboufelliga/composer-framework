package org.composer.beans;

import org.composer.interfaces.Entity;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.db4o.config.annotations.Indexed;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 30, 2008
 * Time: 2:26:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFEntity {
    @Indexed
    private String name;
    private String uri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
