package org.composer.beans;

import org.composer.interfaces.Entity;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 22, 2008
 * Time: 6:24:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class RDFProduct implements Entity {
	private String name;
	private Collection<Entity> properties;

	public String name() {
		return name;
	}

	public Collection<Entity> properties() {
		return properties;
	}

	public void setProperties(Collection<Entity> _properties) {
		this.properties = _properties;
	}
}
