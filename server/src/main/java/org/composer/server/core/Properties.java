package org.composer.server.core;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 9, 2008
 * Time: 7:44:18 AM
 * To change this template use File | Settings | File Templates.
 */
public enum Properties {
        PUBLISH_URL("publish") {
            String value() {
                return "http://publish.composerlab.org";
            }
        },
        PUBLISH_URI("publish") {
            String value() {
                return "/publishing";
            }
        },
        SUBSCRIBE_URL("subscribe") {
            String value() {
                return "http://subscribe.composerlab.org";
            }
        },
        IMPORT_URL("import") {
            String value() {
                return "http://import.composerlab.org";
            }
        },
        EXPORT_URL("export") {
            String value() {
                return "http://export.composerlab.org";
            }
        },
        TRANSFORM_URL("transform") {
            String value() {
                return "http://transform.composerlab.org";
            }
        },
        SCHEMA_URL("schema") {
            String value() {
                return "http://schema.composerlab.org";
            }
        },
        SCHEMA_PATH("schema") {
            String value() {
                return "/elements/0.1";
            }
        },
        PURL_RELATIONSHIP_URI("PURL Relationship URI") {
            String value() {
                return "http://purl.org/dc/elements/1.1/relationship";
            }
        },
        PURL_IDENTIFIER_URI("PURL Identifier URI") {
            String value() {
                return "http://purl.org/dc/elements/1.1/identifier";
            }
        },
        DEFAULT_URL("repository") {
            String value() {
                return "http://composerlab.org";
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
        DEFAULT_DOMAIN_PATH("Default Domain Path") {
            String value() {
                return "";
            }
        },
        DEFAULT_CONTEXT_PATH("Default Context Path") {
            String value() {
                return "";
            }
        },
        DEFAULT_DATABASE_TYPE("Default Database Type") {
            String value() {
                return "PostgreSQL";
            }
        },
        DEFAULT_MODEL_NAME("Default Jena Model Name") {
            String value() {
                return "prototype-0.1";
            }
        },
        DEFAULT_INTERNAL_DBNAME("Default Internal DB Name") {
            String value() {
                return "composer-0.6.db";
            }
        },
        TYPE_JENA_RESOURCE("JENA ResourceBuilder") {
            String value() {
                return "resource";
            }
        },
        TYPE_JENA_STATEMENT("JENA Statement") {
            String value() {
                return "statement";
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
