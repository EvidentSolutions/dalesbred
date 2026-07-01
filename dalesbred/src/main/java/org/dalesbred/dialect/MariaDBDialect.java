package org.dalesbred.dialect;

import org.dalesbred.internal.utils.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Support for MariaDB.
 */
public class MariaDBDialect extends Dialect {

    private static final Logger log = LoggerFactory.getLogger(MariaDBDialect.class);

    private boolean blobsAsByteArrays = false;

    public void setBlobsAsByteArrays(boolean blobsAsByteArrays) {
        this.blobsAsByteArrays = blobsAsByteArrays;
    }

    public void autodetectSettings(DatabaseMetaData metadata) {
        try {
            Version version = Version.parse(metadata.getDriverVersion());
            blobsAsByteArrays = version.compareTo(Version.parse("3.5.2")) >= 0;
        } catch (SQLException | InstantiationException e) {
            // MariaDB Connector/J never explicitly throws a SQLException here but API declares it so we just have to eat it
            log.error("Could not parse driver version.", e);
        }
    }

    @Override
    public @Nullable Type overrideResultSetMetaDataType(@NotNull String className) {
        return switch (className) {
            case "byte[]" ->
                // MariaDB Connector/J 3.x encodes byte array types in a way that is incompatible with Class.forName
                byte[].class;
            case "java.sql.Blob" ->
                // MariaDB Connector/J 3.5.2+ reports java.sql.Blob as the class name for
                // binary blobs but returns them directly as byte arrays when fetched
                blobsAsByteArrays ? byte[].class : null;
            default -> null;
        };
    }
}
