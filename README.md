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
curl http://localhost:8082/api/hackathons
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
Per provare le API in Postman, puoi creare utenti via `POST /api/users`
e poi fare login con `POST /api/auth/login` per ottenere l'id utente.

Se vuoi inserire manualmente utenti nel DB (H2 console: `http://localhost:8082/h2-console`), puoi usare SQL.
Nota: `password_hash` deve contenere una hash BCrypt valida se vuoi usare la login. In alternativa,
puoi registrare utenti via API e usare direttamente l'`X-USER-ID` nelle chiamate protette.
Credenziali H2 di default:
- JDBC URL: `jdbc:h2:mem:hackhub`
- User: `sa`
- Password: vuota

Esempio SQL minimo:
```sql
insert into users (id, email, nome, cognome, password_hash)
values (1, 'org@hackhub.dev', 'Organizer', 'One', null);
insert into users (id, email, nome, cognome, password_hash)
values (2, 'judge@hackhub.dev', 'Judge', 'Two', null);
insert into users (id, email, nome, cognome, password_hash)
values (3, 'mentor@hackhub.dev', 'Mentor', 'Three', null);
insert into users (id, email, nome, cognome, password_hash)
values (4, 'member@hackhub.dev', 'Member', 'Four', null);

insert into user_roles (user_id, role) values (1, 'ORGANIZER');
insert into user_roles (user_id, role) values (2, 'JUDGE');
insert into user_roles (user_id, role) values (3, 'MENTOR');
insert into user_roles (user_id, role) values (4, 'REGISTERED_USER');
```
Nota: richiede che l'utente chiamante abbia ruolo `ADMIN`.
Se non hai un admin, crea uno in H2:

```sql
insert into users (id, email, nome, cognome, password_hash)
values (99, 'admin@hackhub.dev', 'Admin', 'User', null);
insert into user_roles (user_id, role) values (99, 'ADMIN');
```
Ruoli validi: `ADMIN`, `ORGANIZER`, `JUDGE`, `MENTOR`, `REGISTERED_USER`.

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

## Flusso Postman completo (esempio)
Negli esempi seguenti usa la porta effettiva scelta in avvio (`8082` o alternativa).
Per le operazioni con utente loggato aggiungi header `X-USER-ID`.

1. Crea utenti (organizzatore, giudice, mentore, membro team)
```bash
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"org@hackhub.dev","password":"Password123","nome":"Org","cognome":"One"}'
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"judge@hackhub.dev","password":"Password123","nome":"Judge","cognome":"Two"}'
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"mentor@hackhub.dev","password":"Password123","nome":"Mentor","cognome":"Three"}'
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"member@hackhub.dev","password":"Password123","nome":"Member","cognome":"Four"}'
```

2. Crea hackathon (organizzatore)
```bash
curl -X POST http://localhost:8082/api/hackathons \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{
    "name": "HackHub 2026",
    "description": "Hackathon demo",
    "rules": "General rules",
    "registrationDeadline": "2026-01-10T09:00:00",
    "startDate": "2026-01-11T09:00:00",
    "endDate": "2026-01-12T18:00:00",
    "location": "Ancona",
    "prizeMoney": 1500,
    "maxTeamSize": 5,
    "giudiceId": 2,
    "mentoriIds": [3]
  }'
```

3. Crea team (membro team)
```bash
curl -X POST http://localhost:8082/api/teams \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"name":"TeamAlpha","maxSize":4}'
```

4. Iscrivi team a hackathon
```bash
curl -X POST http://localhost:8082/api/hackathons/1/teams/1/registrations \
  -H "X-USER-ID: 4"
```

5. Avvia hackathon (organizzatore)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/start \
  -H "X-USER-ID: 1" \
```
Nota: le transizioni sono vincolate alle date. Se la transizione fallisce, imposta `registrationDeadline`,
`startDate` ed `endDate` nel passato quando crei l'hackathon.

6. Carica sottomissione (solo quando lo stato è `IN_CORSO`)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/submissions \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"title":"Project Alpha","description":"Demo","repoUrl":"https://github.com/teamalpha/project"}'
```

7. Avvia valutazione (organizzatore)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/start-evaluation \
  -H "X-USER-ID: 1"
```

8. Valuta sottomissione (giudice)
```bash
curl -X GET http://localhost:8082/api/submissions/judge -H "X-USER-ID: 2"

curl -X POST http://localhost:8082/api/submissions/1/evaluation \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 2" \
  -d '{"punteggio":8,"giudizio":"Progetto solido"}'
```

9. Proclama vincitore (organizzatore)
```bash
curl -X GET http://localhost:8082/api/hackathons/1/winner/candidates -H "X-USER-ID: 1"

curl -X POST http://localhost:8082/api/hackathons/1/winner/1 -H "X-USER-ID: 1"
```

10. Concludi hackathon (organizzatore)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/conclude -H "X-USER-ID: 1"
```

11. Eroga premio (organizzatore)
```bash
curl -X POST http://localhost:8082/api/pagamenti/hackathons/1/prize -H "X-USER-ID: 1"
```

12. Supporto e call (mentore)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/support-requests \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"message":"Serve aiuto su API"}'

curl -X GET http://localhost:8082/api/support-requests/mentor -H "X-USER-ID: 3"

curl -X POST http://localhost:8082/api/support-requests/1/propose-call \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 3" \
  -d '{"slotPreferiti":["2026-02-02T10:00:00","2026-02-02T11:00:00"]}'
