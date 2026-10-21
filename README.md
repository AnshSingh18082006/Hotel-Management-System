# Hotel Management Database System (RDBMS)

> **Academic Coursework:** 2nd Year / 3rd Semester • Relational Database Management Systems (RDBMS)  
> **Database Engine:** MySQL 8.0 (InnoDB) • **Compatibility:** ANSI SQL / MySQL Workbench / Java JDBC  
> **Architecture:** 3NF/BCNF Relational Architecture with Role-Based Access Control (RBAC)

---

## 📌 Project Overview
This project is an enterprise-grade, fully normalized relational database system designed to automate complete hotel hospitality operations:
* **Walk-In Guest Registration & Multi-Occupant Tracking:** Accommodates primary bookers and individual co-occupant government IDs (Aadhaar/Passport) for regulatory compliance.
* **Autonomous Overbooking Prevention:** Enforces mathematical date-overlap checks directly at the database engine tier via triggers.
* **Dynamic Real-Time Invoicing:** Stored procedures calculate stay durations on the fly and auto-increment bill totals as amenities (Spa, Buffet, Shuttle) are ordered.
* **Financial Governance & Payment Auditing:** Validates advance deposit ceilings (150% incidentals cap) and logs immutable transaction audit trails.
* **Role-Based Access Control (RBAC):** Five hardened departmental accounts enforcing the Principle of Least Privilege (PoLP).

---

## 🗂️ 7-Step Academic Deliverables Breakdown

| Step | Deliverable File(s) | Description |
| :--- | :--- | :--- |
| **Step 1** | `Step1_ER_Diagram.html`, `ER diagram.jpeg` | Conceptual Entity-Relationship model utilizing Chen notation, weak entity identifiers, multivalued double-ovals, and Crow's Foot cardinality. |
| **Step 2** | `Step2_Relational_Schema_Diagram.html`, `Relational Schema.jpeg` | Relational table schema strictly mapping Primary Key (PK) to Foreign Key (FK) dependencies with referential cascade rules. |
| **Step 3** | `Step3_Normalization_Report.docx` | Rigorous formal normalization decomposition from Unnormalized Form (**UNF**) $\rightarrow$ **1NF** $\rightarrow$ **2NF** $\rightarrow$ **3NF** $\rightarrow$ **BCNF**. |
| **Step 4** | `hotel_schema_ddl.sql`, `Step4_DDL_Queries_Report.docx` | Complete Data Definition Language (DDL) creating all 13 tables, primary/foreign keys, domain checks, unique constraints, and B-tree indexes. |
| **Step 5** | `hotel_queries_dml.sql`, `Step5_DML_Queries_Report.docx` | Comprehensive DML suite: sample data inserts, 6-table joins, aggregate queries (`GROUP BY / HAVING`), nested subqueries, and updates. |
| **Step 6** | `hotel_procedures_triggers.sql`, `test_verification_suite.sql`, `Step6_Procedures_Triggers_Report.docx` | Active business logic: 3 stored procedures, 6 database triggers (date validation, double-booking shield, auto-billing, payment audits, deposit caps, room capacity). |
| **Step 7** | `hotel_user_management.sql`, `Step7_User_Management_Report.docx` | Departmental User Management & Security: 5 user accounts, granular DCL permissions, privilege revocation (`REVOKE DELETE`), and account hardening. |
| **UI** | `hotel_frontend_mockup.html` | Interactive multi-role front-end terminal featuring dedicated dashboards for Front Desk, General Manager, Cashier, Housekeeping, and DBA. |

---

## 🏛️ Database Schema Architecture (13 Tables)

```
                            ┌───────────────┐
                            │  DEPARTMENT   │
                            └───────┬───────┘
                                    │ 1:N
                                    ▼
                            ┌───────────────┐
                            │     STAFF     │
                            └───────────────┘

  ┌───────────────────┐               ┌───────────────────┐
  │     CUSTOMER      │               │      PAYMENT      │
  └─────────┬─────────┘               └─────────┬─────────┘
            │ 1:N                               │ 1:1
            ├───────────────┐                   │
            ▼               ▼                   ▼
┌───────────────────────┐ ┌───────────────────────────────┐
│ Customer_Alternate_No │ │            BOOKING            │
└───────────────────────┘ └───────┬───────────────┬───────┘
                                  │               │
                    ┌─────────────┤               ├─────────────┐
                    │ M:N         │ M:N           │ 1:N         │ 1:1
                    ▼             ▼               ▼             ▼
          ┌──────────────┐ ┌─────────────┐ ┌──────────────┐ ┌─────────┐
          │ BOOKING_ROOM │ │BOOKING_SERV │ │BOOKING_GUEST │ │  BILL   │
          └──────┬───────┘ └──────┬──────┘ └──────────────┘ └─────────┘
                 │                │
                 ▼                ▼
          ┌──────────────┐ ┌─────────────┐
          │     ROOM     │ │   SERVICE   │
          └──────┬───────┘ └─────────────┘
                 │ N:1
                 ▼
          ┌──────────────┐
          │  ROOM_TYPE   │
          └──────────────┘
```

