package org.composer.beans;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Sep 23, 2008
 * Time: 10:17:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class RDFStore {
    private DomainEntity domain;
    private ContextEntity context;
    private RDFBean bean;

    public DomainEntity getDomain() {
    return domain;
    }

    public void setDomain(DomainEntity domain) {
        this.domain = domain;
    }

    public ContextEntity getContext() {
        return context;
    }

    public void setContext(ContextEntity context) {
        this.context = context;
    }

    public RDFBean getBean() {
        return bean;
    }

    public void setBean(RDFBean bean) {
        this.bean = bean;
    }
}
