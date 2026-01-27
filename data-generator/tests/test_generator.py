import pytest
from src.generator import load_avro_schema_from_file
from fastavro import parse_schema

def test_sensor_reading_schema_loads_correctly():
    """Tests that the SensorReading Avro schema can be loaded and parsed."""
    try:
        schema_str = load_avro_schema_from_file('specs/001-grid-anomaly-detection/contracts/sensor-reading.avsc')
        parse_schema(eval(schema_str)) # eval is used because the schema is a string representation of a dict
    except Exception as e:
        pytest.fail(f"Failed to parse SensorReading schema: {e}")

def test_anomaly_event_schema_loads_correctly():
    """Tests that the AnomalyEvent Avro schema can be loaded and parsed."""
    # This schema depends on the SensorReading schema, so we need to provide it.
    sensor_schema_str = load_avro_schema_from_file('specs/001-grid-anomaly-detection/contracts/sensor-reading.avsc')
    anomaly_schema_str = load_avro_schema_from_file('specs/001-grid-anomaly-detection/contracts/anomaly-event.avsc')

    try:
        known_schemas = {
            "com.grid.events.SensorReading": eval(sensor_schema_str)
        }
        parse_schema(eval(anomaly_schema_str), known_schemas=known_schemas)
    except Exception as e:
        pytest.fail(f"Failed to parse AnomalyEvent schema: {e}")
