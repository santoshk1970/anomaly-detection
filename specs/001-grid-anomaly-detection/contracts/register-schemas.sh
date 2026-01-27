#!/bin/bash

SCHEMA_REGISTRY_URL="http://schema-registry:8080/apis/registry/v2/groups"

# Wait for Schema Registry to be available
echo "Waiting for Schema Registry..."
until $(curl --output /dev/null --silent --head --fail $SCHEMA_REGISTRY_URL); do
    printf '.'
    sleep 5
done

echo -e "\nSchema Registry is up!"

# Register SensorReading schema
curl -X POST -H "Content-Type: application/json; artifactType=AVRO" -H "X-Registry-ArtifactId: com.grid.events.SensorReading" --data @specs/001-grid-anomaly-detection/contracts/grid-events.avsc $SCHEMA_REGISTRY_URL/default/artifacts

# Register AnomalyEvent schema
curl -X POST -H "Content-Type: application/json; artifactType=AVRO" -H "X-Registry-ArtifactId: com.grid.events.AnomalyEvent" --data @specs/001-grid-anomaly-detection/contracts/grid-events.avsc $SCHEMA_REGISTRY_URL/default/artifacts

echo "\nSchemas registered."
