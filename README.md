# Smart Campus Sensor & Room Management API
**Student Name:** [Sewmini Senevirathna]  
**Student ID:** [20240079/w2149627]

---

---

## Project Overview
The **Smart Campus API** is a RESTful web service designed to manage university IoT infrastructure. It allows administrators to manage Rooms and Sensors, while tracking real-time environmental data through sensor readings. This project demonstrates high-level JAX-RS concepts, including HATEOAS for discovery, Sub-Resource Locators for nested data, and Custom Exception Mapping for robust error handling.

---

##  Build & Launch Instructions

### Prerequisites
* **Java JDK 17** (or higher)
* **Apache Maven**
* **Apache Tomcat 9.0**

### Step-by-Step Setup

1. **Clone the Repository:**
   ```bash
   git clone [INSERT_YOUR_GITHUB_LINK_HERE]

### **2. Build the Project**
In NetBeans, right-click the project in the **Projects** pane and select **Clean and Build**. This will trigger Maven to download all necessary dependencies and package the application into a `.war` file.

### **3. Launch the Server**
Right-click the project and select **Run**. Ensure that **Apache Tomcat 9.0** is selected as the target server. The IDE will deploy the WAR file and start the service.

### **4. Access the API**
Once the server is running, the API base URL will be:
`http://localhost:8080/smart-campus-api/api/v1/`


## Part 1: Service Architecture & Setup

### 1.1 Project & Application Configuration
The "Smart Campus" API is built using **Java 17** and **Maven**, leveraging **Jersey** as the JAX-RS implementation and **Apache Tomcat 9.0** as the servlet container. The application is configured using a subclass of `javax.ws.rs.core.Application`, which serves as the portable metadata source for the JAX-RS runtime.

**Code Reference:** The `AppConfig` class uses the `@ApplicationPath("/api/v1")` annotation to establish the base URI for all resources, ensuring versioning is baked into the API architecture from the start.

### **JAX-RS Lifecycle Analysis**
**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on synchronization.

**Answer:** By default, JAX-RS resource classes follow a **Request-scoped** lifecycle. This means that for every single incoming HTTP request, the JAX-RS runtime creates a brand-new instance of the resource class (e.g., `RoomResource`), and once the response is sent back to the client, that instance is destroyed and eligible for garbage collection.
**Architectural Impact & Data Management:** Because resource instances are temporary, any data stored in local instance variables would be lost between requests. To manage the university's campus data reliably, I implemented the `DataStorage` class using the **Singleton pattern** logic. By using `static` members and `ConcurrentHashMap`, the data persists in the server's memory regardless of how many resource instances are created.

To prevent data loss or "race conditions" (where two simultaneous requests try to modify the same room at the same millisecond), I chose `ConcurrentHashMap` over a standard `HashMap`. This ensures that the in-memory data structures are thread-safe, allowing for high-performance, concurrent access across the campus network.

---
### 1.2 HATEOAS & Discovery Endpoint
The root discovery endpoint at `GET /api/v1` implements **HATEOAS** (Hypermedia as the Engine of Application State).
**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** HATEOAS (Hypermedia as the Engine of Application State) is the final level of the **Richardson Maturity Model** for REST APIs. Providing hypermedia links within JSON responses makes the API self-descriptive.
**Benefits for Client Developers:**
1. **Discoverability:** Clients don't need a hardcoded list of URLs; they can follow the `_links` provided in the response.
2. **Resilience:** If the URI structure changes (e.g., changing `/rooms` to `/campus-rooms`), client code that follows links won't break.
3. **Self-Documentation:** It reduces the reliance on static PDF documentation by providing a "live" map of the API state.

---

## Part 2: Room Management

### 2.1 Room Resource Implementation
The `RoomResource` class manages the `/api/v1/rooms` path, providing endpoints to list all rooms, create new room entries, and fetch specific metadata. To ensure data persistence in a request-scoped environment, all operations interact with the centralized `DataStorage` maps.

