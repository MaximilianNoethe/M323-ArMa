# M323-ArMa

## VehicleRental starten

```bash
cd VehicleRental
javac --enable-preview --release 21 src/*.java
java --enable-preview -cp src Main
```

## Unit Tests starten

Die Tests sind normale JUnit-5-Tests. Sie zeigen einzelne wichtige Pure Functions aus `RentalLogic`.

```bash
cd VehicleRental
mvn test
```

Wenn alles funktioniert, zeigt Maven ungefähr diese Zusammenfassung:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Die Anwendung ist eine Java-Swing-Desktop-App. In der App kann man:

- ein Vehicle auswählen
- Name, E-Mail und Geburtsdatum eingeben
- nur ab 18 Jahren ein Vehicle mieten
- eine Unterlagen-E-Mail simulieren
- simulieren, dass Führerschein und ID mitgeschickt wurden
- sehen, ob ein Vehicle aktuell vermietet ist
- sehen, ab wann ein vermietetes Vehicle wieder verfügbar ist

## Bewertungskriterien im Code

Die folgenden Punkte zeigen, wo die Anforderungen aus dem Bewertungsraster im Code erfüllt werden.

| Bewertungskriterium | Erfüllung | Wo im Code? |
| --- | --- | --- |
| Pure Functions vorhanden | Sehr gut erfüllt | `RentalLogic.getAvailableVehicles(...)`, `RentalLogic.isAvailableOn(...)`, `RentalLogic.getAvailabilityText(...)`, `RentalLogic.createDocumentRequestEmail(...)`, `RentalLogic.isValidEmail(...)`, `RentalLogic.getVehicleDescription(...)`, `RentalLogic.getVehicleDetails(...)` |
| Daten sind immutable gehalten | Sehr gut erfüllt | `Person`, `Contract`, `Car`, `AirVehicle`, `WaterVehicle` sind `record`s. `RentalState` kopiert Listen mit `List.copyOf(...)`. `RentalLogic.createContract(...)` erstellt einen neuen `RentalState`, statt den alten zu verändern. |
| Rekursion wird angewendet | Sehr gut erfüllt | `RentalLogic.calculateTotalContractValueRecursive(...)` berechnet den Gesamtumsatz rekursiv. |
| Funktionale Features | Sehr gut erfüllt | `record`, `sealed interface`, `switch` Pattern Matching, `Optional`, `map`, `filter`, `anyMatch`, `noneMatch`, `findFirst`, `max`, `toList` |
| Higher-Order-Functions | Sehr gut erfüllt | Stream-Methoden wie `filter`, `map`, `anyMatch`, `noneMatch`, `findFirst` und `forEach` bekommen Lambdas oder Method References übergeben. |
| Pipelines | Sehr gut erfüllt | Mehrere Stream-Pipelines in `RentalLogic`, z. B. Verfügbarkeit, Kollisionen, Sperrliste und verfügbare Vehicles. |
| Präsentation bei Lehrperson | Sehr gut erfüllbar | `Main.java` enthält das Swing-Frontend mit Fahrzeugübersicht, Mietformular, Validierung, Mail-Simulation und Statusmeldungen. |

## 1. Pure Functions

Pure Functions sind Funktionen, die aus Eingaben einen Rückgabewert berechnen und keine bestehenden Daten direkt verändern.

Beispiele:

- [`RentalLogic.getAvailableVehicles(...)`](VehicleRental/src/RentalLogic.java) berechnet aus `RentalState` und Datum die verfügbaren Vehicles.
- [`RentalLogic.isAvailableOn(...)`](VehicleRental/src/RentalLogic.java) prüft die Verfügbarkeit für ein Vehicle an einem Datum.
- [`RentalLogic.getAvailabilityText(...)`](VehicleRental/src/RentalLogic.java) erzeugt den Text "Heute verfügbar" oder "Vermietet bis ...".
- [`RentalLogic.createDocumentRequestEmail(...)`](VehicleRental/src/RentalLogic.java) erzeugt den simulierten E-Mail-Text.
- [`RentalLogic.isValidEmail(...)`](VehicleRental/src/RentalLogic.java) prüft eine E-Mail-Adresse.
- [`RentalLogic.getVehicleDescription(...)`](VehicleRental/src/RentalLogic.java) und [`RentalLogic.getVehicleDetails(...)`](VehicleRental/src/RentalLogic.java) erzeugen Ausgabetexte für Vehicles.

Wichtig für die Erklärung: Diese Methoden verändern keine bestehende Liste und kein bestehendes Objekt. Sie berechnen nur ein Ergebnis.

## 2. Immutable Daten

Die Fach-Daten sind bewusst immutable modelliert:

