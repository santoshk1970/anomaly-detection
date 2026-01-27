FROM python:3.11-slim

WORKDIR /app

# Install Poetry
RUN pip install --no-cache-dir poetry==1.7.1

# Copy dependency files
COPY pyproject.toml ./

# Install dependencies (without dev dependencies for production)
RUN poetry config virtualenvs.create false \
    && poetry install --no-interaction --no-ansi --no-dev

# Copy source code
COPY src/ ./src/

# Set Python path
ENV PYTHONPATH=/app

# Default command
CMD ["python", "src/generator.py", "--help"]
