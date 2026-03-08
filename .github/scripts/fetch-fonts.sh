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

# API URL to get popular fonts
URL="https://www.googleapis.com/webfonts/v1/webfonts?key=${GOOGLE_FONTS_API_KEY}&sort=popularity"

# Get the root directory of the repo (one level up from .github/scripts)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUTPUT_FILE="$REPO_ROOT/ext-additional-fonts/google-fonts-cache.json"

# Ensure the output directory exists
mkdir -p "$(dirname "$OUTPUT_FILE")"

# Fetch and store as a minimal JSON
RESPONSE=$(curl -s "$URL")
if echo "$RESPONSE" | jq -e '.items' > /dev/null; then
    echo "$RESPONSE" | jq '{
      last_update: (now | strftime("%Y-%m-%dT%H:%M:%SZ")),
      total_fonts: .items | length,
      fonts: [.items[] | {
        family: .family,
        url: .files.regular
      }]
    }' > "$OUTPUT_FILE"
    echo "Font list updated successfully with $(jq '.fonts | length' "$OUTPUT_FILE") fonts."
else
    echo "Error: Failed to fetch fonts. API Response:"
    echo "$RESPONSE" | jq '.'
    exit 1
fi
