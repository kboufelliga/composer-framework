package org.composer.beans;

import org.composer.interfaces.Entity;

public class RDFBean implements Entity {
    private String name = null;

    public RDFBean(String name) {
        this.name = name;
    }

    public String name() {
		return name;
	}
}
