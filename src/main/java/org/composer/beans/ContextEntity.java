package org.composer.beans;

import org.composer.interfaces.Classification;
import org.composer.interfaces.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 23, 2008
 * Time: 9:00:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContextEntity implements Entity {
    public enum type {CONTEXT};
    private String name = null;

    public ContextEntity(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
    
}
