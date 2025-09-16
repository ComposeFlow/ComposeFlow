#!/bin/bash

# Change to the root directory of the project
cd "$(dirname "$0")/.." || exit 1

# Now we're in the root directory, so all paths are relative to the root
./gradlew :generate-jsonschema-cli:run

# Need to install `npm install typescript-json-schema -g` beforehand
typescript-json-schema generate-jsonschema-cli/composeflow.d.ts "*" > schema.json

typescript-json-schema generate-jsonschema-cli/airesponse.d.ts "*" > ai_response_schema.json

typescript-json-schema generate-jsonschema-cli/create_project_airesponse.d.ts "*" > create_project_ai_response_schema.json

# Now we can use the correct paths relative to the root directory
python3 scripts/trim_json_space.py schema.json minified_schema.json
python3 scripts/make_system_prompts.py

# Define directories
COMPOSEFLOW_API_DIR="${COMPOSEFLOW_API_DIR:-../composeflow-api-ts}"
PROMPTS_DIR="$COMPOSEFLOW_API_DIR/prompts"
EXAMPLE_YAML_DIR="$PROMPTS_DIR/example_yaml"

# Move Schema files to composeflow-api-ts if the directory exists
SCHEMA_FILES=(
    "minified_schema.json"
    "ai_response_schema.json"
    "create_project_ai_response_schema.json"
)
if [ -d "$PROMPTS_DIR" ]; then
    echo "Moving schema files to $PROMPTS_DIR..."

    for schema_file in "${SCHEMA_FILES[@]}"; do
        if [ -f "$schema_file" ]; then
            cp "$schema_file" "$PROMPTS_DIR/"
            echo "✓ Copied $schema_file"
        else
            echo "⚠ $schema_file not found"
        fi
    done
else
    echo "⚠ Target directory $PROMPTS_DIR does not exist, skipping schema files move"
fi

# Move YAML template files to composeflow-api-ts if the directory exists
YAML_TEMPLATES=(
    "core/model/src/commonMain/resources/login_screen_template.yaml"
    "core/model/src/commonMain/resources/messages_screen_template.yaml"
)
if [ -d "$EXAMPLE_YAML_DIR" ]; then
    echo "Moving YAML template files to $EXAMPLE_YAML_DIR..."

    for yaml_path in "${YAML_TEMPLATES[@]}"; do
        yaml_file=$(basename "$yaml_path")
        if [ -f "$yaml_path" ]; then
            cp "$yaml_path" "$EXAMPLE_YAML_DIR/"
            echo "✓ Copied $yaml_file"
        else
            echo "⚠ $yaml_file not found at $yaml_path"
        fi
    done
else
    echo "⚠ Target directory $EXAMPLE_YAML_DIR does not exist, skipping YAML file move"
fi
