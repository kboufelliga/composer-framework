package org.composer.interfaces;

import com.hp.hpl.jena.vocabulary.RDF;

import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 5, 2008
 * Time: 10:30:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Export {
    public OutputStream write(RDF rdfDocument);
}