```

13. Inviti team
```bash
curl -X POST http://localhost:8082/api/invites \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"destinatarioId":2,"teamId":1}'

curl -X GET http://localhost:8082/api/invites -H "X-USER-ID: 2"

curl -X POST http://localhost:8082/api/invites/1/accept -H "X-USER-ID: 2"
```

14. Segnalazione violazione (mentore)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/violations \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 3" \
  -d '{"descrizione":"Team ha copiato codice","motivazione":"Violazione regolamento"}'
```

## API per ciascun caso d'uso (API GENERATE AD HOC PER TESTING CASI D'USO)
1. Registrarsi alla piattaforma
```bash
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"user@hackhub.dev","password":"Password123","nome":"Mario","cognome":"Rossi"}'
```

2. Effettuare login
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@hackhub.dev","password":"Password123"}'
```

3. Consultare elenco hackathon
```bash
curl -X GET http://localhost:8082/api/hackathons
```

4. Creare hackathon (include assegnazione giudice e mentori)
```bash
curl -X POST http://localhost:8082/api/hackathons \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{
    "name": "HackHub 2026",
    "description": "Hackathon demo",
    "rules": "General rules",
    "registrationDeadline": "2026-01-10T09:00:00",
    "startDate": "2026-01-11T09:00:00",
    "endDate": "2026-01-12T18:00:00",
    "location": "Ancona",
    "prizeMoney": 1500,
    "maxTeamSize": 5,
    "giudiceId": 2,
    "mentoriIds": [3]
  }'
```

5. Aggiungere mentore
```bash
curl -X POST http://localhost:8082/api/hackathons/1/mentors \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{"mentorUserId":3}'
```

6. Creare team
```bash
curl -X POST http://localhost:8082/api/teams \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"name":"TeamAlpha","maxSize":4}'
```

7. Invitare utente a team
```bash
curl -X POST http://localhost:8082/api/invites \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"destinatarioId":2,"teamId":1}'
```

8. Accettare invito a team
```bash
curl -X POST http://localhost:8082/api/invites/1/accept -H "X-USER-ID: 2"
```

9. Abbandonare team
```bash
curl -X POST http://localhost:8082/api/teams/leave -H "X-USER-ID: 4"
```

10. Iscrivere team a hackathon
```bash
curl -X POST http://localhost:8082/api/hackathons/1/teams/1/registrations \
  -H "X-USER-ID: 4"
```

11. Verificare stato hackathon
```bash
curl -X GET http://localhost:8082/api/hackathons/1
```

12. Verificare requisiti team (controllo incluso nell'iscrizione)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/teams/1/registrations \
  -H "X-USER-ID: 4"
```

13. Caricare sottomissione
```bash
curl -X POST http://localhost:8082/api/hackathons/1/submissions \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"title":"Project Alpha","description":"Demo","repoUrl":"https://github.com/teamalpha/project"}'
```

14. Visualizzare sottomissioni (giudice)
```bash
curl -X GET http://localhost:8082/api/submissions/judge -H "X-USER-ID: 2"
```

15. Valutare sottomissione (giudice)
```bash
curl -X POST http://localhost:8082/api/submissions/1/evaluation \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 2" \
  -d '{"punteggio":8,"giudizio":"Progetto solido"}'
```

16. Inviare richiesta supporto
```bash
curl -X POST http://localhost:8082/api/hackathons/1/support-requests \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" \
  -d '{"message":"Serve aiuto su API"}'
```

17. Visualizzare richiesta supporto (mentore)
```bash
curl -X GET http://localhost:8082/api/support-requests/mentor -H "X-USER-ID: 3"
```

18. Proporre call supporto (mentore)
```bash
curl -X POST http://localhost:8082/api/calls \
  -H "Content-Type: application/json" \
  -d '{
    "mentorId": 3,
    "richiestaId": 1,
    "dataProposta": "2026-02-26T10:00:00",
    "durataMin": 30,
    "calendarEventId": "CAL-1-1700000000000:2026-02-26T10:00:00"
  }'
```

19. Pianificare call (mentore)
```bash
curl -X POST http://localhost:8082/api/support-requests/1/propose-call \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 3" \
  -d '{"slotPreferiti":["2026-02-26T10:00:00","2026-02-26T11:00:00"]}'
```

20. Aggiornare stato hackathon
```bash
curl -X PUT http://localhost:8082/api/hackathons/1/status \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{"nuovoStato":"IN_VALUTAZIONE"}'
```

21. Proclamare team vincitore
```bash
curl -X POST http://localhost:8082/api/hackathons/1/winner/1 -H "X-USER-ID: 1"
```

22. Erogare premio
```bash
curl -X POST http://localhost:8082/api/pagamenti/hackathons/1/prize -H "X-USER-ID: 1"
```

23. Segnalare violazione (mentore)
```bash
curl -X POST http://localhost:8082/api/hackathons/1/violations \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 3" \
  -d '{"descrizione":"Team ha copiato codice","motivazione":"Violazione regolamento"}'
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
- `src/main/java/it/ids/hackathown/service/SubmissionService.java`
- `src/main/java/it/ids/hackathown/service/SupportoService.java`

## Adapter esterni (stub)
- Calendar: `src/main/java/it/ids/hackathown/integration/calendar`
- Payment: `src/main/java/it/ids/hackathown/integration/payment`
