package org.composer.interfaces;

import com.hp.hpl.jena.vocabulary.RDF;

import java.io.File;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 5, 2008
 * Time: 10:23:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Import {
    public RDF read(InputStream input);
}
