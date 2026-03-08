#!/bin/bash
# .github/scripts/fetch-fonts.sh

if [ -z "$GOOGLE_FONTS_API_KEY" ]; then
  echo "Error: GOOGLE_FONTS_API_KEY is not set."
  exit 1
fi

echo "Fetching latest Google Fonts list..."

# API URL to get popular fonts (limited to 100 for performance)
URL="https://www.googleapis.com/webfonts/v1/webfonts?key=${GOOGLE_FONTS_API_KEY}&sort=popularity"

# Fetch and store as a minimal JSON
# We keep essential info (family + regular ttf) for all fonts (usually ~1500+)
curl -s "$URL" | jq '{
  last_update: (now | strftime("%Y-%m-%dT%H:%M:%SZ")),
  total_fonts: .items | length,
  fonts: [.items[] | {
    family: .family,
    url: .files.regular
  }]
}' > ext-additional-fonts/google-fonts-cache.json

echo "Font list updated successfully with $(jq '.fonts | length' ext-additional-fonts/google-fonts-cache.json) fonts."
