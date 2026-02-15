# HackHub Backend

Backend Spring Boot (Java 21) per il progetto d'esame HackHub.

## Prerequisiti
- JDK 21 installato
- Gradle Wrapper incluso nel progetto (non serve installare Gradle)

## Stack
- Spring Boot 3.4
- Spring Web + Spring Data JPA + Bean Validation
- H2 (default) e profilo MySQL
- Lombok

## Avvio rapido
1. Avvia l'app (porta default `8082`):
```powershell
.\gradlew.bat bootRun
```
2. Verifica che sia attiva:
```bash
curl http://localhost:8082/
```
3. Header obbligatorio per le API protette logicamente: `X-USER-ID`

Se la porta `8082` e occupata:
```powershell
.\gradlew.bat bootRun --args="--server.port=8084"
```
oppure:
```powershell
java -jar build/libs/hackathown-0.0.1-SNAPSHOT.jar --server.port=8084
```

Controllo porte occupate (Windows):
```powershell
Get-NetTCPConnection -LocalPort 8082 -State Listen
```

## Profili database
- Default (H2 in-memory): nessun parametro extra
- MySQL:
```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=mysql"
```
Variabili utili per MySQL:
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Test
```powershell
.\gradlew.bat test
```

## Setup dati minimi (H2)
Ora esistono endpoint User per registrazione e login. Per provare le API, puoi creare utenti via `POST /api/users`
e poi fare login con `POST /api/auth/login` per ottenere l'id utente.

Se vuoi inserire manualmente utenti nel DB (H2 console: `http://localhost:8082/h2-console`), puoi continuare
a usare SQL. Nota: `password_hash` e opzionale; senza di essa la login fallira, ma puoi comunque usare l'`X-USER-ID`
per le API che lo richiedono.
Credenziali H2 di default:
- JDBC URL: `jdbc:h2:mem:hackhub`
- User: `sa`
- Password: vuota

Esempio SQL minimo:
```sql
insert into users (id, email, name) values (1, 'org@hackhub.dev', 'Organizer');
insert into users (id, email, name) values (2, 'judge@hackhub.dev', 'Judge');
insert into users (id, email, name) values (3, 'mentor@hackhub.dev', 'Mentor');
insert into users (id, email, name) values (4, 'member@hackhub.dev', 'Member');

insert into user_roles (user_id, roles) values (1, 'ORGANIZER');
insert into user_roles (user_id, roles) values (2, 'JUDGE');
insert into user_roles (user_id, roles) values (3, 'MENTOR');
insert into user_roles (user_id, roles) values (4, 'REGISTERED_USER');
```

## Login e registrazione (curl)
Registrazione utente:
```bash
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@hackhub.dev",
    "password": "Password123",
    "nome": "Mario",
    "cognome": "Rossi"
  }'
```

Login:
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@hackhub.dev",
    "password": "Password123"
  }'
```

## Admin: assegna ruoli (curl)
Nota: richiede che l'utente chiamante abbia ruolo `ADMIN`.
```bash
curl -X PUT http://localhost:8082/api/admin/users/1/roles \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 99" \
  -d '{
    "roles": ["ORGANIZER", "JUDGE"]
  }'
```

## 5 endpoint chiave (curl)
Negli esempi seguenti usa la porta effettiva scelta in avvio (`8082` o alternativa).

1. Crea hackathon
```bash
curl -X POST http://localhost:8082/api/hackathons \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{
    "name": "HackHub 2026",
    "rules": "General rules",
    "registrationDeadline": "2026-03-01T18:00:00",
    "startDate": "2026-03-02T09:00:00",
    "endDate": "2026-03-03T18:00:00",
    "location": "Ancona",
    "prizeMoney": 1500,
    "maxTeamSize": 5,
    "judgeUserId": 2,
    "mentorUserIds": [3],
    "scoringPolicyType": "INNOVATION_WEIGHTED",
    "validationPolicyType": "REPO_REQUIRED"
  }'
```

2. Crea team
```bash
curl -X POST http://localhost:8082/api/teams \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{
    "name": "TeamAlpha",
    "maxSize": 4
  }'
```

3. Iscrivi team a hackathon
```bash
curl -X POST http://localhost:8082/api/hackathons/1/registrations \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{
    "teamId": 1
  }'
```

4. Avvia hackathon e invia submission
```bash
curl -X POST http://localhost:8082/api/hackathons/1/start -H "X-USER-ID: 1"

curl -X POST http://localhost:8082/api/hackathons/1/submissions \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{
    "repoUrl": "https://github.com/teamalpha/project",
    "description": "Detailed project description with enough characters for validation strategy"
  }'
```

5. Valuta e proclama vincitore
```bash
curl -X POST http://localhost:8082/api/hackathons/1/start-evaluation -H "X-USER-ID: 1"

curl -X POST http://localhost:8082/api/submissions/1/evaluations \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 2" \
  -d '{
    "judgeScore": 8.5,
    "innovationScore": 9.0,
    "technicalScore": 7.5,
    "comment": "Strong project"
  }'

curl -X POST http://localhost:8082/api/hackathons/1/declare-winner -H "X-USER-ID: 1"
```

6. Elimina hackathon (esempio: id `2`)
```bash
curl -X DELETE http://localhost:8082/api/hackathons/2 \
  -H "X-USER-ID: 1"
```
Nota: la cancellazione e consentita solo all'organizer assegnato a quell'hackathon.

## Dove sono applicati i pattern
### Strategy
- Scoring: `src/main/java/it/ids/hackathown/domain/strategy/scoring`
  - `ScoringStrategy`
  - `DefaultScoringStrategy`
  - `InnovationWeightedScoringStrategy`
  - `TechnicalWeightedScoringStrategy`
  - `ScoringStrategyRegistry`
- Validazione submission: `src/main/java/it/ids/hackathown/domain/strategy/validation`
  - `SubmissionValidationStrategy`
  - `BasicSubmissionValidationStrategy`
  - `RepoRequiredValidationStrategy`
  - `ZipAndDescriptionValidationStrategy`
  - `SubmissionValidationStrategyRegistry`

Uso nel service:
- `src/main/java/it/ids/hackathown/service/EvaluationService.java` (scoring)
- `src/main/java/it/ids/hackathown/service/SubmissionService.java` (validation)

### State
- Interfaccia e stati concreti:
  - `src/main/java/it/ids/hackathown/domain/state/HackathonState.java`
  - `src/main/java/it/ids/hackathown/domain/state/RegistrationState.java`
  - `src/main/java/it/ids/hackathown/domain/state/RunningState.java`
  - `src/main/java/it/ids/hackathown/domain/state/EvaluationState.java`
  - `src/main/java/it/ids/hackathown/domain/state/CompletedState.java`
- Factory + context:
  - `src/main/java/it/ids/hackathown/domain/state/HackathonStateFactory.java`
  - `src/main/java/it/ids/hackathown/domain/state/HackathonContext.java`

Uso nel service:
- `src/main/java/it/ids/hackathown/service/HackathonService.java`
- `src/main/java/it/ids/hackathown/service/RegistrationService.java`
- `src/main/java/it/ids/hackathown/service/SubmissionService.java`
- `src/main/java/it/ids/hackathown/service/SupportService.java`
- `src/main/java/it/ids/hackathown/service/EvaluationService.java`

## Adapter esterni (stub)
- Calendar: `src/main/java/it/ids/hackathown/integration/calendar`
- Payment: `src/main/java/it/ids/hackathown/integration/payment`
