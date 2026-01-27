import click
import time
import random
import uuid
import json
import requests
from kafka import KafkaProducer
from datetime import datetime


def load_avro_schema_from_file(schema_file):
    with open(schema_file, 'r') as f:
        return json.load(f)


def serialize_avro_record(schema, record, record_name):
    """Simple JSON serialization for Avro-compatible records"""
    return json.dumps(record).encode('utf-8')


@click.command()
@click.option('--bootstrap-servers', default='localhost:9092', help='Kafka bootstrap server(s).')
@click.option('--schema-registry-url', default='http://localhost:8080', help='Schema Registry URL.')
@click.option('--topic', default='sensor-readings', help='Kafka topic to produce to.')
@click.option('--event-rate', default=10, type=int, help='Number of events to generate per second.')
@click.option('--anomaly-rate', default=0.01, type=float, help='Fraction of events that should be anomalies (e.g., 0.01 for 1%).')
@click.option('--duration', default=60, type=int, help='Duration to run the generator in seconds.')
def main(bootstrap_servers, schema_registry_url, topic, event_rate, anomaly_rate, duration):
    """Generates simulated smart grid sensor data and sends it to Kafka."""

    # Load schema
    schema_data = load_avro_schema_from_file('specs/001-grid-anomaly-detection/contracts/grid-events.avsc')
    
    # Find the SensorReading schema
    sensor_schema = None
    for schema in schema_data:
        if schema.get('name') == 'SensorReading':
            sensor_schema = schema
            break
    
    if not sensor_schema:
        print("Could not find SensorReading schema")
        return

    # Create Kafka producer
    producer = KafkaProducer(
        bootstrap_servers=bootstrap_servers,
        value_serializer=lambda v: v
    )

    # Generate data
    start_time = time.time()
    meter_ids = [f"meter-{i:04d}" for i in range(100)]
    
    print(f"Starting data generation for {duration} seconds at {event_rate} events/second")
    print(f"Anomaly rate: {anomaly_rate * 100:.1f}%")
    
    try:
        while time.time() - start_time < duration:
            # Generate sensor reading
            meter_id = random.choice(meter_ids)
            timestamp = int(datetime.now().timestamp() * 1000)
            
            # Normal values
            voltage = random.normalvariate(240, 5)  # 240V ± 5V
            current = random.normalvariate(10, 1)   # 10A ± 1A
            temperature = random.normalvariate(25, 2)  # 25°C ± 2°C
            status = None
            
            # Inject anomalies
            if random.random() < anomaly_rate:
                anomaly_type = random.choice(['voltage_spike', 'equipment_failure'])
                if anomaly_type == 'voltage_spike':
                    voltage = random.normalvariate(300, 10)  # High voltage
                else:
                    status = "FAILURE"
            
            # Create record
            record = {
                "meter_id": meter_id,
                "timestamp": timestamp,
                "voltage": voltage,
                "current": current,
                "temperature": temperature,
                "status": status
            }
            
            # Serialize and send
            serialized = serialize_avro_record(sensor_schema, record, "SensorReading")
            producer.send(topic, value=serialized)
            
            # Control rate
            time.sleep(1.0 / event_rate)
            
    except KeyboardInterrupt:
        print("\nStopping data generation...")
    finally:
        producer.flush()
        producer.close()
        print("Data generation completed")


if __name__ == '__main__':
    main()
