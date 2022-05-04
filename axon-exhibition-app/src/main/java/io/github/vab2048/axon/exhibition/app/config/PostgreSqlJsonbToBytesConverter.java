package io.github.vab2048.axon.exhibition.app.config;

import org.axonframework.serialization.ContentTypeConverter;
import org.postgresql.util.PGobject;

/**
 * Allows the Jackson serializer to convert between a PGobject to a byte[].
 * @author JohT
 */
public class PostgreSqlJsonbToBytesConverter implements ContentTypeConverter<PGobject, byte[]> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<PGobject> expectedSourceType() {
        return PGobject.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<byte[]> targetType() {
        return byte[].class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] convert(PGobject original) {
        if ((original == null) || (original.getValue() == null)) {
            return new byte[0];
        }
        return original.getValue().getBytes();
    }
}