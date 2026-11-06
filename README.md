# College Bus Seat Allocation System

An institutional transportation management and algorithmic bus seat allocation system built with **HTML5, Vanilla CSS3, Vanilla JavaScript, Java (Spring Boot 3), Spring JDBC, and MySQL 8.0**.

---

## 1. Project Overview & Architectural Design

The College Bus Seat Allocation System manages the daily transit workflow for college commuter students and transport logistics administrators:
- **Students** declare daily travel intentions before an evening cutoff, select their route and shift, and look up their allocated bus and seat number.
- **Administrators** inspect real-time declared passenger counts, execute a greedy capacity-based allocation algorithm that schedules available buses without overbooking, prevents vehicle assignment conflicts, and publishes individual student-to-bus manifests.

```
                          [ Client Layer ]
              HTML5 / Vanilla CSS3 / Vanilla JavaScript
           (index.html, student.html, admin.html, privacy, terms)
                                 │
                     REST API / HTTP JSON
                                 ▼
                     [ Spring Boot Backend ]
  ┌─────────────────────────────────────────────────────────────┐
  │  AdminAuthInterceptor (Token & Role Verification)            │
  ├─────────────────────────────────────────────────────────────┤
  │  Controllers: StudentController, AdminController,           │
  │               AllocationController                          │
  ├─────────────────────────────────────────────────────────────┤
  │  Services: StudentService, AllocationService, AdminService, │
  │            AuthService                                      │
  ├─────────────────────────────────────────────────────────────┤
  │  Repositories: StudentRepository, DeclarationRepository,   │
  │                BusRepository, RouteRepository,              │
  │                AllocationRepository, AdminRepository        │
  │                (Spring JdbcTemplate + Parameterized SQL)     │
  └─────────────────────────────────────────────────────────────┘
                                 │
                     JDBC Connection Pool (HikariCP)
                                 ▼
                          [ MySQL 8.0 ]
   (routes, buses, shifts, students, declarations, allocations,
    allocation_buses, student_bus_assignments, admin_users)
```

---

## 2. Key Modules & Functional Requirements

### 1. Student Module
- **Daily Declarations**: Students enter their alphanumeric Student ID (e.g. `2024CS101`), verify their identity with a 4-digit Access PIN (`1234`), select their route and one of three shifts, and indicate whether they will travel.
- **Deadline Enforcement**: Declarations for the next academic day close at **09:00 PM (21:00)** the previous evening. Same-day modifications close 60 minutes prior to departure.
- **Duplicate Prevention**: Backed by a relational `UNIQUE KEY (student_id, travel_date)`. Submitting a new choice updates the existing record cleanly without creating duplicate rows.
- **Seat & Bus Verification**: Students look up their confirmed vehicle label, bus registration number, and individual seat assignment (e.g., `Bus 01 (TN-09-CB-1001), Seat #1`).
- **Declaration History**: Inspects past declarations with audit timestamps.

### 2. Admin Module
- **Role-Based Security**: Admin endpoints are protected by `AdminAuthInterceptor` requiring a valid `X-Admin-Token` or Bearer token generated upon login (`admin` / `admin123`).
- **Grouped Declarations Manifest**: Real-time view of student declarations grouped by date, shift, and route.
- **Fleet Management**: CRUD operations to add new buses, adjust seating capacities, and toggle operational status.
- **Route Network**: CRUD operations for route codes, origin stops, intermediate pick-up itineraries, and active flags.
- **Genuine Live Statistics**: System metrics page reports real database counts (active fleet capacity, route counts, active buses) with zero fabricated numbers.

### 3. Allocation Algorithm & Scheduling Rules
- **Passenger Aggregation**: Counts confirmed passengers (`will_travel = true`) for each route and shift on the selected travel date.
- **Greedy Capacity Matching**: Available buses are sorted by capacity in descending order and assigned to routes until all passengers are seated or the fleet is exhausted.
- **Conflict Prevention**: Buses assigned to a route for a given date and shift are marked unavailable for other routes in that same shift, strictly preventing overlapping trips.
- **Shortfall Detection**: If total available bus capacity is less than demand, the system records `INSUFFICIENT_CAPACITY`, logs the exact shortfall count, and raises an administrative alert banner.
- **Repeatable Execution**: Re-running an allocation for a date and shift automatically purges previous assignments for that run, re-computes assignments, and writes new allocations without primary or foreign key conflicts.
- **Individual Seat Manifest**: Each confirmed commuter is assigned a specific seat number (`1..Capacity`) on their allocated vehicle.

---

## 3. Technology Stack

