-- College Bus Seat Allocation System Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS bus_allocation_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE bus_allocation_db;

-- Drop dependent tables in reverse order for clean repeatability
DROP TABLE IF EXISTS student_bus_assignments;
DROP TABLE IF EXISTS allocation_buses;
DROP TABLE IF EXISTS allocations;
DROP TABLE IF EXISTS declarations;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS buses;
DROP TABLE IF EXISTS shifts;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS admin_users;

-- 1. Routes Table
CREATE TABLE routes (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    route_code VARCHAR(20) NOT NULL UNIQUE,
    route_name VARCHAR(100) NOT NULL,
    start_location VARCHAR(100) NOT NULL,
    end_location VARCHAR(100) NOT NULL DEFAULT 'College Main Campus',
    stops_description TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Shifts Table (3 standard college shifts)
CREATE TABLE shifts (
    shift_id INT PRIMARY KEY,
    shift_code VARCHAR(20) NOT NULL UNIQUE,
    shift_name VARCHAR(60) NOT NULL,
    departure_time TIME NOT NULL,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- 3. Buses Table
CREATE TABLE buses (
    bus_id INT AUTO_INCREMENT PRIMARY KEY,
    bus_number VARCHAR(30) NOT NULL UNIQUE,
    bus_label VARCHAR(60) NOT NULL,
    total_capacity INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bus_capacity CHECK (total_capacity > 0)
) ENGINE=InnoDB;

-- 4. Students Table
CREATE TABLE students (
    student_id VARCHAR(30) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    assigned_route_id INT NOT NULL,
    phone VARCHAR(20),
    access_pin_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_route FOREIGN KEY (assigned_route_id) 
        REFERENCES routes (route_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 5. Declarations Table
CREATE TABLE declarations (
    declaration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(30) NOT NULL,
    travel_date DATE NOT NULL,
    route_id INT NOT NULL,
    shift_id INT NOT NULL,
    will_travel BOOLEAN NOT NULL,
    declared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_travel_date UNIQUE (student_id, travel_date),
    CONSTRAINT fk_decl_student FOREIGN KEY (student_id) 
        REFERENCES students (student_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_decl_route FOREIGN KEY (route_id) 
        REFERENCES routes (route_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_decl_shift FOREIGN KEY (shift_id) 
        REFERENCES shifts (shift_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_decl_query ON declarations (travel_date, route_id, shift_id, will_travel);

-- 6. System Settings Table
CREATE TABLE system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. Allocations Table (Run summary per date, shift, route)
CREATE TABLE allocations (
    allocation_id INT AUTO_INCREMENT PRIMARY KEY,
    travel_date DATE NOT NULL,
    shift_id INT NOT NULL,
    route_id INT NOT NULL,
    total_passengers INT NOT NULL DEFAULT 0,
    total_allocated INT NOT NULL DEFAULT 0,
    total_capacity INT NOT NULL DEFAULT 0,
    unallocated_count INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL, -- 'COMPLETED', 'INSUFFICIENT_CAPACITY', 'NO_DEMAND'
    allocated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_alloc_date_shift_route UNIQUE (travel_date, shift_id, route_id),
    CONSTRAINT fk_alloc_shift FOREIGN KEY (shift_id) 
        REFERENCES shifts (shift_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_route FOREIGN KEY (route_id) 
        REFERENCES routes (route_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 8. Allocation Buses Table (Buses assigned to an allocation)
CREATE TABLE allocation_buses (
    allocation_bus_id INT AUTO_INCREMENT PRIMARY KEY,
    allocation_id INT NOT NULL,
    bus_id INT NOT NULL,
    assigned_passengers INT NOT NULL DEFAULT 0,
    bus_capacity INT NOT NULL,
    unused_seats INT NOT NULL,
    CONSTRAINT uq_alloc_bus UNIQUE (allocation_id, bus_id),
    CONSTRAINT fk_ab_alloc FOREIGN KEY (allocation_id) 
        REFERENCES allocations (allocation_id) ON DELETE CASCADE,
    CONSTRAINT fk_ab_bus FOREIGN KEY (bus_id) 
        REFERENCES buses (bus_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 9. Student Bus Assignments (Individual student assignment with seat number)
CREATE TABLE student_bus_assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    allocation_id INT NOT NULL,
    student_id VARCHAR(30) NOT NULL,
    bus_id INT NOT NULL,
    seat_number INT NOT NULL,
    travel_date DATE NOT NULL,
    shift_id INT NOT NULL,
    route_id INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_date_shift UNIQUE (student_id, travel_date, shift_id),
    CONSTRAINT fk_sba_alloc FOREIGN KEY (allocation_id) 
        REFERENCES allocations (allocation_id) ON DELETE CASCADE,
    CONSTRAINT fk_sba_student FOREIGN KEY (student_id) 
        REFERENCES students (student_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_sba_bus FOREIGN KEY (bus_id) 
        REFERENCES buses (bus_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sba_shift FOREIGN KEY (shift_id) 
        REFERENCES shifts (shift_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sba_route FOREIGN KEY (route_id) 
        REFERENCES routes (route_id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_student_assignment_lookup ON student_bus_assignments (student_id, travel_date);

-- 10. Admin Users Table
CREATE TABLE admin_users (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL
) ENGINE=InnoDB;
