package io.github.vab2048.axon.exhibition.app.config;

import org.axonframework.serialization.CannotConvertBetweenTypesException;
import org.axonframework.serialization.ContentTypeConverter;
import org.postgresql.util.PGobject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Allows the Jackson serializer to convert between a byte[] and a JSONB PGObject.
 *
 * @author JohT
 */
public class PostgreSqlBytesToJsonbConverter implements ContentTypeConverter<byte[], PGobject> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<byte[]> expectedSourceType() {
        return byte[].class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<PGobject> targetType() {
        return PGobject.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PGobject convert(byte[] original) {
        String value = new String(original, StandardCharsets.UTF_8);

        PGobject object = new PGobject();
        object.setType("JSONB");
        try {
            object.setValue(value);
        } catch (SQLException e) {
            String message = "Unable to convert byte[] to PostgreSQL JSONB PGobject type: " + value;
            throw new CannotConvertBetweenTypesException(message, e);
        }
        return object;
    }
}