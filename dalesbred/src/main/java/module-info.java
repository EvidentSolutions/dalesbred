module org.dalesbred {
    requires transitive java.sql;
    requires java.naming;
    requires java.xml;

    requires org.slf4j;

    requires static org.jetbrains.annotations;
    requires static kotlin.stdlib;
    requires static org.joda.time;
    requires static org.threeten.bp;
    requires static spring.context;
    requires static spring.jdbc;
    requires static org.postgresql.jdbc;
    requires static com.oracle.database.jdbc;

    exports org.dalesbred;
    exports org.dalesbred.annotation;
    exports org.dalesbred.connection;
    exports org.dalesbred.conversion;
    exports org.dalesbred.datatype;
    exports org.dalesbred.dialect;
    exports org.dalesbred.integration.joda;
    exports org.dalesbred.integration.kotlin;
    exports org.dalesbred.integration.spring;
    exports org.dalesbred.integration.threeten;
    exports org.dalesbred.query;
    exports org.dalesbred.result;
    exports org.dalesbred.transaction;
}
