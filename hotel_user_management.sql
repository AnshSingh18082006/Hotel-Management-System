USE hotel_management_db;

-- 1. USER PROVISIONING
DROP USER IF EXISTS 'admin_ananya'@'localhost', 
                    'manager_amit'@'localhost', 
                    'reception_priya'@'localhost', 
                    'cashier_vikas'@'localhost', 
                    'hk_ramesh'@'localhost';

CREATE USER 'admin_ananya'@'localhost' IDENTIFIED BY 'Admin@Secure2026';
CREATE USER 'manager_amit'@'localhost' IDENTIFIED BY 'Manager@Pass2026';
CREATE USER 'reception_priya'@'localhost' IDENTIFIED BY 'FrontDesk@2026';
CREATE USER 'cashier_vikas'@'localhost' IDENTIFIED BY 'Accounts@2026';
CREATE USER 'hk_ramesh'@'localhost' IDENTIFIED BY 'CleanRoom@2026';

-- Each account is granted only the exact permissions needed for their job
-- 2.1 User: admin_ananya (Database Administrator / IT Systems Head)
-- Full administrative control across the hotel database
GRANT ALL PRIVILEGES ON hotel_management_db.* TO 'admin_ananya'@'localhost' WITH GRANT OPTION;
-- 2.2 User: manager_amit (General Manager / Operations Director)
-- Read-only visibility across all tables; execution of analytical procedures.
-- Forbidden from modifying, inserting, or dropping records/tables.
GRANT SELECT ON hotel_management_db.* TO 'manager_amit'@'localhost';
GRANT EXECUTE ON hotel_management_db.* TO 'manager_amit'@'localhost';

-- 2.3 User: reception_priya (Front Desk Executive)
-- Can register guests, create bookings, assign rooms, and order amenities.
-- Restricted from viewing staff salaries, department admin, or audit logs.
-- Walk-In Guest Registration & Bookings
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Customer TO 'reception_priya'@'localhost';
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Customer_Alternate_No TO 'reception_priya'@'localhost';
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Booking TO 'reception_priya'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON hotel_management_db.Booking_Room TO 'reception_priya'@'localhost';
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Booking_Service TO 'reception_priya'@'localhost';
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Booking_Guest TO 'reception_priya'@'localhost';
GRANT SELECT, INSERT ON hotel_management_db.Payment TO 'reception_priya'@'localhost';

-- Catalog Inspection (Read-Only)
GRANT SELECT ON hotel_management_db.Room TO 'reception_priya'@'localhost';
GRANT SELECT ON hotel_management_db.Room_Type TO 'reception_priya'@'localhost';
GRANT SELECT ON hotel_management_db.Service TO 'reception_priya'@'localhost';
GRANT SELECT ON hotel_management_db.Bill TO 'reception_priya'@'localhost';

-- Execution of Stored Procedures for Guest Handling
GRANT EXECUTE ON hotel_management_db.* TO 'reception_priya'@'localhost';

-- 2.4 User: cashier_vikas (Cashier / Accounts & Finance)
-- Payment collections, invoice processing, and payment audit review.
-- Zero access to staff employee files.
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Payment TO 'cashier_vikas'@'localhost';
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Bill TO 'cashier_vikas'@'localhost';
GRANT SELECT ON hotel_management_db.Payment_Audit_Log TO 'cashier_vikas'@'localhost';
GRANT SELECT ON hotel_management_db.Customer TO 'cashier_vikas'@'localhost';
GRANT SELECT, UPDATE ON hotel_management_db.Booking TO 'cashier_vikas'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON hotel_management_db.Booking_Room TO 'cashier_vikas'@'localhost';
GRANT SELECT, INSERT, UPDATE ON hotel_management_db.Booking_Service TO 'cashier_vikas'@'localhost';
GRANT SELECT ON hotel_management_db.Room TO 'cashier_vikas'@'localhost';
GRANT SELECT ON hotel_management_db.Room_Type TO 'cashier_vikas'@'localhost';
GRANT SELECT ON hotel_management_db.Service TO 'cashier_vikas'@'localhost';
GRANT EXECUTE ON hotel_management_db.* TO 'cashier_vikas'@'localhost';

-- 2.5 User: hk_ramesh (Housekeeping Supervisor)
-- Needs room numbers and room types for daily cleaning inspections.
-- Completely restricted from seeing customer personal data, salaries, or payments.
GRANT SELECT ON hotel_management_db.Room TO 'hk_ramesh'@'localhost';
GRANT SELECT ON hotel_management_db.Room_Type TO 'hk_ramesh'@'localhost';
GRANT SELECT ON hotel_management_db.Booking TO 'hk_ramesh'@'localhost';
GRANT SELECT ON hotel_management_db.Booking_Room TO 'hk_ramesh'@'localhost';
GRANT SELECT ON hotel_management_db.Staff TO 'hk_ramesh'@'localhost';
GRANT SELECT ON hotel_management_db.Department TO 'hk_ramesh'@'localhost';


-- Requirement: To maintain reservation audit integrity, front desk staff must
-- NEVER be allowed to delete booking records. Cancellations must be logged via UPDATE.
-- Demonstration: We grant DELETE privilege, then immediately REVOKE it to show DCL control:
GRANT DELETE ON hotel_management_db.Booking TO 'reception_priya'@'localhost';
REVOKE DELETE ON hotel_management_db.Booking FROM 'reception_priya'@'localhost';


-- 5.1 Inspect All Provisioned Users and Account Lock Status
SELECT user, host, account_locked, password_expired 
FROM mysql.user 
WHERE user IN ('admin_ananya', 'manager_amit', 'reception_priya', 'cashier_vikas', 'hk_ramesh')
ORDER BY user;

-- 5.2 Verify Assigned Grants for Front Desk Receptionist
SHOW GRANTS FOR 'reception_priya'@'localhost';

-- 5.3 Verify Assigned Grants for Hotel General Manager
SHOW GRANTS FOR 'manager_amit'@'localhost';

-- 5.4 Verify Assigned Grants for Billing Cashier
SHOW GRANTS FOR 'cashier_vikas'@'localhost';

-- 5.5 Verify Assigned Grants for Housekeeping
SHOW GRANTS FOR 'hk_ramesh'@'localhost';

-- 5.6 Verify Assigned Grants for Administrator
SHOW GRANTS FOR 'admin_ananya'@'localhost';
