USE hotel_management_db;

-- 1.1 Insert Departments
INSERT INTO Department (department_id, name) VALUES
(1, 'Front Desk'),
(2, 'Housekeeping'),
(3, 'Food & Beverage'),
(4, 'Maintenance'),
(5, 'Management');

-- 1.2 Insert Staff Members
INSERT INTO Staff (staff_id, department_id, name, role, phone_no) VALUES
(101, 1, 'Amit Sharma', 'Reception Manager', '9876543210'),
(102, 1, 'Priya Verma', 'Front Desk Executive', '9876543211'),
(103, 2, 'Ramesh Kumar', 'Housekeeping Supervisor', '9876543212'),
(104, 3, 'Chef Sanjeev Roy', 'Head Chef', '9876543213'),
(105, 4, 'Vikas Patel', 'Chief Electrician', '9876543214'),
(106, 5, 'Ananya Sen', 'General Manager', '9876543215');

-- 1.3 Insert Room Types
INSERT INTO Room_Type (room_type_id, price, capacity) VALUES
(1, 2500.00, 1), -- Single Standard
(2, 4500.00, 2), -- Double Deluxe
(3, 7500.00, 3), -- Triple Executive
(4, 12000.00, 4); -- Presidential Family Suite

-- 1.4 Insert Rooms
INSERT INTO Room (room_id, room_type_id, room_no, floor) VALUES
(201, 1, '101', 1),
(202, 1, '102', 1),
(203, 2, '201', 2),
(204, 2, '202', 2),
(205, 3, '301', 3),
(206, 4, '401', 4);

-- 1.5 Insert Services Catalog
INSERT INTO Service (service_id, service_name, cost) VALUES
(1, 'Ayurvedic Spa & Massage', 1800.00),
(2, 'Airport Shuttle Pick & Drop', 1200.00),
(3, 'Express Laundry Service', 450.00),
(4, 'Swimming Pool & Cabana Pass', 600.00),
(5, 'Buffet Breakfast & Dinner', 1500.00);

-- 1.6 Insert Customers
INSERT INTO Customer (customer_id, name, contact_no, email) VALUES
(1, 'Rahul Gupta', '9811122233', 'rahul.gupta@example.com'),
(2, 'Sneha Mukherjee', '9822233344', 'sneha.m@example.com'),
(3, 'Vikramaditya Rao', '9833344455', 'vikram.rao@example.com'),
(4, 'Deepika Padukone', '9844455566', 'deepika.p@example.com'),
(5, 'Arjun Reddy', '9855566677', 'arjun.reddy@example.com');

-- 1.7 Insert Customer Alternate Phone Numbers (Multivalued Attribute)
INSERT INTO Customer_Alternate_No (customer_id, alternate_no) VALUES
(1, '9811122299'),
(1, '9811122288'),
(2, '9822233399'),
(4, '9844455599');

-- 1.8 Insert Payments (Transactions)
INSERT INTO Payment (payment_id, amount, payment_date, payment_method) VALUES
(1001, 10500.00, '2026-10-10', 'Credit Card'),
(1002, 24000.00, '2026-10-12', 'UPI'),
(1003, 4950.00, '2026-10-15', 'Net Banking'),
(1004, 15000.00, '2026-10-18', 'Debit Card'),
(1005, 8000.00, '2026-10-19', 'Cash');

-- 1.9 Insert Bookings (Linking Customer and Payment)
INSERT INTO Booking (booking_id, customer_id, payment_id, check_in_date, check_out_date) VALUES
(501, 1, 1001, '2026-10-10', '2026-10-13'), -- 3 nights
(502, 2, 1002, '2026-10-12', '2026-10-14'), -- 2 nights
(503, 3, 1003, '2026-10-15', '2026-10-16'), -- 1 night
(504, 4, 1004, '2026-10-18', '2026-10-20'), -- 2 nights
(505, 5, 1005, '2026-10-19', '2026-10-22'); -- 3 nights

-- 1.10 Insert Booking_Room (Resolving M:N "Reserves")
INSERT INTO Booking_Room (booking_id, room_id) VALUES
(501, 203), -- Booking 501 booked Room 201
(502, 206), -- Booking 502 booked Room 401 (Presidential)
(503, 201), -- Booking 503 booked Room 101
(504, 205), -- Booking 504 booked Room 301
(505, 204); -- Booking 505 booked Room 202

-- 1.11 Insert Booking_Service (Resolving M:N "Utilizes")
INSERT INTO Booking_Service (booking_id, service_id) VALUES
(501, 1), -- Booking 501 utilized Spa
(501, 5), -- Booking 501 utilized Buffet
(502, 2), -- Booking 502 utilized Airport Shuttle
(502, 4), -- Booking 502 utilized Pool Cabana
(503, 3), -- Booking 503 utilized Laundry
(504, 1), -- Booking 504 utilized Spa
(505, 5); -- Booking 505 utilized Buffet

-- 1.12 Insert Bills (Weak Entity to Booking, Receives Payment)
INSERT INTO Bill (booking_id, bill_id, payment_id, generation_date, total_amount) VALUES
(501, 9001, 1001, '2026-10-13', 10500.00),
(502, 9002, 1002, '2026-10-14', 24000.00),
(503, 9003, 1003, '2026-10-16', 4950.00),
(504, 9004, 1004, '2026-10-20', 15000.00),
(505, 9005, 1005, '2026-10-22', 8000.00);