- **Frontend**: HTML5, Vanilla CSS3 (Restrained navy, white, and neutral gray palette, responsive, accessible, zero purple gradients/pill buttons/fake metrics), Vanilla JavaScript (ES6+ `fetch` with error handling).
- **Backend**: Java 17 / 21, Spring Boot 3.3.4, Spring Web, Spring JDBC (`JdbcTemplate`).
- **Database**: MySQL 8.0 with InnoDB engine, primary keys, foreign keys with cascading cleanup, unique constraints, and indexes.
- **Configuration**: Environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`) loaded seamlessly via `.env` file and Spring `EnvironmentPostProcessor`.

---

## 4. Database Schema Structure

The database `bus_allocation_db` comprises 10 relational tables:
1. `routes` (`route_id`, `route_code`, `route_name`, `start_location`, `end_location`, `stops_description`, `is_active`)
2. `shifts` (`shift_id`, `shift_code`, `shift_name`, `departure_time`, `description`)
3. `buses` (`bus_id`, `bus_number`, `bus_label`, `total_capacity`, `is_active`)
4. `students` (`student_id`, `full_name`, `email`, `department`, `assigned_route_id`, `access_pin_hash`)
5. `declarations` (`declaration_id`, `student_id`, `travel_date`, `route_id`, `shift_id`, `will_travel`, `declared_at`, `updated_at`, `UNIQUE(student_id, travel_date)`)
6. `system_settings` (`setting_key`, `setting_value`, `description`)
7. `allocations` (`allocation_id`, `travel_date`, `shift_id`, `route_id`, `total_passengers`, `total_allocated`, `total_capacity`, `unallocated_count`, `status`, `UNIQUE(travel_date, shift_id, route_id)`)
8. `allocation_buses` (`allocation_bus_id`, `allocation_id`, `bus_id`, `assigned_passengers`, `bus_capacity`, `unused_seats`)
9. `student_bus_assignments` (`assignment_id`, `allocation_id`, `student_id`, `bus_id`, `seat_number`, `travel_date`, `shift_id`, `route_id`, `UNIQUE(student_id, travel_date, shift_id)`)
10. `admin_users` (`admin_id`, `username`, `password_hash`, `full_name`, `role`, `is_active`)

---

## 5. Local Setup & Execution Instructions

### Prerequisites
- Java 17 or Java 21 JDK installed
- MySQL Server 8.0 running on `localhost:3306`

### Step 1: Database Initialization
In PowerShell or MySQL Shell, execute the provided SQL scripts:
```powershell
# Using MySQL CLI
Get-Content schema.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
Get-Content data.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

### Step 2: Configure Environment Variables
Update `.env` in the project root with your MySQL credentials:
```properties
DB_URL=jdbc:mysql://localhost:3306/bus_allocation_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
SERVER_PORT=8080
```

### Step 3: Run the Test Suite
Verify that all unit and database integration tests pass:
```powershell
.\mvnw.cmd test
```
*Output: Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 (BUILD SUCCESS)*

### Step 4: Start the Server
Start the application using Maven or the pre-built JAR:
```powershell
# Option A: Run directly via Maven
.\mvnw.cmd spring-boot:run

# Option B: Run the standalone packaged JAR
java -jar target/bus-allocation-system-1.0.0.jar
```

### Step 5: Access the Web Portal
Open your web browser and navigate to:
- **Home & Schedule Gateway**: `http://localhost:8080/index.html`
- **Student Travel Declaration & Seat Check**: `http://localhost:8080/student.html`
- **Administrative Allocation Console**: `http://localhost:8080/admin.html`
- **Privacy Policy**: `http://localhost:8080/privacy.html`
- **Terms & Conditions**: `http://localhost:8080/terms.html`

### Default Demonstration Credentials
- **Student Portal**:
  - Student IDs: `2024CS101`, `2024CS102`, `2024EC101`, `2024ME101`
  - Access PIN: `1234`
- **Administrator Portal**:
  - Username: `admin`
  - Password: `admin123`

---

## 6. Verification and Test Results Summary

| Test Area | Scenario | Method | Result |
| :--- | :--- | :--- | :--- |
| **Empty Demand** | 0 confirmed passengers on route | Unit Test (`AllocationAlgorithmTest`) | Status: `NO_DEMAND`, 0 buses assigned (Passed) |
| **Normal Capacity** | 35 passengers with 50-cap bus | Unit Test (`AllocationAlgorithmTest`) | Status: `COMPLETED`, 35/35 allocated, 15 unused seats (Passed) |
| **Insufficient Capacity**| 60 passengers with 40-cap bus | Unit Test (`AllocationAlgorithmTest`) | Status: `INSUFFICIENT_CAPACITY`, shortfall = 20 (Passed) |
| **Multiple Routes** | Multiple routes in single shift | Unit Test (`AllocationAlgorithmTest`) | Independent route allocation verified (Passed) |
| **Duplicate Prevention** | Re-submitting declaration on same date | Database Integration Test (`DuplicatePreventionTest`) | Updated existing record without duplicate row (Passed) |
| **PIN Validation** | Wrong PIN submitted by student | Unit Test (`DeclarationValidationTest`) | HTTP 401 Unauthorized (Passed) |
| **Past Date Rejection** | Declaring travel for past date | Unit Test (`DeclarationValidationTest`) | HTTP 400 Bad Request (Passed) |
| **Repeatable Allocation**| Executing allocation multiple times | REST API Verification | Atomic clean replace without duplicate conflicts (Passed) |
| **Overlapping Trips** | Scheduling fleet across same shift | REST API Verification | Non-overlapping bus assignments enforced (Passed) |
| **Seat Manifest** | Looking up assigned seats | REST API & Student UI | Specific bus & seat numbers assigned (`Seat #1..N`) (Passed) |
