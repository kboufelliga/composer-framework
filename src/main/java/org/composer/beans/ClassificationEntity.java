package org.composer.beans;

import org.composer.interfaces.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 23, 2008
 * Time: 9:01:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationEntity implements Entity {
    public enum type {CLASSIFICATION};
    
    private String name = null;

    public ClassificationEntity(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

}
