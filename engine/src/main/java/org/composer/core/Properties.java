package org.composer.core;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 9, 2008
 * Time: 7:44:18 AM
 * To change this template use File | Settings | File Templates.
 */
public enum Properties {
        PUBLISH("publish") {
            String value() {
                return "http://publish.composerlab.org/";
            }
        },
        SUBSCRIBE("subscribe") {
            String value() {
                return "http://subscribe.composerlab.org/";
            }
        },
        IMPORT("import") {
            String value() {
                return "http://import.composerlab.org/";
            }
        },
        EXPORT("export") {
            String value() {
                return "http://export.composerlab.org/";
            }
        },
        TRANSFORM("transform") {
            String value() {
                return "http://transform.composerlab.org/";
            }
        },
        SCHEMA("schema") {
            String value() {
                return "http://schema.composerlab.org/elements/1.0/";
            }
        },
        RELATIONSHIP_URI("relationship") {
            String value() {
                return "http://purl.org/vocab/relationship/";
            }
        },
        SITE("site") {
            String value() {
                return "http://composerlab.org/";
            }
        },
        DEFAULT_DOMAIN_PREFIX("Default Domain Prefix") {
            String value() {
                return "domain";
            }
        },
        DEFAULT_CONTEXT_PREFIX("Default Context Prefix") {
            String value() {
                return "context";
            }
        },
        DEFAULT_DOMAIN_URI("Default Domain URI") {
            String value() {
                return "http://composerlab.org/domain";
            }
        },
        DEFAULT_CONTEXT_URI("Default Context URI") {
            String value() {
                return "http://composerlab.org/context";
            }
        },
        DEFAULT_MODEL_NAME("Default Jena Model Name") {
            String value() {
                return "prototype";
            }
        },
        DEFAULT_INTERNAL_DBNAME("Default Internal DB Name") {
            String value() {
                return "composer.db";
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
