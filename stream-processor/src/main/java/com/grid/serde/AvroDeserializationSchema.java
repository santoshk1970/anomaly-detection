package com.grid.serde;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Collections;

public class AvroDeserializationSchema<T> implements DeserializationSchema<T> {

    private final Class<T> recordType;
    private transient Deserializer<T> deserializer;
    private final String schemaRegistryUrl;

    public AvroDeserializationSchema(Class<T> recordType, String schemaRegistryUrl) {
        this.recordType = recordType;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public void open(InitializationContext context) {
        deserializer = new AvroKafkaDeserializer<>();
        deserializer.configure(Collections.singletonMap("apicurio.registry.url", schemaRegistryUrl), false);
    }

    @Override
    public T deserialize(byte[] message) throws IOException {
        return deserializer.deserialize(null, message);
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeInformation.of(recordType);
    }
}
