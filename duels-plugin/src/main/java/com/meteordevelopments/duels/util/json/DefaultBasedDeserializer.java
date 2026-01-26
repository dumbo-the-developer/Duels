package com.meteordevelopments.duels.util.json;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;

public abstract class DefaultBasedDeserializer<T> extends StdDeserializer<T> implements ResolvableDeserializer {

    protected final JsonDeserializer<?> defaultDeserializer;
    @Getter
    private final Class<T> target;

    public DefaultBasedDeserializer(final Class<T> target, final JsonDeserializer<?> defaultDeserializer) {
        super(target);
        this.target = target;
        this.defaultDeserializer = defaultDeserializer;
    }

    @Override
    public void resolve(final DeserializationContext context) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(context);
    }
}
