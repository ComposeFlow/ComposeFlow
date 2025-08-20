#!/bin/bash

# Script to generate JSON schema files using KSP and move them to the composeflow-api-ts/dist/schemas directory

# Change to the root directory of the project
cd "$(dirname "$0")/.."

# Default location for composeflow-api-ts
DEFAULT_API_TS_DIR="../composeflow-api-ts"

# Parse command line arguments
API_TS_DIR=$DEFAULT_API_TS_DIR
if [ $# -gt 0 ]; then
  API_TS_DIR=$1
fi

# Target directory for schema files
TARGET_DIR="${API_TS_DIR}/schemas"

# Clear up the old JSON schema files
echo "Cleaning up old JSON schema files..."
find "${TARGET_DIR}" -name "*_mcp_tool.json" -delete
rm -fr feature/uibuilder/build/generated/llm-tools
rm -fr feature/appstate-editor/build/generated/llm-tools
rm -fr feature/datatype-editor/build/generated/llm-tools
rm -fr feature/string-editor/build/generated/llm-tools

# Print information
echo "Generating MCP schema files..."
echo "Target directory: ${TARGET_DIR}"

# Run KSP to generate JSON schema files
echo "Running KSP to generate JSON schema files..."
./gradlew \
    :feature:uibuilder:runKsp \
    :feature:appstate-editor:runKsp \
    :feature:datatype-editor:runKsp \
    :feature:string-editor:runKsp

# Check if KSP ran successfully
if [ $? -ne 0 ]; then
  echo "Error: Failed to run KSP. Exiting."
  exit 1
fi

# Create the target directory if it doesn't exist
echo "Creating target directory if it doesn't exist..."
mkdir -p "${TARGET_DIR}"

# Check if target directory was created successfully
if [ ! -d "${TARGET_DIR}" ]; then
  echo "Error: Failed to create target directory: ${TARGET_DIR}"
  exit 1
fi

# Find and copy only the *_mcp_tool.json files
echo "Copying MCP tool schema files to ${TARGET_DIR}..."
find feature/uibuilder/build/generated/llm-tools -name "*_mcp_tool.json" -exec cp {} "${TARGET_DIR}" \;
find feature/appstate-editor/build/generated/llm-tools -name "*_mcp_tool.json" -exec cp {} "${TARGET_DIR}" \;
find feature/datatype-editor/build/generated/llm-tools -name "*_mcp_tool.json" -exec cp {} "${TARGET_DIR}" \;
find feature/string-editor/build/generated/llm-tools -name "*_mcp_tool.json" -exec cp {} "${TARGET_DIR}" \;

# Count the number of files copied
NUM_FILES=$(find "${TARGET_DIR}" -name "*_mcp_tool.json" | wc -l)
echo "Successfully copied ${NUM_FILES} MCP tool schema files to ${TARGET_DIR}"

echo "Done!"