### Data Retrieval Strategy: IDs vs. Full Objects
When returning the list of rooms, I chose to return **Full Room Objects**.
*Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:** * **Returning Only IDs:** This approach significantly minimizes **network bandwidth**, which is beneficial for mobile clients on limited data connections. However, it increases **client-side processing** and results in **"Chatty I/O."** If a client needs to display room names or capacities, they would be forced to make a separate API request for every single ID retrieved, leading to high latency and increased server load.
* **Returning Full Objects:** While this increases the initial payload size, it allows the client to render a comprehensive dashboard in a single round-trip. For a Smart Campus management system where facilities managers need immediate visibility, returning full objects is superior as it reduces total request latency and improves the overall user experience.

---

### 2.2 Room Deletion & Safety Logic
The API implements a `DELETE /{roomId}` endpoint to allow for room decommissioning. To maintain **referential integrity**, a business logic constraint is enforced: a room cannot be deleted if it still has active sensors assigned to it. Attempting to delete a room containing hardware triggers a `409 Conflict` custom error, preventing "orphaned sensors."

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, the `DELETE` operation in this implementation is **idempotent**.

**Justification:**
If a client sends the same `DELETE` request multiple times:
1. The first request deletes the room and returns `204 No Content`.
2. Subsequent requests will find the room missing and return `404 Not Found`.
   Even though the HTTP status code changes, the **final state of the server remains the same** (the room remains deleted). Therefore, the operation is idempotent as it has no additional effect after the first successful call.

### Business Logic: Deletion Safety
To maintain **referential integrity**, my API blocks the deletion of any room that contains sensors. This prevents "Orphaned Sensors" (sensors linked to a room ID that no longer exists). A `409 Conflict` error is returned to inform the user that the room must be cleared before decommissioning.

## Part 3: Sensor Operations & Filtering

### 3.1 Sensor Resource & Integrity
The `SensorResource` manages the registration and lifecycle of hardware devices across the campus. A critical feature of this resource is the enforcement of **Referential Integrity**: when a new sensor is registered via `POST`, the system validates that the `roomId` provided in the payload exists within the `DataStorage`. If the ID is missing, the API prevents the registration and returns a custom error.

#### **Technical Report: @Consumes Mismatch**
**Question:** Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** If a client attempts to send data as `application/xml` or `text/plain` to a method strictly annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime will automatically intercept and reject the request before it reaches the resource logic. 

**Technical Handling:** The client will receive an **HTTP 415 Unsupported Media Type** status code. JAX-RS handles this mismatch by inspecting the `Content-Type` header of the incoming HTTP request. If the header does not match the expected JSON media type, the request is blocked. This acts as a security and stability layer, ensuring the server never attempts to parse malformed or unexpected data formats into Java POJOs.

---

### 3.2 Filtered Retrieval & Search
The `GET /api/v1/sensors` endpoint has been enhanced to support dynamic discovery. Facilities managers can filter the vast array of campus hardware by using optional query parameters to narrow down their search results.

#### **Technical Report: Query Parameters vs. Path Parameters**
**Question:** Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** * **Path Parameters:** These are designed to identify a **specific, unique resource** (e.g., `/sensors/TEMP-001`). Using them for filtering (e.g., `/sensors/type/CO2`) creates a rigid, hierarchical URL structure that is difficult to scale.
* **Query Parameters:** These are the RESTful standard for **filtering, sorting, or searching** a collection (e.g., `?type=CO2`).

**Why Query Parameters are Superior:** Query parameters are **optional** and **combinable**. In a real-world campus scenario, a user might want to see sensors that are both a specific type AND a specific status (e.g., `?type=CO2&status=MAINTENANCE`). Achieving this with path parameters would require creating a "combinatorial explosion" of different URL paths for every possible filter combination, whereas query parameters handle multiple optional filters cleanly in a single endpoint.

---
## Part 4: Deep Nesting with Sub-Resources

### 4.1 The Sub-Resource Locator Pattern
To manage the historical data generated by thousands of sensors, I implemented the **Sub-Resource Locator** pattern. In the `SensorResource` class, the path `{sensorId}/readings` does not handle the request directly; instead, it returns an instance of `SensorReadingResource`, delegating the responsibility for nested operations.

#### **Technical Report: Architectural Benefits**
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

