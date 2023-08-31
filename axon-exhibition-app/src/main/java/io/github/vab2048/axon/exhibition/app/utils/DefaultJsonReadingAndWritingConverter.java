package io.github.vab2048.axon.exhibition.app.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * The DefaultReadingAndWritingConverter provides a default implementation of the logic a
 * Spring Data JDBC @WritingConverter and @ReadingConverter can defer to when converting between
 * a postgres "jsonb" PGObject and a POJO.
 */
public class DefaultJsonReadingAndWritingConverter {
    private static final Logger log = LoggerFactory.getLogger(DefaultJsonReadingAndWritingConverter.class);
    private final ObjectMapper objectMapper;

    public DefaultJsonReadingAndWritingConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Convert the source object into a jsonb PGobject.
     * @param source        Object we want to persist as JSON.
     * @param errorMessage  Error message to be used when the conversion fails.
     * @return The jsonb PGobject representation of the POJO.
     */
    public PGobject defaultWrite(Object source, Supplier<String> errorMessage) {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            String initiation = objectMapper.writeValueAsString(source);
            jsonObject.setValue(initiation);
        } catch (SQLException | JsonProcessingException e) {
            var errorMsg = errorMessage.get();
            log.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
        return jsonObject;
    }

    /**
     * Convert the given PGobject into a POJO of type T.
     * @param pgObject       Postgres jsonb object we want to read in as a POJO.
     * @param cls            Class of the type of the POJO we want to read into.
     * @param errorMessage   Error message to use if the read fails.
     * @param <T>            Type of the POJO we want to read the PGobject in to.
     * @return               POJO of type T.
     */
    public <T> T defaultRead(PGobject pgObject, Class<T> cls, Supplier<String> errorMessage) {
        String initiation = pgObject.getValue();
        try {
            return objectMapper.readValue(initiation, cls);
        } catch (JsonProcessingException e) {
            var errorMsg = errorMessage.get();
            log.error(errorMsg, e);
            throw new IllegalStateException(errorMessage.get(), e);
        }
    }

}
