# Bonus-App für ein Restaurant

> Projekt für: POS/DBMM Projekt 
> Stand: 23.09.2025 (Europe/Vienna)

## Überblick

Die **Bonus-App** ermöglicht Restaurantkund:innen, bei Einkäufen Punkte zu sammeln und gegen Rewards (Gratisprodukte, Rabatte) einzulösen. Ein:e Kund:in kann bei mehreren Restaurants teilnehmen; Restaurants können mehrere Filialen haben. Die Teilnahme wird über **LoyaltyAccounts** modelliert.

---

## Ziele & Hauptfunktionen

- Punkte sammeln anhand konfigurierbarer **PointRules** (z. B. „1 Punkt pro €1 ab €5 Mindestumsatz“).
- Punktejournal (**PointLedger**) als Quelle der Wahrheit: Earn, Redeem, Adjust, Expire.
- Rewards (mit Punkte-Kosten & Gültigkeit) definieren und **einlösen** (**Redemption**).
- Mehrfilial-fähige Käufe (**Purchase**) inkl. Währung/Brutto/Netto/VAT.
- **3NF-konformes** Schema, klare Trennung von Stammdaten, Regeln & Bewegungen.

---

## Architektur

- **Backend**: Java 21, **Spring Boot 3** (Web, Validation, Security, Data JPA, Actuator), **Flyway** (Migration), **Springdoc OpenAPI**.
- **Frontend**: **React** + Vite, Fetch Tailwind (optional).
- **DB**: Lokal **H2** (Dev/Test)
- **Build/Run**: Maven, Docker/Docker Compose.
- **Auth**: stateless JWT (Access + Refresh), Rollen: `ADMIN`, `STAFF`, `CUSTOMER`.

### Komponenten

- **API Gateway/Controller-Layer** (REST)
- **Service-Layer** (Businesslogik: Regelauswertung, Ledger-Buchungen)
- **Persistence-Layer** (JPA-Entities, Repositories)
- **Frontend SPA** (Dashboard für Kund:innen & Personal)

---

## Domänenmodell (Kernauszug)

- **Customer** (E-Mail eindeutig, Status)  
- **Restaurant** / **Branch** (Filialen, ISO-Ländercode)  
- **LoyaltyAccount** (UNIQUE pro (Customer, Restaurant), Tier, current_points optional materialisiert)  
- **Purchase** (FK auf Account & Branch, Währungs-Check, Brutto=Netto+VAT)  
- **PointRule** (points_per_currency, Zeitraum, aktiv)  
- **PointLedger** (EARN/REDEEM/ADJUST/EXPIRE, delta≠0, balance_after)  
- **Reward** (points_cost > 0, gültig von/bis)  
- **Redemption** (FKs auf Account/Reward/Branch, Status)

**Kardinalitäten (Auszug):**  
Customer 1—N LoyaltyAccount, Restaurant 1—N Branch, LoyaltyAccount 1—N Purchase/PointLedger/Redemption, Reward 1—N Redemption; Redemption 1—1 PointLedger (Negativbuchung).

---

## User Stories (Beispiele)

- **Als Kund:in** möchte ich Belege scannen/anlegen, damit automatisch Punkte gutgeschrieben werden.  
- **Als Kund:in** möchte ich Rewards im gewünschten Branch einlösen.  
- **Als Admin** möchte ich Regeln & Rewards zeitlich begrenzen (Kampagnen).  
- **Als Auditor** möchte ich alle Buchungen nachvollziehen können (Journal).

---

## REST API (Entwurf)

> Basis-Pfad: `/api/v1`

### Auth
- `POST /auth/register` (Customer)  
- `POST /auth/login` → JWT  
- `POST /auth/refresh`

### Stammdaten
- `GET/POST /restaurants`, `GET/POST /restaurants/{id}/branches`

### Accounts & Kunden
- `GET /customers/me`  
- `GET /customers/{id}/accounts`  
- `POST /accounts` (customer_id, restaurant_id)

### Käufe & Punkte
- `POST /purchases` (account_id, branch_id, amounts, currency, purchased_at) → Service berechnet Punkte gemäß aktiver **PointRule** und schreibt **PointLedger(EARN)** inkl. `balance_after`.  
- `GET /accounts/{id}/ledger?from=&to=` (paginierbar)

### Regeln & Rewards
- `GET/POST /restaurants/{id}/pointrules`  
- `GET/POST /restaurants/{id}/rewards`

### Einlösen
- `POST /redemptions` (account_id, reward_id, branch_id)

---

## Geschäftslogik (Kurz)

1. **Purchase erfasst** → Rule-Engine ermittelt Punkte → **Ledger(EARN)** + `balance_after`.  
2. **Redemption** → Prüfen, ob `current_points ≥ points_cost`; **Ledger(REDEEM)** (negativ) + **Redemption**.  
3. **Adjust/Expire** → Administrative Buchungen/automatische Verfalljobs über **Ledger**.

