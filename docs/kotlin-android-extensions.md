# Création d'extensions en Kotlin pour des applications Android

Ce guide explique comment créer des extensions Kotlin pour des applications Android au sein de cet écosystème.

## Pré-requis

- Android Studio ou IntelliJ IDEA.
- Kotlin 1.9+.
- Connaissances de base de l'architecture Android (Services, BroadcastReceivers, ou AIDL si applicable).

## Structure d'une extension

Chaque extension doit idéalement suivre une architecture modulaire.

1. **Manifest (AndroidManifest.xml)** : Déclaration des composants.
2. **Code (src/main/kotlin)** : Logique de l'extension.
3. **Resources (src/main/res)** : Assets et UI (si nécessaire).

### Communication via AIDL (Interface de définition d'interface Android)

Pour permettre à Dragon Launcher de communiquer avec votre extension :

1. Définissez un fichier `.aidl` pour l'interface de service.
2. Implémentez un `Service` Android qui renvoie l'interface au `onBind`.

```kotlin
class MyExtensionService : Service() {
    private val binder = object : IExtensionInterface.Stub() {
        override fun getExtensionName(): String = "Ma Super Extension"
        // Autres méthodes...
    }

    override fun onBind(intent: Intent): IBinder? = binder
}
```

### Injection de scripts (Optionnel)

Si l'application prend en charge le chargement dynamique (DEX/JAR) :
- Compilez votre code Kotlin en un fichier DEX.
- L'application hôte chargera ce fichier via `DexClassLoader`.

## Bonnes pratiques

- **Permission**: Utilisez des permissions personnalisées pour sécuriser l'accès à votre extension.
- **Performance**: Évitez les opérations lourdes sur le thread principal pour ne pas ralentir le lanceur.
- **Compatibilité**: Ciblez un SDK Android minimum cohérent avec l'application hôte.
