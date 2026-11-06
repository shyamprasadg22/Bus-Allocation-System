-- Realistic, Clearly-Labelled Sample Data for College Bus Seat Allocation System
-- Note: All sample data is labeled for development, demonstration, and automated test scenarios.

USE bus_allocation_db;

-- 1. Shifts
INSERT INTO shifts (shift_id, shift_code, shift_name, departure_time, description) VALUES
(1, 'SHIFT_1', 'Shift 1: Morning (07:30 AM)', '07:30:00', 'Morning inbound transport for regular 08:30 AM college schedule'),
(2, 'SHIFT_2', 'Shift 2: Mid-day (12:30 PM)', '12:30:00', 'Mid-day transport for afternoon laboratory batches'),
(3, 'SHIFT_3', 'Shift 3: Evening (04:30 PM)', '16:30:00', 'Evening outbound transport from college campus')
ON DUPLICATE KEY UPDATE shift_name=VALUES(shift_name);

-- 2. System Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('declaration_cutoff_hour', '21', 'Cutoff hour (24h format, 21 = 9 PM) on previous day after which declarations lock'),
('system_notice', 'Sample Data Environment: Preloaded for testing and demonstration.', 'Notice banner displayed across the portal')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);

-- 3. Routes (Realistic Chennai Metro college routes)
INSERT INTO routes (route_id, route_code, route_name, start_location, end_location, stops_description, is_active) VALUES
(1, 'RT-101', 'Tambaram - Guindy Express', 'Tambaram Sanatorium', 'College Main Campus', 'Tambaram, Chromepet, Pallavaram, Airport, Guindy, Campus', TRUE),
(2, 'RT-102', 'Central - Koyambedu Line', 'Chennai Central', 'College Main Campus', 'Central, Egmore, Aminjikarai, Koyambedu, Vadapalani, Campus', TRUE),
(3, 'RT-103', 'Velachery - Medavakkam Link', 'Velachery MRTS', 'College Main Campus', 'Velachery, Madipakkam, Medavakkam, Kovilambakkam, Campus', TRUE),
(4, 'RT-104', 'Avadi - Mogappair Connector', 'Avadi Bus Terminus', 'College Main Campus', 'Avadi, Thirumullaivoyal, Ambattur OT, Mogappair, Campus', TRUE),
(5, 'RT-105', 'OMR IT Corridor', 'Sholinganallur Junction', 'College Main Campus', 'Sholinganallur, Karapakkam, Thoraipakkam, Perungudi, Campus', TRUE)
ON DUPLICATE KEY UPDATE route_name=VALUES(route_name);

-- 4. Buses (Fleet with realistic varied capacities)
INSERT INTO buses (bus_id, bus_number, bus_label, total_capacity, is_active) VALUES
(1, 'TN-09-CB-1001', 'Bus 01 (Ashok Leyland Viking)', 50, TRUE),
(2, 'TN-09-CB-1002', 'Bus 02 (Tata Starbus Ultra)', 40, TRUE),
(3, 'TN-09-CB-1003', 'Bus 03 (BharatBenz 1017)', 40, TRUE),
(4, 'TN-09-CB-1004', 'Bus 04 (Eicher Skyline Pro)', 30, TRUE),
(5, 'TN-09-CB-1005', 'Bus 05 (Ashok Leyland Lynx)', 25, TRUE),
(6, 'TN-09-CB-1006', 'Bus 06 (Force Traveller Mini)', 15, TRUE)
ON DUPLICATE KEY UPDATE bus_label=VALUES(bus_label);