-- 2.1 Basic Inspection: View All Customers with Primary & Alternate Contact Numbers
SELECT 
    c.customer_id,
    c.name AS customer_name,
    c.contact_no AS primary_contact,
    c.email,
    COALESCE(GROUP_CONCAT(a.alternate_no SEPARATOR ', '), 'None Registered') AS alternate_numbers
FROM Customer c
LEFT JOIN Customer_Alternate_No a ON c.customer_id = a.customer_id
GROUP BY c.customer_id, c.name, c.contact_no, c.email;

-- 2.2 Dynamic Calculation of Stay Duration & Room Cost Per Booking
SELECT 
    b.booking_id,
    c.name AS guest_name,
    r.room_no,
    rt.price AS room_price_per_night,
    b.check_in_date,
    b.check_out_date,
    DATEDIFF(b.check_out_date, b.check_in_date) AS stay_duration_days,
    (DATEDIFF(b.check_out_date, b.check_in_date) * rt.price) AS total_room_cost
FROM Booking b
JOIN Customer c ON b.customer_id = c.customer_id
JOIN Booking_Room br ON b.booking_id = br.booking_id
JOIN Room r ON br.room_id = r.room_id
JOIN Room_Type rt ON r.room_type_id = rt.room_type_id;

-- Combines Customer, Booking, Room, Room Type, Bill, and Payment
SELECT 
    b.booking_id,
    c.name AS customer_name,
    c.email,
    r.room_no,
    rt.price AS base_rate,
    DATEDIFF(b.check_out_date, b.check_in_date) AS nights_stayed,
    bill.bill_id,
    bill.total_amount AS final_billed_amount,
    p.payment_method,
    p.amount AS amount_paid,
    p.payment_date
FROM Booking b
INNER JOIN Customer c ON b.customer_id = c.customer_id
INNER JOIN Booking_Room br ON b.booking_id = br.booking_id
INNER JOIN Room r ON br.room_id = r.room_id
INNER JOIN Room_Type rt ON r.room_type_id = rt.room_type_id
LEFT JOIN Bill bill ON b.booking_id = bill.booking_id
LEFT JOIN Payment p ON b.payment_id = p.payment_id;

-- 2.4 Extra Services Utilization Breakdown by Booking
SELECT 
    b.booking_id,
    c.name AS customer_name,
    s.service_name,
    s.cost AS service_cost
FROM Booking b
JOIN Customer c ON b.customer_id = c.customer_id
JOIN Booking_Service bs ON b.booking_id = bs.booking_id
JOIN Service s ON bs.service_id = s.service_id
ORDER BY b.booking_id;


-- 3.1 Total Revenue & Transaction Count by Payment Method
SELECT 
    payment_method,
    COUNT(payment_id) AS total_transactions,
    SUM(amount) AS total_revenue_collected,
    AVG(amount) AS average_ticket_size,
    MAX(amount) AS highest_payment
FROM Payment
GROUP BY payment_method
ORDER BY total_revenue_collected DESC;

-- 3.2 Service Popularity & Total Revenue Generated Per Amenity
SELECT 
    s.service_name,
    COUNT(bs.booking_id) AS times_ordered,
    SUM(s.cost) AS total_revenue_generated
FROM Service s
LEFT JOIN Booking_Service bs ON s.service_id = bs.service_id
GROUP BY s.service_id, s.service_name
ORDER BY times_ordered DESC;

-- 3.3 Staff Distribution & Total Headcount by Department
SELECT 
    d.department_id,
    d.name AS department_name,
    COUNT(s.staff_id) AS employee_count
FROM Department d
LEFT JOIN Staff s ON d.department_id = s.department_id
GROUP BY d.department_id, d.name
HAVING employee_count > 0;

-- 4.1 Subquery with IN: Find Customers Who Have Booked Rooms on Floors Above Floor 1
SELECT name, email, contact_no
FROM Customer
WHERE customer_id IN (
    SELECT b.customer_id
    FROM Booking b
    JOIN Booking_Room br ON b.booking_id = br.booking_id
    JOIN Room r ON br.room_id = r.room_id
    WHERE r.floor > 1
);

-- 4.2 Subquery with Scalar Comparison: Payments Exceeding the Average Hotel Transaction
SELECT payment_id, amount, payment_method, payment_date
FROM Payment
WHERE amount > (SELECT AVG(amount) FROM Payment);

-- 4.3 Correlated Subquery: Find Customers with Higher Than Average Total Spend
SELECT c.customer_id, c.name, bill.total_amount
FROM Customer c
JOIN Booking b ON c.customer_id = b.customer_id
JOIN Bill bill ON b.booking_id = bill.booking_id
WHERE bill.total_amount > (SELECT AVG(total_amount) FROM Bill);

-- 5.1 Conditional Update: Apply a 10% Inflation Adjustment to Deluxe & Suite Room Types
-- Note: 'room_type_id > 0' satisfies MySQL Workbench Safe Updates mode (Error Code 1175)
UPDATE Room_Type
SET price = price * 1.10
WHERE room_type_id > 0 AND capacity >= 2;

-- 5.2 Conditional Update: Update Staff Phone Number upon Promotion
UPDATE Staff
SET phone_no = '9876543999', role = 'Senior Reception Executive'
WHERE staff_id = 102;

-- 5.3 Controlled Deletion with Referential Cascade Test:
-- Delete a Customer's specific alternate phone number
DELETE FROM Customer_Alternate_No
WHERE customer_id = 1 AND alternate_no = '9811122288';