---

## Datenbank & Migrations

- **Schema erfüllt 3NF**; keine transitiven Abhängigkeiten.  
- **Integrität & Löschregeln**: RESTRICT für Stammdaten; CASCADE nur wo rechtlich zulässig.  
- **Flyway**: `V1__baseline.sql` enthält Tabellen **Customer, Restaurant, Branch, LoyaltyAccount, Purchase, PointRule, PointLedger, Reward, Redemption**.

---

## Tech-Stack (Vorschlag)

- **Backend**: Java 21, Spring Boot 3.x, Spring Data JPA (Hibernate), Spring Security (JWT), Validation, Actuator, Flyway, springdoc-openapi, MapStruct.  
- **DB**: H2 (Dev/Test), PostgreSQL 16 (Prod).  
- **Frontend**: React 18, Vite, React Router, Zustand/RTK Query, TailwindCSS.  
- **Tooling**: Maven, Docker, Testcontainers (Integration), JUnit 5, Mockito.

---

## Projektstruktur

```
/backend
  /src/main/java/.../api (controllers, dtos)
  /src/main/java/.../domain (entities, enums)
  /src/main/java/.../service (rules, ledger, redemption)
  /src/main/java/.../repo (Spring Data)
  /src/main/resources/db/migration (Flyway SQL)
  /src/main/resources/application-*.yml
/frontend
  /src (routes, components, api client)
/infra
  docker-compose.yml
```

---

## End-to-End-Flow (aus der Spezifikation)

1) Kunde kauft → **Purchase** wird angelegt.  
2) Punkte werden pro **PointRule** berechnet → **PointLedger(EARN)** + `balance_after`.  
3) Kunde löst **Reward** ein → **Redemption** + **PointLedger(REDEEM)** (negativ).  
4) **current_points** = SUM(points_delta) oder synchron gehalten.

---

## API-Kontrakte (Beispiele)

```http
POST /api/v1/purchases
Content-Type: application/json
{
  "accountId": 123,
  "branchId": 45,
  "currency": "EUR",
  "netAmount": 20.00,
  "vatAmount": 2.00,
  "purchasedAt": "2025-09-22T18:45:00Z",
  "receiptNo": "X-2025-0001"
}
```

```http
POST /api/v1/redemptions
{
  "accountId": 123,
  "rewardId": 7,
  "branchId": 45
}
```

---

## Sicherheit

- JWT Bearer, Passwörter mit Argon2/BCrypt.  
- Rollen‐basierte Autorisierung:  
  - `CUSTOMER`: eigene Accounts, Käufe, Redemptions  
  - `STAFF`: Filial-Käufe einsehen, Redemptions bestätigen  
  - `ADMIN`: Restaurants, Branches, Rules, Rewards verwalten

---

## Non-Functional

- **Auditing** via PointLedger (vollständiges Journal).  
- **Mandantenfähigkeit**: mehrere Restaurants/Accounts pro Customer.  
- **Beobachtbarkeit**: Actuator (health, metrics), strukturierte Logs.  
- **Performance**: `current_points` optional materialisiert; fachliche Wahrheit = Ledger.

---

## Lokales Setup

### Voraussetzungen
- JDK 21, Maven 3.9+, Node 20+, Docker

### Backend starten
```bash
cd backend
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend starten
```bash
cd frontend
npm i
npm run dev
```

### Docker (DB + App)
```bash
docker compose up -d
```

---

## Environment-Variablen (Beispiel)

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/bonusapp
SPRING_DATASOURCE_USERNAME=bonusapp
SPRING_DATASOURCE_PASSWORD=secret
JWT_SECRET=change_me
```

---

## Tests

- **Unit** (Service/Rules), **Integration** (Repository + Testcontainers), **API** (MockMVC/RestAssured).  
- Seed-Daten (`data.sql`) für Demo-Zwecke.

---

## OpenAPI/Swagger

- `springdoc-openapi` → `GET /v3/api-docs`, UI unter `/swagger-ui.html`.

---

## Frontend (Screens)

- **Kund:in**: Home (Punktestand, Tier), Beleg hinzufügen, Rewards einlösen, Ledger-Historie.  
- **Staff**: Redemption-Bestätigung, Kaufübersicht nach Filiale.  
- **Admin**: Restaurants/Branches, PointRules, Rewards, Accounts.

---

## Datenmodell-Diagramm

- Die Spezifikation verweist auf ein **PlantUML-ER-Diagramm**. Legt die Datei z. B. unter `docs/er-model.puml` ab und referenziert sie in der README.

---

## Lizenz

MIT (falls nicht anders gefordert).
