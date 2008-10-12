package org.composer.interfaces;

import com.hp.hpl.jena.vocabulary.RDF;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: kboufelliga | Date Created: Oct 5, 2008
 */
public interface Transform {
    public RDF input(InputStream inputStream, String format);
    public OutputStream output(RDF document, String format);
}
