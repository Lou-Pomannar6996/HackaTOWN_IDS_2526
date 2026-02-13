# HackHub Backend

Backend Spring Boot (Java 21) per il progetto d'esame HackHub.

## Stack
- Spring Boot 3.4
- Spring Web + Spring Data JPA + Bean Validation
- H2 (default) e profilo MySQL
- Lombok

## Avvio rapido
1. Avvia l'app:
```powershell
.\gradlew.bat bootRun
```
2. API disponibili su `http://localhost:8082`
3. Header obbligatorio per le API protette logicamente: `X-USER-ID`

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
Non esistono endpoint User. Per provare le API, inserisci prima utenti nel DB (H2 console: `http://localhost:8082/h2-console`).

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

## 5 endpoint chiave (curl)
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
