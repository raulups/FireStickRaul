# FireStickRaul

Launcher personal para Fire TV en Kotlin + Compose for TV.

## Configuración

`local.properties` (no se sube al repo):

```
TMDB_API_KEY=tu_key
```

Región de TMDB en `data/tmdb/TmdbConfig.kt` (`ES` por defecto).

## Instalar

```
adb connect IP_DEL_FIRE:5555
./gradlew installRelease
```

## Abrir al pulsar Home

```
adb shell settings put secure enabled_accessibility_services com.raularribas.firelauncher/com.raularribas.firelauncher.service.HomeRedirectService
adb shell settings put secure accessibility_enabled 1
```

Para desactivarlo:

```
adb shell settings delete secure enabled_accessibility_services
```
