USE hotel_management_db;

CREATE TABLE IF NOT EXISTS Payment_Audit_Log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    payment_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$

-- ----------------------------------------------------------------------------
-- 1.1 Procedure: Check Room Availability for Given Dates and Room Type
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_CheckRoomAvailability$$
CREATE PROCEDURE sp_CheckRoomAvailability(
    IN p_room_type_id INT,
    IN p_check_in DATE,
    IN p_check_out DATE
)
BEGIN
    SELECT 
        r.room_id,
        r.room_no,
        r.floor,
        rt.price AS price_per_night,
        rt.capacity
    FROM Room r
    JOIN Room_Type rt ON r.room_type_id = rt.room_type_id
    WHERE r.room_type_id = p_room_type_id
      AND r.room_id NOT IN (
          SELECT br.room_id
          FROM Booking_Room br
          JOIN Booking b ON br.booking_id = b.booking_id
          WHERE (b.check_in_date < p_check_out AND b.check_out_date > p_check_in)
      );
END$$

-- ----------------------------------------------------------------------------
-- 1.2 Procedure: Generate/Recalculate Final Bill for a Booking
-- Computes (Nights * Room_Price) + SUM(Services Cost)
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_CalculateFinalBill$$
CREATE PROCEDURE sp_CalculateFinalBill(
    IN p_booking_id INT
)
BEGIN
    DECLARE v_room_charges DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_service_charges DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_total_bill DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_advance_paid DECIMAL(10, 2) DEFAULT 0.00;

    -- 1. Calculate Room Charges (Stay Duration * Room Price)
    SELECT COALESCE(SUM(DATEDIFF(b.check_out_date, b.check_in_date) * rt.price), 0.00)
    INTO v_room_charges
    FROM Booking b
    JOIN Booking_Room br ON b.booking_id = br.booking_id
    JOIN Room r ON br.room_id = r.room_id
    JOIN Room_Type rt ON r.room_type_id = rt.room_type_id
    WHERE b.booking_id = p_booking_id;

    -- 2. Calculate Services Charges
    SELECT COALESCE(SUM(s.cost), 0.00)
    INTO v_service_charges
    FROM Booking_Service bs
    JOIN Service s ON bs.service_id = s.service_id
    WHERE bs.booking_id = p_booking_id;

    -- 3. Total Bill
    SET v_total_bill = v_room_charges + v_service_charges;

    -- 4. Update the Bill table
    UPDATE Bill
    SET total_amount = v_total_bill,
        generation_date = CURRENT_DATE
    WHERE booking_id = p_booking_id;

    -- 5. Fetch advance payment received
    SELECT COALESCE(p.amount, 0.00)
    INTO v_advance_paid
    FROM Booking b
    LEFT JOIN Payment p ON b.payment_id = p.payment_id
    WHERE b.booking_id = p_booking_id;

    -- 6. Return comprehensive settlement dossier including balance & refund status
    SELECT 
        p_booking_id AS booking_id,
        v_room_charges AS room_subtotal,
        v_service_charges AS services_subtotal,
        v_total_bill AS final_total_amount,
        v_advance_paid AS advance_payment_received,
        (v_total_bill - v_advance_paid) AS net_balance_payable,
        CASE 
            WHEN (v_total_bill - v_advance_paid) > 0 THEN 'Balance Due from Guest'
            WHEN (v_total_bill - v_advance_paid) = 0 THEN 'Account Settled / Zero Balance'
            ELSE 'Refund Payable to Guest'
        END AS settlement_status;
END$$

-- ----------------------------------------------------------------------------
-- 1.3 Procedure: Customer Booking History Dossier
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_GetCustomerHistory$$
CREATE PROCEDURE sp_GetCustomerHistory(
    IN p_customer_id INT
)
BEGIN
    SELECT 
        c.customer_id,
        c.name AS customer_name,
        b.booking_id,
        b.check_in_date,
        b.check_out_date,
        DATEDIFF(b.check_out_date, b.check_in_date) AS nights,
        r.room_no,
        rt.price AS room_price,
        bill.total_amount AS billed_amount,
        p.amount AS amount_paid,
        p.payment_method
    FROM Customer c
    JOIN Booking b ON c.customer_id = b.customer_id
    JOIN Booking_Room br ON b.booking_id = br.booking_id
    JOIN Room r ON br.room_id = r.room_id
    JOIN Room_Type rt ON r.room_type_id = rt.room_type_id
    LEFT JOIN Bill bill ON b.booking_id = bill.booking_id
    LEFT JOIN Payment p ON b.payment_id = p.payment_id
    WHERE c.customer_id = p_customer_id
    ORDER BY b.check_in_date DESC;
END$$


-- ----------------------------------------------------------------------------
-- 2.1 Trigger: Validate Booking Dates Before Insertion
-- Ensures check_in_date < check_out_date
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_ValidateBookingDates$$
CREATE TRIGGER trg_ValidateBookingDates
BEFORE INSERT ON Booking
FOR EACH ROW
BEGIN
    IF NEW.check_out_date <= NEW.check_in_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Check-out date must be strictly after Check-in date.';
    END IF;
END$$

