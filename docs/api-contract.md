# Proposed live-arrival API contract

Status: **proposal for owner review; not implemented**. Scope: the live-arrival
backend issue only. Companion document: [LTA flow](lta-flow.md).

The paths, JSON examples, limits, and error policies below are BusPulse design
choices. Example data is illustrative, not a live response. This document does
not imply that the placeholder frontend already calls these endpoints.

## Routing

| Purpose | Spring Boot path, port 8080 | Browser path through Next.js, port 3000 |
| --- | --- | --- |
| Search stops | `GET /v1/stops?q=284&limit=20` | `GET /api/v1/stops?q=284&limit=20` |
| Services at a stop | `GET /v1/stops/28411/services` | `GET /api/v1/stops/28411/services` |
| Selected service's arrivals | `GET /v1/stops/28411/services/198/arrivals` | `GET /api/v1/stops/28411/services/198/arrivals` |

The existing Next.js rewrite strips `/api` and forwards the rest. Spring Boot
owns validation, upstream credentials, and transformation. All endpoints return
JSON. The existing `/actuator/health` route remains an infrastructure check.

## 1. Search stops

`q` is required: trim whitespace, then accept 2–80 characters. Match against stop
code, description, and road name, ignoring letter case. Codes remain strings so
leading zeroes survive. Reject invalid limits rather than silently truncating
them: integer `limit`, default 20, allowed 1–50.

```json
{
  "query": "284",
  "stops": [
    {
      "code": "28411",
      "description": "Example boarding stop",
      "roadName": "Example Road"
    }
  ],
  "hasMore": false,
  "catalogRetrievedAt": "2026-10-17T00:00:00Z"
}
```

Rank exact code first, code prefixes second, remaining text matches third; break
ties by stop code. `hasMore` indicates additional matches beyond the limit. For
the MVP, users refine their query rather than paginate search results. No matches
is `200` with an empty array. No ready reference catalog is `503`, not an empty
search result. Coordinates are omitted until a feature needs them.

## 2. Services at the selected stop

```json
{
  "stopCode": "28411",
  "services": [{ "serviceNumber": "198" }],
  "catalogRetrievedAt": "2026-10-17T00:00:00Z"
}
```

Enumerate from reference route data, not just buses currently predicted to arrive.
Deduplicate a stop/service pair across route records and order service identifiers
with numeric-aware sorting. Preserve suffixes; do not parse identifiers as integers.
A known stop with no listed services returns `200` and an empty array. Do not
claim a service is operating now merely because it appears in this list.

No destinations, route planner, or direction-selection UI is included. If live
data reveals repeated visits/ambiguous loop behavior, surface the limitation and
review the contract before expanding the stop/service identity.

## 3. Arrivals for one stop/service pair

```json
{
  "stopCode": "28411",
  "serviceNumber": "198",
  "status": "AVAILABLE",
  "retrievedAt": "2026-10-17T00:00:00Z",
  "arrivals": [
    {
      "position": 1,
      "estimatedArrival": "2026-10-17T00:04:30Z",
      "waitMinutes": 4,
      "load": "SEATS_AVAILABLE",
      "busType": "DOUBLE_DECK",
      "predictionBasis": "LOCATION"
    }
  ]
}
```

- Return zero to three usable predictions. `position` retains source slot 1, 2,
  or 3; omitted slots do not cause the remaining positions to be renumbered.
- `estimatedArrival` is an ISO-8601 UTC instant. It is a prediction, never an
  inferred actual arrival. `retrievedAt` is backend receipt time, not a source
  measurement timestamp or evidence of historical collection.
- Derive `waitMinutes` at receipt time by rounding down whole positive minutes.
  A future prediction less than one minute away yields zero. Omit past predictions
  rather than label an old estimate as a bus arriving now. Test the exact-time boundary.
- Missing/blank ETA means no usable prediction for that slot. Missing or unfamiliar
  non-ETA fields map to `UNKNOWN`, leaving a valid ETA visible. An unparseable,
  nonblank ETA is a source-format error (`502`), not fabricated arrival data.
- Proposed load enums: `SEATS_AVAILABLE`, `STANDING_AVAILABLE`, `LIMITED_STANDING`,
  `UNKNOWN`. Type enums: `SINGLE_DECK`, `DOUBLE_DECK`, `BENDY`, `UNKNOWN`.
  Prediction basis: `LOCATION`, `SCHEDULE`, `UNKNOWN`; preserve this distinction
  so the UI can label schedule-based estimates.
- `AVAILABLE` means at least one usable prediction. Otherwise return `200` with
  `status: "NO_ESTIMATES"` and `arrivals: []` for recognized empty/no-prediction
  source responses. This does not establish that the service has stopped running:
  missing data can have more than one cause. A malformed response is an error.
- Do not return old predictions after an upstream failure. Initially there is no
  live-response cache. Send `Cache-Control: no-store` on arrival responses so the
  browser/proxy does not accidentally cache them.

The API exposes whole-minute waits for display convenience; a later frontend
must use `estimatedArrival` to update the countdown between refreshes. Such a
countdown remains an estimate. Proposed visible-page refresh interval: 20 seconds;
background historical collection remains a separate future 60-second process.

## Validation and errors

Stop codes must contain exactly five ASCII digits. Service identifiers must be
1–10 ASCII letters/digits, normalized to uppercase, and URL-encoded by clients.
Check the known stop and stop/service association against the ready reference
catalog. A reference miss may be caused by outdated catalog data; do not infer
network-wide service withdrawal. Reject unsupported query parameters to catch
client mistakes early.

```json
{
  "code": "SOURCE_UNAVAILABLE",
  "message": "Arrival information is temporarily unavailable."
}
```

| HTTP status | BusPulse code | Meaning |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | Invalid query, stop-code format, or service identifier. |
| 404 | `STOP_NOT_FOUND` / `SERVICE_NOT_FOUND` | Not in the ready reference catalog for this stop. |
| 502 | `SOURCE_INVALID_RESPONSE` | Unexpected source status/payload, wrong stop identity, or invalid nonblank ETA. |
| 503 | `CATALOG_NOT_READY` | Reference snapshot is not usable yet. |
| 503 | `SOURCE_UNAVAILABLE` | Source credentials missing/rejected, rate limiting, or transient source outage. |
| 504 | `SOURCE_TIMEOUT` | Arrival source exceeded the configured response deadline. |

Do not relay raw upstream bodies, credentials, authorization headers, or exception
details to clients. Log a sanitized failure category/status server-side. An LTA
HTTP error is not evidence that a BusPulse stop/service is unknown.

## Review and acceptance

Before application code, agree on these three routes, response shapes, and the
in-memory reference strategy in [lta-flow.md](lta-flow.md). Implementation tests
should cover validation, partial/empty predictions, unknown enums, timestamp
conversion, rounding boundaries, malformed payloads, wrong-stop responses,
pagination, timeouts, and credential/rate-limit failures. Tests use local fixtures
and a controlled clock, without a real API key or network calls. A separate manual
smoke check can verify real LTA responses after the key is configured.

## Source boundary

Verified against LTA's [API User Guide v6.10, 1 Oct 2026](https://datamall.lta.gov.sg/content/dam/datamall/datasets/LTA_DataMall_API_User_Guide.pdf),
sections 1 and 2.1–2.4. BusPulse response names, validation policies, error mapping,
and lifecycle decisions are proposals, not upstream API guarantees.
