import click
import time
import random
import uuid
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka import SerializingProducer


def load_avro_schema_from_file(schema_file):
    with open(schema_file, 'r') as f:
        return f.read()

@click.command()
@click.option('--bootstrap-servers', default='localhost:9092', help='Kafka bootstrap server(s).')
@click.option('--schema-registry-url', default='http://localhost:8080', help='Schema Registry URL.')
@click.option('--topic', default='sensor-readings', help='Kafka topic to produce to.')
@click.option('--event-rate', default=10, type=int, help='Number of events to generate per second.')
@click.option('--anomaly-rate', default=0.01, type=float, help='Fraction of events that should be anomalies (e.g., 0.01 for 1%).')
@click.option('--duration', default=60, type=int, help='Duration to run the generator in seconds.')
def main(bootstrap_servers, schema_registry_url, topic, event_rate, anomaly_rate, duration):
    """Generates simulated smart grid sensor data and sends it to Kafka."""

    schema_str = load_avro_schema_from_file('specs/001-grid-anomaly-detection/contracts/grid-events.avsc')
    schema_registry_client = SchemaRegistryClient({'url': schema_registry_url})

    avro_serializer = AvroSerializer(schema_registry_client, schema_str)

    producer_conf = {
        'bootstrap.servers': bootstrap_servers,
        'value.serializer': avro_serializer
    }

    producer = SerializingProducer(producer_conf)

    start_time = time.time()
    num_events = 0
    print(f"Starting data generation for {duration} seconds...")

    while time.time() - start_time < duration:
        loop_start_time = time.time()
        for _ in range(event_rate):
            meter_id = f"meter-{random.randint(1, 1000)}"
            current_timestamp = int(time.time() * 1000)

            is_anomaly = random.random() < anomaly_rate

            if is_anomaly:
                # Equipment failure anomaly
                voltage = 120.0 + random.uniform(-5, 5)
                current = 5.0 + random.uniform(-1, 1)
                temperature = 60.0 + random.uniform(-5, 5)
                status = random.choice(["FAILURE_CRITICAL", "ERROR_VOLTAGE_SPIKE"])
                print(f"Injecting anomaly: {meter_id}, status: {status}")
            else:
                # Normal reading
                voltage = 120.0 + random.uniform(-2, 2)
                current = 5.0 + random.uniform(-0.5, 0.5)
                temperature = 60.0 + random.uniform(-3, 3)
                status = None

            event = {
                'meter_id': meter_id,
                'timestamp': current_timestamp,
                'voltage': voltage,
                'current': current,
                'temperature': temperature,
                'status': status
            }

            try:
                producer.produce(topic=topic, key=str(uuid.uuid4()), value=event)
                num_events += 1
            except Exception as e:
                print(f"Failed to produce event: {e}")

        producer.poll(0)
        
        # Sleep to maintain the desired event rate
        loop_end_time = time.time()
        sleep_duration = 1.0 - (loop_end_time - loop_start_time)
        if sleep_duration > 0:
            time.sleep(sleep_duration)

    print(f"Flushing producer and exiting. Total events produced: {num_events}")
    producer.flush()

if __name__ == '__main__':
    main()
