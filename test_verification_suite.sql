-- ============================================================================
-- HOTEL MANAGEMENT DATABASE SYSTEM
-- STEP 6: VERIFICATION & TEST SCRIPT
-- Run this script to test all 3 Procedures and all 4 Triggers
-- ============================================================================

USE hotel_management_db;

-- ----------------------------------------------------------------------------
-- TEST 1: Test sp_CheckRoomAvailability
-- Check if Double Deluxe rooms (Room_Type 2) are free between Nov 1 and Nov 5, 2026
-- ----------------------------------------------------------------------------
SELECT '--- TEST 1: Checking Room Availability ---' AS test_name;
CALL sp_CheckRoomAvailability(2, '2026-11-01', '2026-11-05');


-- ----------------------------------------------------------------------------
-- TEST 2: Test sp_CalculateFinalBill
-- Calculate and update final bill for Booking 501
-- Expected: (3 nights * 4500) + Spa (1800) + Buffet (1500) = 16800.00
-- ----------------------------------------------------------------------------
SELECT '--- TEST 2: Calculate Final Bill for Booking 501 ---' AS test_name;
CALL sp_CalculateFinalBill(501);

-- Verify the Bill table was updated
SELECT * FROM Bill WHERE booking_id = 501;


-- ----------------------------------------------------------------------------
-- TEST 3: Test sp_GetCustomerHistory
-- Retrieve the full booking dossier for Customer 1 (Rahul Gupta)
-- ----------------------------------------------------------------------------
SELECT '--- TEST 3: Customer 1 Booking Dossier ---' AS test_name;
CALL sp_GetCustomerHistory(1);


-- ----------------------------------------------------------------------------
-- TEST 4: Test Trigger trg_AutoUpdateBillOnService
-- Order an additional Airport Shuttle (Service 2 = cost 1200) for Booking 503
-- ----------------------------------------------------------------------------
SELECT '--- TEST 4: Trigger Auto-Increment Bill on Service Order ---' AS test_name;

-- Check bill before
SELECT booking_id, total_amount AS bill_before FROM Bill WHERE booking_id = 503;

-- Insert new service usage
INSERT INTO Booking_Service (booking_id, service_id) VALUES (503, 2);

-- Check bill after (should be bill_before + 1200)
SELECT booking_id, total_amount AS bill_after FROM Bill WHERE booking_id = 503;


-- ----------------------------------------------------------------------------
-- TEST 5: Test Trigger trg_AuditPaymentLog
-- Record a new payment and check if the audit log table catches it
-- ----------------------------------------------------------------------------
SELECT '--- TEST 5: Trigger Payment Audit Trail ---' AS test_name;

INSERT INTO Payment (payment_id, amount, payment_date, payment_method)
VALUES (9999, 5000.00, CURRENT_DATE, 'UPI');

-- Check the audit log table
SELECT * FROM Payment_Audit_Log WHERE payment_id = 9999;


-- ----------------------------------------------------------------------------
-- TEST 6: Test Trigger trg_ValidateBookingDates (Should FAIL with Error)
-- Try booking with check_out earlier than check_in (invalid dates)
-- ----------------------------------------------------------------------------
SELECT '--- TEST 6: Trigger Validate Booking Dates (Expect Error) ---' AS test_name;
-- Uncomment the line below to test the error block:
-- INSERT INTO Booking (booking_id, customer_id, check_in_date, check_out_date) VALUES (888, 1, '2026-12-10', '2026-12-05');


-- ----------------------------------------------------------------------------
-- TEST 7: Test Trigger trg_PreventDoubleBooking (Should FAIL with Error)
-- Room 203 is already booked for Booking 501 (2026-10-10 to 2026-10-13)
-- Trying to book Room 203 during 2026-10-11 will be blocked by the trigger.
-- ----------------------------------------------------------------------------
SELECT '--- TEST 7: Trigger Prevent Double Booking (Expect Conflict Error) ---' AS test_name;
-- Uncomment the lines below to test the conflict block:
-- INSERT INTO Booking (booking_id, customer_id, check_in_date, check_out_date) VALUES (777, 2, '2026-10-11', '2026-10-15');
-- INSERT INTO Booking_Room (booking_id, room_id) VALUES (777, 203);


-- ----------------------------------------------------------------------------
-- TEST 8: Test Trigger trg_ValidateAdvancePaymentCeiling (Should FAIL with Error)
-- Test Financial Governance: Attempting to deposit 10 Lakh for a 22k room
-- Expected: Trigger blocks with "Financial Governance Error: Advance payment exceeds allowable 150%"
-- ----------------------------------------------------------------------------
SELECT '--- TEST 8: Trigger Financial Governance Advance Ceiling (Expect Error) ---' AS test_name;
-- Uncomment lines below to test exorbitant overpayment block:
-- INSERT INTO Payment (payment_id, amount, payment_date, payment_method) VALUES (8888, 1000000.00, CURRENT_DATE, 'UPI');
-- INSERT INTO Booking (booking_id, customer_id, payment_id, check_in_date, check_out_date) VALUES (888, 1, 8888, '2026-11-10', '2026-11-13');
-- INSERT INTO Booking_Room (booking_id, room_id) VALUES (888, 205);


-- ----------------------------------------------------------------------------
-- TEST 9: Test Trigger trg_EnforceRoomCapacity (Couple & Occupancy Limits)
-- Room 201 is a Single Standard room (Capacity = 1)
-- Adding a 2nd occupant to Booking 503 (which has Room 201) will be blocked!
-- ----------------------------------------------------------------------------
SELECT '--- TEST 9: Trigger Enforce Room Capacity (Expect Error on Overcapacity) ---' AS test_name;
-- Register Primary Guest (Occupant 1) - Allowed:
INSERT INTO Booking_Guest (booking_id, full_name, id_proof_type, id_number, is_primary)
VALUES (503, 'Vikramaditya Rao', 'Aadhaar Card', '9988-7766-5544', TRUE);

-- Uncomment line below to test trying to add a 2nd guest into a Single room (Capacity 1):
-- INSERT INTO Booking_Guest (booking_id, full_name, id_proof_type, id_number, is_primary) VALUES (503, 'Suresh Rao', 'Aadhaar Card', '9988-7766-1122', FALSE);


-- ----------------------------------------------------------------------------
-- TEST 10: Test Enhanced sp_CalculateFinalBill with Balance & Refund Dossier
-- ----------------------------------------------------------------------------
SELECT '--- TEST 10: Final Settlement Dossier with Refund/Balance Output ---' AS test_name;
CALL sp_CalculateFinalBill(501);