-- ----------------------------------------------------------------------------
-- 2.2 Trigger: Prevent Double Booking of the Same Room for Overlapping Dates
-- Fires BEFORE INSERT on Booking_Room
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_PreventDoubleBooking$$
CREATE TRIGGER trg_PreventDoubleBooking
BEFORE INSERT ON Booking_Room
FOR EACH ROW
BEGIN
    DECLARE v_overlap_count INT DEFAULT 0;
    DECLARE v_new_check_in DATE;
    DECLARE v_new_check_out DATE;

    -- Fetch the new booking's dates
    SELECT check_in_date, check_out_date 
    INTO v_new_check_in, v_new_check_out
    FROM Booking 
    WHERE booking_id = NEW.booking_id;

    -- Check if this room is already booked for overlapping dates
    SELECT COUNT(*)
    INTO v_overlap_count
    FROM Booking_Room br
    JOIN Booking b ON br.booking_id = b.booking_id
    WHERE br.room_id = NEW.room_id
      AND (b.check_in_date < v_new_check_out AND b.check_out_date > v_new_check_in);

    IF v_overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Conflict: The selected room is already reserved for overlapping dates.';
    END IF;
END$$

-- ----------------------------------------------------------------------------
-- 2.3 Trigger: Automatically Add Service Cost to Bill on New Service Order
-- Fires AFTER INSERT on Booking_Service
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_AutoUpdateBillOnService$$
CREATE TRIGGER trg_AutoUpdateBillOnService
AFTER INSERT ON Booking_Service
FOR EACH ROW
BEGIN
    DECLARE v_service_cost DECIMAL(10, 2);

    -- Get cost of the service ordered
    SELECT cost INTO v_service_cost
    FROM Service
    WHERE service_id = NEW.service_id;

    -- Automatically increment the bill
    UPDATE Bill
    SET total_amount = total_amount + v_service_cost
    WHERE booking_id = NEW.booking_id;
END$$

-- ----------------------------------------------------------------------------
-- 2.4 Trigger: Audit Log for Every Recorded Payment
-- Fires AFTER INSERT on Payment
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_AuditPaymentLog$$
CREATE TRIGGER trg_AuditPaymentLog
AFTER INSERT ON Payment
FOR EACH ROW
BEGIN
    INSERT INTO Payment_Audit_Log (payment_id, amount, payment_method)
    VALUES (NEW.payment_id, NEW.amount, NEW.payment_method);
END$$

-- ----------------------------------------------------------------------------
-- 2.5 Trigger: Financial Governance - Prevent Exorbitant Overpayment
-- Fires BEFORE INSERT on Booking_Room
-- Caps maximum advance deposit to 150% of estimated stay cost (allows incidentals)
-- Blocks extreme overpayments (e.g. 10 lac for a 22k room) under PMLA compliance
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_ValidateAdvancePaymentCeiling$$
CREATE TRIGGER trg_ValidateAdvancePaymentCeiling
BEFORE INSERT ON Booking_Room
FOR EACH ROW
BEGIN
    DECLARE v_est_room_cost DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_advance_paid DECIMAL(10, 2) DEFAULT 0.00;

    -- Calculate estimated room stay cost and fetch advance payment
    SELECT (DATEDIFF(b.check_out_date, b.check_in_date) * rt.price), COALESCE(p.amount, 0.00)
    INTO v_est_room_cost, v_advance_paid
    FROM Booking b
    JOIN Room r ON r.room_id = NEW.room_id
    JOIN Room_Type rt ON r.room_type_id = rt.room_type_id
    LEFT JOIN Payment p ON b.payment_id = p.payment_id
    WHERE b.booking_id = NEW.booking_id;

    -- Enforce ceiling rule: Maximum allowed advance is 150% of room cost
    IF v_advance_paid > (v_est_room_cost * 1.50) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Financial Governance Error: Advance payment exceeds allowable 150% incidental deposit ceiling!';
    END IF;
END$$

-- ----------------------------------------------------------------------------
-- 2.6 Trigger: Enforce Maximum Room Legal Capacity for Multiple Occupants (Couples)
-- Fires BEFORE INSERT on Booking_Guest
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_EnforceRoomCapacity$$
CREATE TRIGGER trg_EnforceRoomCapacity
BEFORE INSERT ON Booking_Guest
FOR EACH ROW
BEGIN
    DECLARE v_current_guest_count INT DEFAULT 0;
    DECLARE v_max_capacity INT DEFAULT 0;

    -- Count existing registered occupants for this booking
    SELECT COUNT(*) INTO v_current_guest_count
    FROM Booking_Guest
    WHERE booking_id = NEW.booking_id;

    -- Look up maximum room capacity
    SELECT rt.capacity INTO v_max_capacity
    FROM Booking_Room br
    JOIN Room r ON br.room_id = r.room_id
    JOIN Room_Type rt ON r.room_type_id = rt.room_type_id
    WHERE br.booking_id = NEW.booking_id
    LIMIT 1;

    -- Reject if room capacity is exceeded
    IF v_current_guest_count >= v_max_capacity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Capacity Violation: Number of occupants exceeds maximum legal capacity of the allotted room!';
    END IF;
END$$

DELIMITER ;

-- Test 1: Check availability of Double Deluxe rooms (Type 2) between 2026-11-01 and 2026-11-05
CALL sp_CheckRoomAvailability(2, '2026-11-01', '2026-11-05');

-- Test 2: Calculate bill for Booking 501
CALL sp_CalculateFinalBill(501);

-- Test 3: Get full dossier for Customer 1
CALL sp_GetCustomerHistory(1);
