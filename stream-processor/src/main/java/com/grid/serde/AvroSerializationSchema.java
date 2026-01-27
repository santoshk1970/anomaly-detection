package com.grid.serde;

import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Collections;

public class AvroSerializationSchema<T> implements SerializationSchema<T> {

    private final Class<T> recordType;
    private transient Serializer<T> serializer;
    private final String schemaRegistryUrl;

    public AvroSerializationSchema(Class<T> recordType, String schemaRegistryUrl) {
        this.recordType = recordType;
        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    @Override
    public void open(InitializationContext context) {
        serializer = new AvroKafkaSerializer<>();
        serializer.configure(Collections.singletonMap("apicurio.registry.url", schemaRegistryUrl), false);
    }

    @Override
    public byte[] serialize(T element) {
        return serializer.serialize(null, element);
    }
}
