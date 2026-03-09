#!/bin/bash

# Configuration des couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Récupération de l'extension
EXTENSION=$1

if [ -z "$EXTENSION" ]; then
    echo -e "${BLUE}Extensions disponibles :${NC}"
    ls -d ext-*/ | cut -f1 -d'/'
    echo -n "Entrez le nom de l'extension à installer (ex: ext-weather-widget) : "
    read EXTENSION
fi

# Vérification de l'existence du dossier
if [ ! -d "$EXTENSION" ]; then
    echo -e "${RED}Erreur : L'extension '$EXTENSION' n'existe pas.${NC}"
    exit 1
fi

echo -e "${BLUE}==> Compilation de $EXTENSION...${NC}"
./gradlew ":$EXTENSION:assembleDebug"

if [ $? -ne 0 ]; then
    echo -e "${RED}Erreur : La compilation a échoué.${NC}"
    exit 1
fi

# Recherche de l'APK généré
APK_PATH=$(find "$EXTENSION/build/outputs/apk/debug" -name "*.apk" | head -n 1)

if [ -z "$APK_PATH" ]; then
    echo -e "${RED}Erreur : APK non trouvé.${NC}"
    exit 1
fi

echo -e "${BLUE}==> Installation via ADB...${NC}"
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}==> Succès ! L'extension $EXTENSION a été installée.${NC}"
else
    echo -e "${RED}Erreur : L'installation ADB a échoué.${NC}"
    exit 1
fi
