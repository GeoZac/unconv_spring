package com.unconv.spring.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Custom JSON deserializer for converting JSON representations of authorities into a collection of
 * GrantedAuthority objects.
 */
public class UnconvAuthorityDeserializer
        extends JsonDeserializer<Collection<? extends GrantedAuthority>> {

    /**
     * Deserializes JSON representation of authorities into a collection of GrantedAuthority
     * objects.
     *
     * @param jsonParser the JSON parser
     * @param deserializationContext the deserialization context
     * @return a collection of GrantedAuthority objects
     * @throws IOException if an I/O error occurs while reading the JSON content
     */
    @Override
    public Collection<? extends GrantedAuthority> deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        JsonNode jsonNode = mapper.readTree(jsonParser);

        Iterator<JsonNode> elements = jsonNode.elements();
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            JsonNode authority = next.get("authority");
            authorities.add(new SimpleGrantedAuthority(authority.asText()));
        }

        return authorities;
    }
}