- [`Person`](VehicleRental/src/Person.java) ist ein `record`.
- [`Contract`](VehicleRental/src/Contract.java) ist ein `record`.
- [`Car`](VehicleRental/src/Car.java), [`AirVehicle`](VehicleRental/src/AirVehicle.java) und [`WaterVehicle`](VehicleRental/src/WaterVehicle.java) sind `record`s.
- [`RentalState`](VehicleRental/src/RentalState.java) ist ein `record`.
- Im Konstruktor von `RentalState` werden alle Listen mit `List.copyOf(...)` kopiert.

Bei einer neuen Miete wird der alte State nicht direkt verändert. In [`RentalLogic.createContract(...)`](VehicleRental/src/RentalLogic.java) werden neue Listen erzeugt und danach ein neuer `RentalState` zurückgegeben.

Das ist wichtig für funktionales Programmieren: Daten werden nicht mutiert, sondern neue Datenstände werden erzeugt.

## 3. Rekursion

Rekursion ist in [`RentalLogic.calculateTotalContractValueRecursive(...)`](VehicleRental/src/RentalLogic.java) umgesetzt.

Die Methode funktioniert so:

1. Wenn die Liste leer ist, wird `0.0` zurückgegeben.
2. Sonst wird der erste Vertrag berechnet.
3. Danach ruft sich die Methode mit der Restliste nochmals selbst auf.

Dadurch wird der Gesamtumsatz ohne klassische Schleife rekursiv berechnet.

## 4. Funktionale Features

Im Projekt werden mehrere gelernte funktionale Features verwendet:

- `record` für immutable Datenmodelle: `Person`, `Contract`, `Car`, `AirVehicle`, `WaterVehicle`, `RentalState`
- `sealed interface` für geschlossene Typ-Hierarchien: [`Vehicle`](VehicleRental/src/Vehicle.java), [`Result`](VehicleRental/src/Result.java)
- Pattern Matching mit `switch`: `RentalLogic.getVehicleDescription(...)` und `RentalLogic.getVehicleDetails(...)`
- `Optional`: `RentalLogic.getNextAvailableDate(...)` und `RentalLogic.getAvailabilityText(...)`
- `map`, `filter`, `anyMatch`, `noneMatch`, `findFirst`, `max`, `toList`: vor allem in [`RentalLogic`](VehicleRental/src/RentalLogic.java)

Diese Features zeigen, dass nicht nur objektorientiert, sondern auch funktional gearbeitet wurde.

## 5. Higher-Order-Functions

Higher-Order-Functions sind Funktionen, die andere Funktionen als Parameter bekommen.

In Java passiert das im Projekt über Streams und Lambdas:

- `filter(c -> c.vehicle().equals(vehicle))`
- `map(Contract::vehicle)`
- `anyMatch(c -> datesOverlap(...))`
- `noneMatch(c -> isDateInContract(...))`
- `forEach(...)` in [`Main.refreshVehicleCards(...)`](VehicleRental/src/Main.java)

Diese Stellen erfüllen das Kriterium, weil den Stream-Methoden Verhalten als Lambda oder Method Reference übergeben wird.

## 6. Pipelines

Pipelines sind im Projekt an mehreren Stellen umgesetzt. Eine Pipeline verarbeitet Daten Schritt für Schritt.

Beispiele aus [`RentalLogic`](VehicleRental/src/RentalLogic.java):

- `checkCollision(...)`: Verträge filtern und danach prüfen, ob Daten überlappen.
- `getAvailableVehicles(...)`: vermietete Vehicles herausfiltern und verfügbare Vehicles zurückgeben.
- `getNextAvailableDate(...)`: Verträge filtern, Enddatum um einen Tag erhöhen und spätestes Datum bestimmen.
- `getAvailabilityText(...)`: aktiven Vertrag suchen und daraus einen Status-Text erstellen.

Dadurch ist die Datenverarbeitung gut nachvollziehbar und funktional aufgebaut.

## 7. Präsentation / Frontend

Das CLI wurde durch ein Swing-Frontend ersetzt. Die Präsentation ist in [`Main.java`](VehicleRental/src/Main.java) umgesetzt.

Das Frontend zeigt:

- Vehicle-Liste mit Verfügbarkeit
- technische Details pro Vehicle
- Preis pro Stunde
- ob ein Vehicle heute verfügbar ist
- bei vermieteten Vehicles das Datum, ab wann sie wieder verfügbar sind
- Mietformular mit Vehicle-Auswahl
- Name, E-Mail-Adresse und Geburtsdatum
- Start- und Enddatum
- Unterlagen-E-Mail-Simulation
- Checkbox für Führerschein und ID
- verständliche Fehlermeldungen

Für die Präsentation kann man gut zeigen, dass die UI nur Eingaben sammelt und die eigentliche Fachlogik in `RentalLogic` liegt.