**Answer:** Using the Sub-Resource Locator pattern offers two primary architectural advantages:
1. **Separation of Concerns:** It allows for a clean logical split. The `SensorResource` focuses strictly on hardware metadata (ID, type, room location), while the `SensorReadingResource` is dedicated to the complex, data-heavy logic of managing historical time-series logs.
2. **Prevention of "God Classes":** In a massive campus API, defining every nested path (e.g., `/sensors/{id}/readings/{rid}`) in a single class would result in a "God Class" with hundreds of lines of code. This makes the API difficult to test and maintain. Delegating to sub-resources keeps individual classes small, modular, and focused on a single responsibility, which significantly improves code readability and debugging.

---

### 4.2 Historical Data Management & Consistency
The `SensorReadingResource` supports `GET` to retrieve a sensor's history and `POST` to append new measurement events. 

#### **Technical Report: Data Consistency & Side Effects**
**Requirement:** A successful POST to a reading must trigger an update to the `currentValue` field on the corresponding parent Sensor object.

**Implementation:** My implementation ensures **Data Consistency** across the API by triggering a state-change "side effect." Whenever a new reading is successfully recorded via `POST`, the logic automatically locates the parent `Sensor` object in `DataStorage` and updates its `currentValue` field. This ensures that a Facilities Manager calling `GET /api/v1/sensors/{id}` will always see the most recent real-time measurement without having to query the entire historical log manually.

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### 5.1 & 5.2 Resource Conflict & Dependency Validation
To ensure the API is "leak-proof," I implemented custom Exception Mappers. This prevents the server from ever returning a raw Java stack trace, providing instead a meaningful JSON error response and the correct HTTP status code.

#### **Technical Report: Semantic Accuracy (422 vs 404)**
**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** A `404 Not Found` typically suggests that the **URL endpoint** itself does not exist. However, in the case of a "Missing Room Reference," the URL is correct, and the JSON syntax is valid. The error is logical: the data provided (the `roomId`) does not exist in the system. **HTTP 422 Unprocessable Entity** (or 400) is more accurate because it tells the client, "I understood your request, but I cannot process the data inside of it."

---

### 5.3 & 5.4 State Constraints & The Global Safety Net
I implemented a "Catch-All" `ExceptionMapper<Throwable>` to intercept unexpected runtime errors (like `NullPointerException`), ensuring the client only sees a clean `500 Internal Server Error` message.

#### **Technical Report: Cybersecurity & Stack Traces**
**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather?

**Answer:** Exposing stack traces is a major security risk (Security Misconfiguration). An attacker can gather:
1. **Infrastructure Details:** The versions of the JAX-RS implementation (Jersey), the Server (Tomcat), and the JDK.
2. **Implementation Logic:** Internal class names, package structures, and method names, which reveal how the code is organized.
3. **Exploitation Vectors:** If an attacker knows specific library versions, they can look up known vulnerabilities (CVEs) to launch a targeted attack. By mapping all errors to generic JSON responses, we hide the internal workings of the "Smart Campus" backend.

---

### 5.5 API Request & Response Logging Filters
I implemented a custom filter class implementing `ContainerRequestFilter` and `ContainerResponseFilter` to provide full observability of the system without cluttering the business logic.

#### **Technical Report: Advantages of JAX-RS Filters**
**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** This approach follows the principle of **Separation of Concerns**. 
1. **Consistency:** A filter is a "Cross-Cutting Concern" that applies to every single request automatically. Manual logging is prone to human error; a developer might forget to log one specific endpoint.
2. **Clean Code:** It keeps the Resource classes clean. Instead of every method having 2-3 lines of logging code, the logging is centralized in one place.
3. **Maintainability:** If the logging format needs to change (e.g., adding a timestamp or an IP address), it only needs to be updated in one file rather than hundreds of methods.

---

##  Sample cURL Commands
Test the API's core functionality using these commands in your terminal:

```bash
# 1. API Discovery (HATEOAS)
curl -X GET http://localhost:8080/smart-campus-api/api/v1/

# 2. Register a New Room
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id": "R101", "name": "Computing Lab"}'

# 3. Register a Sensor to a Room
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id": "S1", "roomId": "R101", "type": "CO2", "status": "ACTIVE"}'

# 4. Post a New Sensor Reading
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/S1/readings \
-H "Content-Type: application/json" \
-d '{"value": 450.5}'

# 5. Filter Sensors by Type (Query Param)
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2"