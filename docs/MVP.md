# BusPulse — MVP v0.1

## 1. Product Goal

BusPulse is a Singapore bus reliability application.

Existing bus applications are good at answering:

“What bus is arriving next?”

BusPulse aims to additionally answer:

“How reliable is this bus service at this stop, especially at the time I normally travel?”

The product combines live bus-arrival information with historical observations collected over time.

---

## 2. Target User

For the MVP, the primary user is a regular Singapore bus commuter.

The initial real user is the project owner, so historical data collection will begin only for bus stops and bus services that the owner regularly uses.

The application should nevertheless be designed so broader coverage can be added later.

---

## 3. Core User Experience

### First-time user

The experience is search-first.

The user searches for a bus stop and selects a bus service.

BusPulse displays current arrival information immediately.

### Returning user

The user can save frequently used combinations of:

Bus Service + Boarding Stop

Example:

198 @ Stop 28411

These become part of a lightweight “My Commute” dashboard.

Destination stops and full multi-leg journeys are explicitly outside the initial MVP.

---

## 4. Live Arrival Information

For each of the next three buses, show:

- estimated waiting time
- crowding/load
- bus type

The live experience should remain familiar rather than attempting to redesign existing bus-arrival applications.

Historical analytics are the main product differentiator.

---

## 5. Historical Reliability Metrics

BusPulse should eventually expose three separate concepts.

### Wait Consistency

How consistent the waiting time for a service is at a particular stop and time period.

### Long-Wait Frequency

How often observed waiting time exceeds a chosen threshold.

The threshold should initially be configurable in code rather than exposed as a complex user setting.

### ETA Accuracy

How closely previous arrival predictions matched inferred actual arrival times.

Do not combine these into a single reliability score during the initial MVP.

Raw metrics should remain understandable.

---

## 6. ETA Accuracy and Measurement Uncertainty

BusPulse will poll selected LTA bus-arrival data every 60 seconds.

Because a bus may arrive between polls, exact arrival time is not always known.

Example:

Bus exists at 12:05.

Bus no longer appears at 12:06.

BusPulse should treat its inferred arrival as occurring within:

12:05–12:06

A midpoint may be used for analytical calculations where necessary, but the system should preserve the underlying uncertainty.

Principle:

Never display more precision than the collected data supports.

---

## 7. Historical Collection Scope

The first collector will monitor only selected bus-stop/service combinations used by the project owner.

It will not attempt to monitor the entire Singapore bus network.

Polling interval:

60 seconds.

Each poll should retain information about all three upcoming buses returned by the source.

Live searches and background historical collection are separate concerns.

A user may search a service for current information even if BusPulse has not accumulated historical data for that combination.

---

## 8. Data Storage Strategy

Store both:

### Raw Source Data

Preserve the original API response, preferably using PostgreSQL JSONB.

This allows:

- debugging
- replay
- reprocessing
- recovery if transformation logic changes
- access to fields we did not initially normalize

### Normalized Observations

Extract fields required for application logic and analytics into structured database columns.

Likely fields include:

- poll timestamp
- bus stop code
- service number
- upcoming-bus position (1 / 2 / 3)
- estimated arrival
- latitude
- longitude
- crowding/load
- bus type
- monitored status
- source/raw-response reference

The exact database schema should be designed before implementation.

Do not add fields merely because the upstream API exposes them. Every normalized field should have a reason for existing.

---

## 9. Initial Architecture

MVP architecture:

LTA Data Source  
→ Spring Boot Application  
→ Scheduled Historical Collector  
→ PostgreSQL  
→ Spring Boot REST API  
→ Next.js Frontend

The Spring Boot application may initially contain both the API and scheduled collector.

Do not introduce Kafka, microservices, Redis, Kubernetes, or similar infrastructure unless a demonstrated requirement appears later.

---

## 10. Technology Stack

Frontend:

- Next.js
- React
- TypeScript

Backend:

- Java
- Spring Boot

Database:

- PostgreSQL

Version control:

- Git
- GitHub

The project should gradually incorporate production-oriented engineering practices where appropriate.

Potential later practices include:

- automated tests
- CI
- static analysis
- Docker
- logging
- monitoring
- security checks
- documentation
- deployment

Do not add everything at once.

---

## 11. MVP Features

### Required

- search for a bus stop
- select/view a bus service
- retrieve current arrival information
- display next three buses
- show wait time
- show load
- show bus type
- configure a small set of historical collection targets
- poll those targets every 60 seconds
- persist raw responses
- persist normalized observations
- accumulate enough data for historical analysis
- expose at least one meaningful historical waiting-time metric
- basic responsive UI

### Desired after sufficient historical data exists

- wait consistency
- long-wait frequency
- ETA accuracy
- comparison by time of day
- comparison by weekday

---

## 12. Explicit Non-MVP Features

Do not implement during the initial version:

- full-route journey planning
- MRT integration
- multi-leg commute planning
- user accounts
- social features
- machine learning
- AI chatbot
- anomaly detection
- Singapore-wide historical collection
- Kafka/event streaming
- microservice decomposition
- complex notification systems
- native mobile application
- single opaque “reliability score”

These belong in a backlog and require justification before implementation.

---

## 13. Product Principles

### Give value before configuration

A first-time user should be able to search immediately.

Do not require an account or commute setup first.

### Historical analytics are the differentiator

Live arrival information is necessary but not the unique value of BusPulse.

### Preserve raw data

Derived analytics may change as our understanding improves.

### Be honest about uncertainty

Do not present inferred arrival times as exact measurements.

### Protect the MVP

Interesting features should be recorded in the backlog instead of automatically becoming current scope.

---

## 14. Engineering Workflow

AI/Codex may perform most implementation once requirements and architecture are sufficiently defined.

The project owner remains responsible for engineering decisions and PR approval.

For meaningful pull requests, the project owner should be able to explain:

- what changed
- why it changed
- important files/functions
- how data flows through the change
- relevant edge cases
- how the change was tested

Small changes may use a lighter review.

Do not optimize for code volume.

Optimize for understandable, testable, maintainable software.

---

## 15. Initial Definition of Done

MVP v0.1 is complete when:

1. BusPulse can retrieve real bus-arrival information.
2. A user can search/select a supported stop and service.
3. The next three buses are displayed with wait time, load, and type.
4. Selected commute combinations are collected automatically every 60 seconds.
5. Raw API responses are persisted.
6. Useful normalized observations are persisted.
7. Historical observations can be queried.
8. At least one historical reliability insight is visible in the product.
9. Core backend logic has automated tests.
10. The project can be run from documented setup instructions.
11. The owner can explain the architecture and major code paths without relying on AI-generated explanations.