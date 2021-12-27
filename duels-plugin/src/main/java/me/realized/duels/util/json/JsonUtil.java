package me.realized.duels.util.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER;
    private static final ObjectWriter OBJECT_WRITER;

    static {
        final JsonFactory factory = new JsonFactory();
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        factory.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

        OBJECT_MAPPER = new ObjectMapper(factory);
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        OBJECT_WRITER = OBJECT_MAPPER.writer(buildDefaultPrettyPrinter());
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static ObjectWriter getObjectWriter() {
        return OBJECT_WRITER;
    }

    public static <T> void registerDeserializer(final Class<T> type, final Class<? extends DefaultBasedDeserializer<T>> deserializerClass) {
        final SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {

            @Override
            public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config, final BeanDescription description, final JsonDeserializer<?> deserializer) {
                if (description.getBeanClass().equals(type)) {
                    try {
                        return deserializerClass.getConstructor(JsonDeserializer.class).newInstance(deserializer);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                        ex.printStackTrace();
                        return deserializer;
                    }
                }

                return deserializer;
            }
        });
        OBJECT_MAPPER.registerModule(module);
    }

    private static PrettyPrinter buildDefaultPrettyPrinter() {
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter() {

            @Override
            public DefaultPrettyPrinter withSeparators(Separators separators) {
                _separators = separators;
                _objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
                return this;
            }

            @NotNull
            @Override
            public DefaultPrettyPrinter createInstance() {
                return new DefaultPrettyPrinter(this);
            }
        };

        printer.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        printer.indentObjectsWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        return printer;
    }

    private JsonUtil() {}
}
