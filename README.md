# Smart Campus Sensor & Room Management API
**Student Name:** [Sewmini Senevirathna]  
**Student ID:** [20240079/w2149627]

---

## Part 1: Service Architecture & Setup

### JAX-RS Lifecycle Analysis
In this implementation, the JAX-RS Resource classes (like `RoomResource`) follow the **Request-scoped** lifecycle by default. This means the JAX-RS runtime creates a new instance of the resource class for every single incoming HTTP request.

**Architectural Impact:**
Because instances are destroyed after a request is completed, we cannot store data as local variables within the Resource classes. To prevent data loss, I implemented a **Singleton pattern** for the `DataStorage` class. This ensures that our `ConcurrentHashMap` structures persist in memory across multiple requests.

### HATEOAS & Discovery Endpoint
The root discovery endpoint at `GET /api/v1` implements **HATEOAS** (Hypermedia as the Engine of Application State).

**Benefits for Client Developers:**
1. **Discoverability:** Clients don't need a hardcoded list of URLs; they can follow the `_links` provided in the response.
2. **Resilience:** If the URI structure changes (e.g., changing `/rooms` to `/campus-rooms`), client code that follows links won't break.
3. **Self-Documentation:** It reduces the reliance on static PDF documentation by providing a "live" map of the API state.

---

## Part 2: Room Management

### Data Retrieval Strategy: IDs vs. Full Objects
When returning the list of rooms, I chose to return **Full Room Objects**.

**Comparison:**
- **Only IDs:** Reduces network bandwidth significantly but forces the client to perform "Chatty I/O" (making a separate request for every single room's details).
- **Full Objects:** Increases the initial payload size but improves performance by providing all necessary data in a single round-trip. For a campus management dashboard, full objects are superior as they reduce the load on the server's request handling thread.

### DELETE Operation & Idempotency
The `DELETE /api/v1/rooms/{roomId}` operation is **Idempotent**.

**Justification:**
If a client sends the same `DELETE` request multiple times:
1. The first request deletes the room and returns `204 No Content`.
2. Subsequent requests will find the room missing and return `404 Not Found`.
   Even though the HTTP status code changes, the **final state of the server remains the same** (the room remains deleted). Therefore, the operation is idempotent as it has no additional effect after the first successful call.

### Business Logic: Deletion Safety
To maintain **referential integrity**, my API blocks the deletion of any room that contains sensors. This prevents "Orphaned Sensors" (sensors linked to a room ID that no longer exists). A `409 Conflict` error is returned to inform the user that the room must be cleared before decommissioning.

## Part 3: Sensor Operations & Filtering

### Technical Consequence of @Consumes mismatch
If a client sends data as `text/plain` or `application/xml` while the method is restricted to `application/json`, JAX-RS will automatically block the request and return a **415 Unsupported Media Type** status code. This ensures the API only processes data formats it can safely parse into Java objects.

### Filtering: Query Parameters vs. Path Parameters
I implemented sensor filtering using `@QueryParam` (e.g., `/sensors?type=CO2`) rather than Path Parameters.
**Justification:**
Path parameters are used to identify a **specific resource** (e.g., `/sensors/101`). Query parameters are the standard RESTful way to **filter or sort a collection**. Using query parameters makes the API more flexible, as multiple filters (like type and status) can be combined easily without creating complex URL structures.