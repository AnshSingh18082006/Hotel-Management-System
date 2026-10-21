
DROP DATABASE IF EXISTS hotel_management_db;
CREATE DATABASE hotel_management_db;
USE hotel_management_db;

-- 1.1 Customer Table
CREATE TABLE Customer (
    customer_id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_no VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    CONSTRAINT pk_customer PRIMARY KEY (customer_id),
    CONSTRAINT uq_customer_email UNIQUE (email)
);

-- 1.2 Room_Type Table
CREATE TABLE Room_Type (
    room_type_id INT AUTO_INCREMENT,
    price DECIMAL(10, 2) NOT NULL,
    capacity INT NOT NULL,
    CONSTRAINT pk_room_type PRIMARY KEY (room_type_id),
    CONSTRAINT chk_room_type_price CHECK (price > 0),
    CONSTRAINT chk_room_type_capacity CHECK (capacity >= 1)
);

-- 1.3 Service Table
CREATE TABLE Service (
    service_id INT AUTO_INCREMENT,
    service_name VARCHAR(100) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_service PRIMARY KEY (service_id),
    CONSTRAINT chk_service_cost CHECK (cost >= 0)
);

-- 1.4 Department Table
CREATE TABLE Department (
    department_id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_department PRIMARY KEY (department_id),
    CONSTRAINT uq_department_name UNIQUE (name)
);

-- 1.5 Payment Table
CREATE TABLE Payment (
    payment_id INT AUTO_INCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    CONSTRAINT pk_payment PRIMARY KEY (payment_id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

-- 2.1 Customer_Alternate_No Table
CREATE TABLE Customer_Alternate_No (
    customer_id INT NOT NULL,
    alternate_no VARCHAR(15) NOT NULL,
    CONSTRAINT pk_customer_alternate_no PRIMARY KEY (customer_id, alternate_no),
    CONSTRAINT fk_alternate_customer FOREIGN KEY (customer_id)
        REFERENCES Customer (customer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 2.2 Staff Table
CREATE TABLE Staff (
    staff_id INT AUTO_INCREMENT,
    department_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone_no VARCHAR(15) NOT NULL,
    CONSTRAINT pk_staff PRIMARY KEY (staff_id),
    CONSTRAINT fk_staff_department FOREIGN KEY (department_id)
        REFERENCES Department (department_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 2.3 Room Table
CREATE TABLE Room (
    room_id INT AUTO_INCREMENT,
    room_type_id INT NOT NULL,
    room_no VARCHAR(10) NOT NULL,
    floor INT NOT NULL,
    CONSTRAINT pk_room PRIMARY KEY (room_id),
    CONSTRAINT uq_room_no UNIQUE (room_no),
    CONSTRAINT chk_room_floor CHECK (floor >= 0),
    CONSTRAINT fk_room_room_type FOREIGN KEY (room_type_id)
        REFERENCES Room_Type (room_type_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 2.4 Booking Table
-- Matches Schema: Booking [PK: booking_id (int), FK: customer_id (int)
CREATE TABLE Booking (
    booking_id INT AUTO_INCREMENT,
    customer_id INT NOT NULL,
    payment_id INT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (booking_id),
    CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id)
        REFERENCES Customer (customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_payment FOREIGN KEY (payment_id)
        REFERENCES Payment (payment_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- 2.5 Bill Table

CREATE TABLE Bill (
    booking_id INT NOT NULL,
    bill_id INT NOT NULL,
    payment_id INT NULL,
    generation_date DATE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_bill PRIMARY KEY (booking_id, bill_id),
    CONSTRAINT uq_bill_id UNIQUE (bill_id),
    CONSTRAINT chk_bill_total CHECK (total_amount >= 0),
    CONSTRAINT fk_bill_booking FOREIGN KEY (booking_id)
        REFERENCES Booking (booking_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_bill_payment FOREIGN KEY (payment_id)
        REFERENCES Payment (payment_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- 3.1 Booking_Room Table

CREATE TABLE Booking_Room (
    booking_id INT NOT NULL,
    room_id INT NOT NULL,
    CONSTRAINT pk_booking_room PRIMARY KEY (booking_id, room_id),
    CONSTRAINT fk_br_booking FOREIGN KEY (booking_id)
        REFERENCES Booking (booking_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_br_room FOREIGN KEY (room_id)
        REFERENCES Room (room_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 3.2 Booking_Service Table
CREATE TABLE Booking_Service (
    booking_id INT NOT NULL,
    service_id INT NOT NULL,
    CONSTRAINT pk_booking_service PRIMARY KEY (booking_id, service_id),
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id)
        REFERENCES Booking (booking_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_bs_service FOREIGN KEY (service_id)
        REFERENCES Service (service_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 3.3 Booking_Guest Table (Government ID Registration for Couples & Co-Occupants)
CREATE TABLE IF NOT EXISTS Booking_Guest (
    guest_id INT AUTO_INCREMENT,
    booking_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    id_proof_type VARCHAR(50) NOT NULL,    -- Aadhaar, Passport, Driving License, Voter ID
    id_number VARCHAR(50) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_booking_guest PRIMARY KEY (guest_id),
    CONSTRAINT fk_guest_booking FOREIGN KEY (booking_id)
        REFERENCES Booking (booking_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_booking_customer ON Booking (customer_id);
CREATE INDEX idx_booking_payment ON Booking (payment_id);
CREATE INDEX idx_room_type ON Room (room_type_id);
CREATE INDEX idx_staff_department ON Staff (department_id);
CREATE INDEX idx_bill_payment ON Bill (payment_id);
CREATE INDEX idx_booking_room_room ON Booking_Room (room_id);
CREATE INDEX idx_booking_service_service ON Booking_Service (service_id);
CREATE INDEX idx_booking_guest_booking ON Booking_Guest (booking_id);
