# MFS FraudGraph – F1 & F2

- **F1** – Import a CSV dataset and save it as a versioned snapshot.
- **F2** – Validate required fields, positive amounts, timestamps and duplicate IDs.

Java 21 · Spring Boot 4.1.2 (Spring MVC) · Thymeleaf · MongoDB

## Setup (once)

1. Install **JDK 21** and the VS Code **Extension Pack for Java** + **Spring Boot Extension Pack**.
2. Install and start **MongoDB** on port 27017 (MongoDB Community Server, or
   `docker run -d -p 27017:27017 --name fraudgraph-mongo mongo:8`).

## Run

- VS Code: open `FraudGraphApplication.java` → **Run**, or
- Terminal: `mvnw.cmd spring-boot:run` (Windows) / `./mvnw spring-boot:run` (Mac/Linux)

Open http://localhost:8080 and upload a file from `sample-data/`.

## Test (MongoDB not needed)

`mvnw.cmd test` (Windows) / `./mvnw test` (Mac/Linux), or the Testing (flask) panel in VS Code.

## How it works

```
Browser ──upload──> ImportController ──> ImportService ──> CsvRowValidator (F2 checks)
                                              │
                                              └──> Repositories ──> MongoDB
```

`ImportService.importCsv()` does everything in five numbered steps:

1. Check the header has all 8 columns.
2. For each row: run the F2 checks; then check the transaction ID
   (new → keep, exact repeat → ignore, same ID with different details → report conflict).
3. If no row is valid → status `REJECTED`, no snapshot.
4. Save the batch and a new snapshot with version = latest + 1.
5. Link every good row to the snapshot and save them.

Before that, `ImportController` asks `findSameFile()` whether this exact file was already imported
(using a SHA-256 fingerprint). If yes, nothing is saved again.

## Files

| MVC part | File | Feature |
|---|---|---|
| Model | `domain/ImportBatch` – one upload and its result | F1 |
| Model | `domain/DatasetSnapshot` – a saved version (v1, v2, …) | F1 |
| Model | `domain/Transaction` – one row, linked to a snapshot | F1 |
| Model | `domain/ImportIssue` – one problem in one row | F2 |
| Repository | `repository/*Repository` – save/read MongoDB | F1 |
| Service | `service/ImportService` – the import steps above | F1, F2 |
| Service | `service/RowValidator`, `CsvRowValidator` – checks one row | F2 |
| Controller | `controller/ImportController` | F1, F2 |
| View | `templates/import.html` (ImportPage), `templates/validation.html` (ValidationPage) | F1, F2 |

## CSV format

```
transactionId,senderProvider,senderAccountId,receiverProvider,receiverAccountId,amount,timestamp,status
T001,BKASH,A001,NAGAD,B001,1500.00,2026-11-01T10:00:00,Completed
```

Timestamps are Bangladesh time, written as `yyyy-MM-ddTHH:mm:ss`.

## Acceptance criteria → tests

| SRS | Test |
|---|---|
| F1: 10 valid rows are stored once and linked to one snapshot | `ImportServiceTest.tenValidRowsAreStoredOnceAndLinkedToOneSnapshot` |
| F1: versioned snapshot | `firstSnapshotIsVersion1`, `nextSnapshotIsLatestVersionPlusOne` |
| F2: missing senders are rejected | `CsvRowValidatorTest.missingSenderIsRejected`, `ImportServiceTest.rowWithMissingSenderIsRejectedButOtherRowsAreSaved` |
| F2: identical retries are ignored | `exactRepeatOfARowIsIgnored`, `sameFileIsFoundByItsHash` |
| F2: conflicting IDs are reported | `sameIdWithDifferentDetailsIsReported` |
| F2: positive amounts, valid timestamps | `CsvRowValidatorTest` (zero, negative, not a number, bad timestamp) |
