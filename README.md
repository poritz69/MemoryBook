# MemoryBook

Webanwendung mit Spring Boot, Thymeleaf und PostgreSQL. Man kann Bücher mit Status, Fortschritt und Bewertung
verwalten, Kategorien anlegen und Notizen zu Büchern schreiben. Jeder Benutzer sieht nur seine eigenen Daten.

## Starten

Voraussetzungen: Java 25 und PostgreSQL auf `localhost:5432` (Benutzer `postgres`).

```bash
./mvnw spring-boot:run
```

Danach `http://localhost:8085` öffnen und unter „Hier registrieren“ ein Konto anlegen.

Mit Testdaten (Benutzer `demo`, Passwort `demo1234`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

## Tests

```bash
./mvnw test
```

Die Tests benutzen eine H2-Datenbank im Speicher.
