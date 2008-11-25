package org.composer.client;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 15, 2008
 * Time: 11:45:35 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Properties {
        REPOSITORY_URL("repository") {
            String value() {
                return "http://repository.composerlab.org";
            }
        },
        DEFAULT_DOMAIN_PREFIX("Default DomainPath Prefix") {
            String value() {
                return "domain";
            }
        },
        DEFAULT_CONTEXT_PREFIX("Default ContextPath Prefix") {
            String value() {
                return "context";
            }
        };

        private final String name;

        Properties(String name) {
            this.name = name;
        }

        @Override public String toString() {
            return name;
        }

        abstract String value();
}
