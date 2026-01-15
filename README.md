# Bonus-App (Restaurant Loyalty)

## Uebersicht
Die Bonus-App ist ein Spring-Boot-Projekt zur Verwaltung von Treuepunkten in Restaurants.
Kunden sammeln Punkte bei Einkaeufen und loesen diese gegen Rewards ein.
Die Punktebewegungen werden als Journal im PointLedger gespeichert; das Ledger ist die Source of Truth.

## Technologie-Stack
- Spring Boot 3.x
- Spring Web (REST)
- Spring Data JPA
- Flyway (DB-Migration)
- H2 (In-Memory fuer Entwicklung/Test)
- Optional: PostgreSQL (application-postgres.properties)

## Architektur
Schichten und Packages:
- Controller: REST-Endpunkte
- Service: Geschaeftslogik
- Repository: Datenzugriff
- Entity: JPA-Modelle

Package-Struktur:
```
at.htlle
  controller
  service
  repository
  entity
  dto
  config
```

## Datenbankdesign (3NF)
Kernentitaeten:
- Customer
- Restaurant
- Branch
- LoyaltyAccount (UNIQUE(customer_id, restaurant_id), current_points >= 0)
- Purchase (branch_id NOT NULL, total_amount > 0, currency ISO-3)
- PointRule (restaurant-spezifisch, zeitlich gueltig, aktiv)
- PointLedger (EARN/REDEEM/ADJUST/EXPIRE, points != 0, balance_after)
- Reward (cost_points > 0, aktiv, gueltig)
- Redemption (account, reward, branch, ledger_entry_id 1:1)

Wichtige Regeln:
- Source of Truth fuer Punkte ist das PointLedger.
- current_points im LoyaltyAccount ist materialisiert und wird konsistent gehalten.
- Fremdschluessel sind NOT NULL (Ausnahme: purchase_id/point_rule_id im Ledger).

## REST-API
Basis: `/api`

### POST /api/purchases
Request:
```json
{
  "accountId": 1,
  "branchId": 1,
  "purchaseNumber": "PUR-001",
  "totalAmount": 123.45,
  "currency": "EUR",
  "purchasedAt": "2025-01-01T10:15:30Z",
  "notes": "Mittagessen",
  "description": "Kauf im Lokal",
  "pointRuleId": 1
}
```
Response:
```json
{
  "purchaseId": 10,
  "purchaseNumber": "PUR-001",
  "totalAmount": 123.45,
  "currency": "EUR",
  "purchasedAt": "2025-01-01T10:15:30Z",
  "accountId": 1,
  "branchId": 1,
  "ledgerEntryId": 42,
  "points": 123,
  "balanceAfter": 123
}
```

### POST /api/redemptions
Request:
```json
{
  "accountId": 1,
  "rewardId": 1,
  "branchId": 1,
  "notes": "Welcome Drink"
}
```
Response:
```json
{
  "redemptionId": 7,
  "accountId": 1,
  "rewardId": 1,
  "branchId": 1,
  "ledgerEntryId": 43,
  "pointsSpent": 50,
  "balanceAfter": 73,
  "status": "COMPLETED",
  "redeemedAt": "2025-01-01T10:20:00Z"
}
```

### POST /api/accounts/{id}/sync
Synchronisiert current_points aus dem Ledger.
Optional: `includeLedger=true` fuer Ledger-Ausgabe.

### GET /api/accounts/{id}
Optional: `includeLedger=true`

## Fehlerformat
Fehlerantworten sind einheitlich:
```json
{
  "timestamp": "2025-01-01T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Total amount must be greater than zero",
  "path": "/api/purchases"
}
```

## Lokales Setup
1) Tests ausfuehren:
```
mvn clean test
```
2) Anwendung starten:
```
mvn spring-boot:run
```
3) H2-Console (optional):
- URL: `http://localhost:8080/h2-console`
- JDBC: `jdbc:h2:mem:bonusapp`

## Hinweise
- Flyway-Migrationen liegen unter `src/main/resources/db/migration`.
- H2 laeuft im PostgreSQL-Kompatibilitaetsmodus.
