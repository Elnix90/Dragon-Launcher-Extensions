#!/bin/bash

# Configuration des couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Récupération de l'extension
EXTENSION=$1
# Nettoyage de l'argument (enlève -- si présent)
EXTENSION=${EXTENSION#--}

if [ -z "$EXTENSION" ]; then
    echo -e "${BLUE}Extensions disponibles :${NC}"
    # Stocker les dossiers dans un tableau
    EXT_LIST=($(ls -d ext-*/ | cut -f1 -d'/'))
    
    # Afficher avec un index
    for i in "${!EXT_LIST[@]}"; do
        echo "  $((i+1))) ${EXT_LIST[$i]}"
    done
    echo "  A) Toutes les extensions (ALL)"

    echo -n "Sélectionnez le numéro de l'extension (ou 'A') : "
    read INDEX

    # Vérification si l'entrée est 'a' pour toutes
    if [ "$INDEX" == "A" ]; then
        EXTENSION="ALL"
    # Vérification si l'entrée est un nombre et dans la plage
    elif [[ "$INDEX" =~ ^[0-9]+$ ]] && [ "$INDEX" -ge 1 ] && [ "$INDEX" -le "${#EXT_LIST[@]}" ]; then
        EXTENSION="${EXT_LIST[$((INDEX-1))]}"
    else
        echo -e "${RED}Erreur : Sélection invalide.${NC}"
        exit 1
    fi
fi

# Préparation de la liste de travail
if [ "$EXTENSION" == "ALL" ]; then
    TO_PROCESS=($(ls -d ext-*/ | cut -f1 -d'/'))
else
    TO_PROCESS=("$EXTENSION")
fi

for CURRENT in "${TO_PROCESS[@]}"; do
    # Vérification de l'existence du dossier
    if [ ! -d "$CURRENT" ]; then
        echo -e "${RED}Erreur : L'extension '$CURRENT' n'existe pas.${NC}"
        continue
    fi

    echo -e "${BLUE}==> Compilation de $CURRENT...${NC}"
    ./gradlew ":$CURRENT:assembleDebug"

    if [ $? -ne 0 ]; then
        echo -e "${RED}Erreur : La compilation de $CURRENT a échoué.${NC}"
        continue
    fi

    # Recherche de l'APK généré
    APK_PATH=$(find "$CURRENT/build/outputs/apk/debug" -name "*.apk" | head -n 1)

    if [ -z "$APK_PATH" ]; then
        echo -e "${RED}Erreur : APK non trouvé pour $CURRENT.${NC}"
        continue
    fi

    echo -e "${BLUE}==> Installation de $CURRENT via ADB...${NC}"
    adb install -r "$APK_PATH"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}==> Succès ! L'extension $CURRENT a été installée.${NC}"
    else
        echo -e "${RED}Erreur : L'installation ADB de $CURRENT a échoué.${NC}"
    fi
done
