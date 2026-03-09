#!/bin/bash
# .github/scripts/fetch-fonts.sh

if [ -z "$GOOGLE_FONTS_API_KEY" ]; then
  echo "GOOGLE_FONTS_API_KEY is not set in environment."
  read -p "Please enter your Google Fonts API Key: " GOOGLE_FONTS_API_KEY
  
  if [ -z "$GOOGLE_FONTS_API_KEY" ]; then
    echo "Error: API Key is required."
    exit 1
  fi

  read -p "Save key in .bashrc/.zshrc? (y/n): " save_key
  if [[ $save_key =~ ^[Yy]$ ]]; then
    SHELL_CONFIG="$HOME/.bashrc"
    [[ "$SHELL" == *"zsh"* ]] && SHELL_CONFIG="$HOME/.zshrc"
    echo "export GOOGLE_FONTS_API_KEY=\"$GOOGLE_FONTS_API_KEY\"" >> "$SHELL_CONFIG"
    echo "Key saved to $SHELL_CONFIG. Run: source $SHELL_CONFIG"
  fi
fi

echo "Fetching latest Google Fonts list..."

# Get the root directory of the repo (one level up from .github/scripts)
# Use realpath if available, otherwise fallback to basic path resolution
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUTPUT_FILE="$REPO_ROOT/ext-additional-fonts/google-fonts-cache.json"

echo "Repo root: $REPO_ROOT"
echo "Output file: $OUTPUT_FILE"

# Ensure the output directory exists
mkdir -p "$(dirname "$OUTPUT_FILE")"

# API URL to get popular fonts
URL="https://www.googleapis.com/webfonts/v1/webfonts?key=${GOOGLE_FONTS_API_KEY}&sort=popularity"

echo "Requesting data from Google Fonts API..."
echo "API URL: $URL"

# Fetch and store as a minimal JSON
RESPONSE=$(curl -s "$URL")

# Debug: check if response is empty or error
if [ -z "$RESPONSE" ]; then
    echo "Error: Empty response from Google API."
    exit 1
fi

if echo "$RESPONSE" | jq -e '.items' > /dev/null; then
    # Create the JSON and store it in a variable first to check it
    # Fixed URL fallback to avoid "string has no keys" error when .files is accessed improperly
    GENERATED_JSON=$(echo "$RESPONSE" | jq '{
      last_update: (now | strftime("%Y-%m-%dT%H:%M:%SZ")),
      total_fonts: .items | length,
      fonts: [.items[] | {
        family: .family,
        category: .category,
        variants: .variants,
        subsets: .subsets,
        axes: (.axes // null),
        version: .version,
        last_modified: .lastModified,
        url: (.files.regular // .files["400"] // (.files | to_entries | .[0].value))
      }]
    }')

    echo "$GENERATED_JSON" > "$OUTPUT_FILE"
    
    # Wait local disk sync
    sleep 1

    # Check the count from the FILE
    if [ -f "$OUTPUT_FILE" ]; then
        COUNT=$(jq '.fonts | length' "$OUTPUT_FILE")
    else
        COUNT=0
    fi
    if [ "$COUNT" -eq 0 ]; then
        echo "Warning: Generated JSON has 0 fonts. Check API response."
        echo "$RESPONSE" | head -n 20
    else
        echo "Font list updated successfully with $COUNT fonts (including Variable Font axes)."
    fi
else
    echo "Error: Failed to fetch fonts. API Response contains no items."
    echo "$RESPONSE" | jq '.' | head -n 20
    exit 1
fi

# Synchro avec les assets de l'extension
ASSETS_DIR="$REPO_ROOT/ext-additional-fonts/src/main/assets"
mkdir -p "$ASSETS_DIR"
cp "$OUTPUT_FILE" "$ASSETS_DIR/google-fonts-cache.json"
echo "Font list synchronized to: $ASSETS_DIR/google-fonts-cache.json"

cp "$OUTPUT_FILE" "$REPO_ROOT/ext-additional-fonts/src/main/assets/google-fonts-cache.json"
echo "Font list copied to extension assets."