---

## ⚙️ Stored Procedures & Triggers Specification

### Stored Procedures
1. **`sp_CheckRoomAvailability(p_room_type_id, p_check_in, p_check_out)`**: Uses mathematical date-overlap logic to return vacant rooms of a specific type.
2. **`sp_CalculateFinalBill(p_booking_id)`**: Calculates $\text{Nights} \times \text{Price} + \sum(\text{Services})$, compares with advance payment, and outputs `net_balance_payable` and `settlement_status` ('Settled', 'Balance Due', or 'Refund Payable').
3. **`sp_GetCustomerHistory(p_customer_id)`**: Retrieves the guest's lifetime reservation, billing, and payment portfolio.

### Database Triggers
1. **`trg_ValidateBookingDates`**: `BEFORE INSERT ON Booking` — Blocks invalid dates (`check_out <= check_in`).
2. **`trg_PreventDoubleBooking`**: `BEFORE INSERT ON Booking_Room` — Prevents allocating a room already reserved for overlapping dates.
3. **`trg_AutoUpdateBillOnService`**: `AFTER INSERT ON Booking_Service` — Automatically adds the service cost to the customer's bill total.
4. **`trg_AuditPaymentLog`**: `AFTER INSERT ON Payment` — Automatically records an immutable entry in `Payment_Audit_Log`.
5. **`trg_ValidateAdvancePaymentCeiling`**: `BEFORE INSERT ON Booking_Room` — Enforces financial governance by capping advance payments to 150% of the room stay cost.
6. **`trg_EnforceRoomCapacity`**: `BEFORE INSERT ON Booking_Guest` — Blocks registering more occupants than the legal capacity of the allotted room type.

---

## 🔐 Departmental Access Control (Step 7 RBAC)

| Account | Role / Scope | Granted Privileges | Restriction Enforced |
| :--- | :--- | :--- | :--- |
| **`admin_ananya@localhost`** | Database Administrator | `ALL PRIVILEGES ... WITH GRANT OPTION` | Root system access. |
| **`manager_amit@localhost`** | General Manager | `SELECT` on all tables, `EXECUTE` on procedures | Read-only analytics; no DDL or DELETE. |
| **`reception_priya@localhost`**| Front Desk Executive | `SELECT, INSERT, UPDATE` on Bookings/Guests/Services | Cannot view staff salaries; `DELETE` revoked. |
| **`cashier_vikas@localhost`** | Cashier / Finance | `SELECT, INSERT, UPDATE` on Payment, Bill | No access to employee employment records. |
| **`hk_ramesh@localhost`** | Housekeeping Supervisor | `SELECT` on Room, Room_Type | Zero access to customer personal info or bills. |

---

## 🚀 Execution & Setup Instructions

To load the complete system into your local MySQL server:

```sql
-- Step 1: Create Database & Tables (13 tables + constraints)
source hotel_schema_ddl.sql;

-- Step 2: Insert Realistic Sample Data
source hotel_queries_dml.sql;

-- Step 3: Register Stored Procedures & Triggers
source hotel_procedures_triggers.sql;

-- Step 4: Configure Users & Security Policies
source hotel_user_management.sql;

-- Step 5: Run Automated Verification Suite (10 Comprehensive Tests)
source test_verification_suite.sql;
```

---

## 🖥️ Interactive Front-End Terminal
Open `hotel_frontend_mockup.html` in any web browser to access:
* **Interactive Floor Map:** Real-time room status with color-coded badges and 1-click room allotment.
* **Role Switcher:** Dynamic persona switching between Front Desk, General Manager, Cashier, Housekeeping, and DBA.
* **Tax Invoice Generator:** Printable receipt generator with GSTIN and service breakdowns.