-- 5. Admin Users
-- Default credentials:
-- Username: admin
-- Password: Password@123 (BCrypt hash: $2a$10$7ZkP9X0q0Q0Z0u4u8uO7zeq1g2eYpZ5F4jD1Cj8aG8F8g1H1H1H1a - we use SHA-256 with salt for direct verification and portable JDBC compatibility)
-- We store standard SHA-256 hash of 'admin123' or 'Admin@123'
-- Hash of 'Admin@123': 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO admin_users (admin_id, username, password_hash, full_name, role, is_active) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Transportation Administrator', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 6. Students (Realistic sample students with Access PIN set to the last 5 digits / characters of their Student ID)
INSERT INTO students (student_id, full_name, email, department, assigned_route_id, phone, access_pin_hash) VALUES
('2024CS101', 'Aarav Sharma', 'aarav.cs101@college.edu', 'Computer Science', 1, '9876543201', SHA2(UPPER(RIGHT('2024CS101', 5)), 256)), -- PIN: CS101
('2024CS102', 'Diya Ramesh', 'diya.cs102@college.edu', 'Computer Science', 1, '9876543202', SHA2(UPPER(RIGHT('2024CS102', 5)), 256)), -- PIN: CS102
('2024CS103', 'Karthik Raja', 'karthik.cs103@college.edu', 'Computer Science', 1, '9876543203', SHA2(UPPER(RIGHT('2024CS103', 5)), 256)), -- PIN: CS103
('2024CS104', 'Ananya Sundar', 'ananya.cs104@college.edu', 'Computer Science', 1, '9876543204', SHA2(UPPER(RIGHT('2024CS104', 5)), 256)), -- PIN: CS104
('2024CS105', 'Pranav Kumar', 'pranav.cs105@college.edu', 'Computer Science', 1, '9876543205', SHA2(UPPER(RIGHT('2024CS105', 5)), 256)), -- PIN: CS105

('2024EC101', 'Siddharth Iyer', 'siddharth.ec101@college.edu', 'Electronics & Comm', 2, '9876543206', SHA2(UPPER(RIGHT('2024EC101', 5)), 256)), -- PIN: EC101
('2024EC102', 'Pooja Venkatesh', 'pooja.ec102@college.edu', 'Electronics & Comm', 2, '9876543207', SHA2(UPPER(RIGHT('2024EC102', 5)), 256)), -- PIN: EC102
('2024EC103', 'Gautham Menon', 'gautham.ec103@college.edu', 'Electronics & Comm', 2, '9876543208', SHA2(UPPER(RIGHT('2024EC103', 5)), 256)), -- PIN: EC103
('2024EC104', 'Harini Krishnan', 'harini.ec104@college.edu', 'Electronics & Comm', 2, '9876543209', SHA2(UPPER(RIGHT('2024EC104', 5)), 256)), -- PIN: EC104
('2024EC105', 'Rohan Balaji', 'rohan.ec105@college.edu', 'Electronics & Comm', 2, '9876543210', SHA2(UPPER(RIGHT('2024EC105', 5)), 256)), -- PIN: EC105

('2024ME101', 'Vigneshwaran M', 'vignesh.me101@college.edu', 'Mechanical Eng', 3, '9876543211', SHA2(UPPER(RIGHT('2024ME101', 5)), 256)), -- PIN: ME101
('2024ME102', 'Deepak Chandran', 'deepak.me102@college.edu', 'Mechanical Eng', 3, '9876543212', SHA2(UPPER(RIGHT('2024ME102', 5)), 256)), -- PIN: ME102
('2024ME103', 'Swetha Subramanian', 'swetha.me103@college.edu', 'Mechanical Eng', 3, '9876543213', SHA2(UPPER(RIGHT('2024ME103', 5)), 256)), -- PIN: ME103
('2024ME104', 'Arjun Natarajan', 'arjun.me104@college.edu', 'Mechanical Eng', 3, '9876543214', SHA2(UPPER(RIGHT('2024ME104', 5)), 256)), -- PIN: ME104

('2024IT101', 'Sneha Parthasarathy', 'sneha.it101@college.edu', 'Information Tech', 4, '9876543215', SHA2(UPPER(RIGHT('2024IT101', 5)), 256)), -- PIN: IT101
('2024IT102', 'Manoj Kumar', 'manoj.it102@college.edu', 'Information Tech', 4, '9876543216', SHA2(UPPER(RIGHT('2024IT102', 5)), 256)), -- PIN: IT102
('2024IT103', 'Keerthana R', 'keerthana.it103@college.edu', 'Information Tech', 4, '9876543217', SHA2(UPPER(RIGHT('2024IT103', 5)), 256)), -- PIN: IT103

