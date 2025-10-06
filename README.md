# 🍽️ Bonus-App für Restaurants

## 📖 Überblick

Die **Bonus-App** ist ein Full-Stack-Projekt, das Restaurantkunden ermöglicht, bei Einkäufen Punkte (Bonis) zu sammeln und gegen Rewards (z. B. Rabatte oder kostenlose Produkte) einzulösen.  
Sie wurde im Rahmen des Schulprojekts **HTL Leoben – 4AIT/5AIT** von  
**Luschenz, Papic, Schüller, Schuster, Winter, Ziegerhofer** entwickelt.

Ziel ist es, ein skalierbares, nachvollziehbares und datenbankzentriertes System zu schaffen, das mehrere Restaurants unterstützt und eine transparente Verwaltung von Treuepunkten erlaubt.

---

## 🧱 Technologie-Stack

Die Anwendung basiert auf **Spring Boot** und nutzt moderne Java- und Web-Technologien:

| Komponente | Zweck |
|-------------|--------|
| **Spring Boot** | Framework zur Entwicklung moderner Webanwendungen |
| **Spring Web** | Bereitstellung von REST-APIs |
| **Spring Data JPA** | ORM-Schicht für den Datenbankzugriff |
| **Spring Validation** | Validierung von Benutzereingaben |
| **Spring Test** | Unit- und Integrationstests |
| **H2 Database** | Leichtgewichtige relationale Datenbank (lokal/produktiv) |

---

## 🧩 Architektur

Das Projekt folgt einer **mehrschichtigen Architektur** nach dem MVC-Prinzip:

```
Controller → Service → Repository → Entity (POJO)
```

### Package-Struktur (`at.htlle`)

```
at.htlle
 ├── controller   → REST-Endpunkte (z. B. /api/accounts)
 ├── service      → Geschäftslogik und Datenflusssteuerung
 ├── repository   → Datenbankzugriff über JPA-Repositorys
 ├── entity       → JPA-Entitäten (Customer, Restaurant, LoyaltyAccount, …)
 └── factory      → Objekt-/DTO-Erzeugung
```

### Design-Patterns
- **Repository-Pattern:** Trennt Datenzugriff von Logik.  
- **Service-Pattern:** Kapselt Geschäftsregeln.  
- **Factory-Pattern:** Erzeugt komplexe Objekte strukturiert.  
- **POJO-Prinzip:** Einfache, Framework-unabhängige Java-Klassen.

---

## 🔗 Datenfluss

1. **Frontend** ruft REST-Endpoint im **Controller** auf.  
2. **Controller** delegiert an **Service**.  
3. **Service** führt Logik aus und nutzt **Repository** für DB-Zugriffe.  
4. **Repository** kommuniziert via **JPA** mit der Datenbank.  
5. **Antwort** wird als **JSON** an das Frontend zurückgegeben.

---

## 🗃️ Datenbankdesign

Das System verwendet eine **relationale Datenbank** im **3. Normalform-Design (3NF)**.  
Zentrale Grundlage ist das **Entity-Relationship-Modell (ER-Diagramm)** mit folgenden Hauptentitäten:

| Entität | Zweck / Beschreibung |
|----------|-----------------------|
| **Customer** | Stammdaten der Kunden |
| **Restaurant** | Unternehmen, die das Bonussystem nutzen |
| **Branch** | Filialen eines Restaurants |
| **LoyaltyAccount** | Verknüpft Kunde & Restaurant, speichert Punkte |
| **Purchase** | Erfasste Käufe (inkl. Beträge, Filiale, Zeitpunkt) |
| **PointRule** | Regeln zur Punktevergabe |
| **PointLedger** | Journal aller Punktebewegungen (Earn, Redeem, Adjust, Expire) |
| **Reward** | Einlösbare Belohnungen |
| **Redemption** | Dokumentiert Reward-Einlösungen |

### Beziehungen (Auszug)
- `Customer 1—N LoyaltyAccount`  
- `Restaurant 1—N Branch`  
- `LoyaltyAccount 1—N Purchase`  
- `Purchase 1—N PointLedger`  
- `Restaurant 1—N Reward`  
- `LoyaltyAccount 1—N Redemption`  

Jede Einlösung (`Redemption`) ist über eine Ledger-Buchung nachvollziehbar.

---

## ⚙️ Integritätsregeln & Constraints

- **Referenzielle Integrität:** FK-Beziehungen (teilweise nullable).  
- **Löschregeln:** `ON DELETE RESTRICT` für Stammdaten, optional `CASCADE` für abhängige Datensätze.  
- **Eindeutigkeit:** `UNIQUE(customer_id, restaurant_id)` verhindert doppelte Konten.  
- **Konsistenz:** `CHECK`-Constraints für ISO-Codes, Statuswerte, Betragslogik.  
- **Ableitungen:** `current_points` = Summe aller Ledger-Einträge.

---

## 🧮 Beispielablauf – Sammeln & Einlösen

1. Kunde tätigt Kauf → `Purchase` wird angelegt.  
2. Regel (`PointRule`) berechnet Punkte → `PointLedger(EARN)`.  
3. Kunde löst Reward ein → `Redemption` + `PointLedger(REDEEM)` (negativ).  
4. `LoyaltyAccount.current_points` wird automatisch aktualisiert.

---

## 🧠 Warum dieses Design?

- **Nachvollziehbarkeit:** Vollständiges Journal über Punktebewegungen.  
- **Flexibilität:** Zeitlich begrenzte Regeln und Kampagnen.  
- **Mandantenfähigkeit:** Mehrere Restaurants, getrennte Kundenkonten.  
- **Skalierbarkeit & Performance:** Materialisierte Felder bei Bedarf.  
- **Saubere Trennung:** Strikte 3NF und klare Schichtenarchitektur.

---

## 🧑‍💻 Entwicklerteam

| Name | Klasse | Jahr |
|------|---------|------|
| Luschenz | 4AIT / 5AIT | 2024–2025 |
| Papic | 4AIT / 5AIT | 2024–2025 |
| Schüller | 4AIT / 5AIT | 2024–2025 |
| Schuster | 4AIT / 5AIT | 2024–2025 |
| Winter | 4AIT / 5AIT | 2024–2025 |
| Ziegerhofer | 4AIT / 5AIT | 2024–2025 |

---

## 📅 Versionsstand
- **Datenbankspezifikation:** 23.09.2025  
- **Technologie-Stack & Architektur:** 05.10.2025  

---

## 🏗️ Geplante Erweiterungen
- Frontend-Integration (React oder Angular)
- Benutzer-Authentifizierung (Spring Security)
- Admin-Dashboard zur Kampagnenverwaltung
- Export von Punktetransaktionen (CSV/PDF)

---

## 📜 Lizenz
Dieses Projekt wurde im Rahmen des Unterrichts an der **HTL Leoben** erstellt und dient ausschließlich zu Lern- und Demonstrationszwecken.
