# MemoryBook

Spring-Boot-Webanwendung zum Verwalten von Büchern, Kategorien und Notizen.

## Voraussetzungen

- Java 25 (mit Java 27 läuft Lombok noch nicht)
- PostgreSQL auf `localhost:5432`, Benutzer `postgres` (siehe `src/main/resources/application.properties`)

## Starten

```bash
./mvnw spring-boot:run
```

Danach im Browser `http://localhost:8085/users` öffnen, einen Benutzer registrieren und einloggen.