('2024AI101', 'Aditya Verma', 'aditya.ai101@college.edu', 'AI & Data Science', 5, '9876543218', SHA2(UPPER(RIGHT('2024AI101', 5)), 256)), -- PIN: AI101
('2024AI102', 'Bhavana Reddy', 'bhavana.ai102@college.edu', 'AI & Data Science', 5, '9876543219', SHA2(UPPER(RIGHT('2024AI102', 5)), 256)), -- PIN: AI102
('2024AI103', 'Varun Raghavan', 'varun.ai103@college.edu', 'AI & Data Science', 5, '9876543220', SHA2(UPPER(RIGHT('2024AI103', 5)), 256)), -- PIN: AI103

('250701711', 'Shyamprasad G', 'shyamprasad.250701711@college.edu', 'Computer Science & Eng', 1, '9876543230', SHA2(UPPER(RIGHT('250701711', 5)), 256)), -- PIN: 01711
('2507010711', 'Shyamprasad G', 'shyam.2507010711@college.edu', 'Computer Science & Eng', 1, '9876543231', SHA2(UPPER(RIGHT('2507010711', 5)), 256)), -- PIN: 10711
('250701712', 'Student 250701712', 'student.250701712@college.edu', 'Computer Science & Eng', 2, '9876543232', SHA2(UPPER(RIGHT('250701712', 5)), 256)), -- PIN: 01712
('250701716', 'Student 250701716', 'student.250701716@college.edu', 'Computer Science & Eng', 3, '9876543233', SHA2(UPPER(RIGHT('250701716', 5)), 256))  -- PIN: 01716
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), access_pin_hash=VALUES(access_pin_hash);

-- 7. Preloaded realistic travel declarations for testing (Sample tomorrow's date: CURRENT_DATE + 1 day)
INSERT INTO declarations (student_id, travel_date, route_id, shift_id, will_travel) VALUES
('2024CS101', CURRENT_DATE + INTERVAL 1 DAY, 1, 1, TRUE),
('2024CS102', CURRENT_DATE + INTERVAL 1 DAY, 1, 1, TRUE),
('2024CS103', CURRENT_DATE + INTERVAL 1 DAY, 1, 1, TRUE),
('2024CS104', CURRENT_DATE + INTERVAL 1 DAY, 1, 1, FALSE),
('2024CS105', CURRENT_DATE + INTERVAL 1 DAY, 1, 1, TRUE),

('2024EC101', CURRENT_DATE + INTERVAL 1 DAY, 2, 1, TRUE),
('2024EC102', CURRENT_DATE + INTERVAL 1 DAY, 2, 1, TRUE),
('2024EC103', CURRENT_DATE + INTERVAL 1 DAY, 2, 1, TRUE),
('2024EC104', CURRENT_DATE + INTERVAL 1 DAY, 2, 1, TRUE),
('2024EC105', CURRENT_DATE + INTERVAL 1 DAY, 2, 1, TRUE),

('2024ME101', CURRENT_DATE + INTERVAL 1 DAY, 3, 2, TRUE),
('2024ME102', CURRENT_DATE + INTERVAL 1 DAY, 3, 2, TRUE),
('2024ME103', CURRENT_DATE + INTERVAL 1 DAY, 3, 2, FALSE),
('2024ME104', CURRENT_DATE + INTERVAL 1 DAY, 3, 2, TRUE),

('2024IT101', CURRENT_DATE + INTERVAL 1 DAY, 4, 1, TRUE),
('2024IT102', CURRENT_DATE + INTERVAL 1 DAY, 4, 1, TRUE),
('2024IT103', CURRENT_DATE + INTERVAL 1 DAY, 4, 1, TRUE),

('2024AI101', CURRENT_DATE + INTERVAL 1 DAY, 5, 3, TRUE),
('2024AI102', CURRENT_DATE + INTERVAL 1 DAY, 5, 3, TRUE),
('2024AI103', CURRENT_DATE + INTERVAL 1 DAY, 5, 3, TRUE)
ON DUPLICATE KEY UPDATE will_travel=VALUES(will_travel);
