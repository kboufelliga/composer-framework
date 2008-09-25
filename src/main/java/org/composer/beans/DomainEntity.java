package org.composer.beans;

import org.composer.interfaces.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 23, 2008
 * Time: 9:13:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class DomainEntity implements Entity {
    public enum type {DOMAIN};

    private String name = null;

    public DomainEntity(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
