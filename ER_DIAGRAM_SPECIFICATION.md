# Entity-Relationship (ER) Diagram Specification & Explanation Guide

> **Coursework:** 2nd Year / 3rd Semester B.Tech &bull; Relational Database Management Systems (RDBMS)  
> **Topic:** Formal Conceptual ER Design & Structural Business Rules  
> **Notations Used:** Chen Notation (Entity, Weak Entity, Derived, Multivalued) & Crow's Foot Cardinality Ratios  

---

## 📌 1. Formal Chen Notation Reference Sheet

When presenting your ER diagram to your teacher or viva examiner, use these standard academic terms:

| ER Component | Chen Graphical Symbol | Meaning & Application in our Hotel Model |
| :--- | :--- | :--- |
| **Regular Entity** | Single Rectangle `[ ]` | An independent, tangible object with its own primary key (e.g., `CUSTOMER`, `ROOM`, `STAFF`, `PAYMENT`). |
| **Weak Entity** | Double Rectangle `[[ ]]` | An entity that cannot exist without its parent entity and lacks a standalone primary key (e.g., `BILL` depends on `BOOKING`). |
| **Identifying Relationship** | Double Diamond `<◇>` | Links a weak entity to its identifying parent entity (e.g., `GENERATES` relationship between `BOOKING` and `BILL`). |
| **Primary Key (PK)** | Underlined Oval `(_id_)` | The unique identifier attribute for an entity (e.g., <u>`customer_id`</u>, <u>`room_id`</u>). |
| **Multivalued Attribute** | Double Oval `(( ))` | An attribute holding multiple values for a single entity (e.g., `(( alternate_no ))` for a Customer). |
| **Derived Attribute** | Dashed Oval `(- -)` | An attribute dynamically calculated from other stored attributes (e.g., `(- duration -)` computed from check-in and check-out dates). |
| **Partial Key (Discriminator)**| Dashed Underline `_ _` | Attribute distinguishing occurrences of a weak entity under the same owner (e.g., `_bill_id_`). |
| **Total Participation** | Double Line `===` | Every instance of the entity **must** participate in the relationship (Mandatory / 100% participation). |
| **Partial Participation** | Single Line `───` | Some instances of the entity may not participate in the relationship (Optional participation). |

---

## 🔍 2. Rule-by-Rule Explanation Guide (With Oral Exam Explanations)

Here is how each of your **10 Business Rules** maps to the ER Diagram:

---

### Rule 1: Customer Entity & Multivalued Contact Numbers
> *"Customers have a customer ID, a name, a contact number, and an email. A customer can have multiple alternate contact numbers."*

* **Entity:** `CUSTOMER` (Strong Entity, Rectangle)
* **Primary Key:** <u>`customer_id`</u> (Underlined Oval)
* **Single-Valued Attributes:** `name`, `contact_no`, `email` (Simple Ovals)
* **Multivalued Attribute:** `alternate_no` (**Double Oval**)
* **💡 Teacher Explanation:**  
  *"A guest might provide personal, spouse, or office emergency mobile numbers. Because a single customer record can hold multiple phone numbers, `alternate_no` is depicted as a **double oval** in the ER diagram and normalized into an independent table in 1NF."*

---

### Rule 2: Room Type Entity
> *"Room types have a room type ID, a price, and a capacity."*

* **Entity:** `ROOM_TYPE` (Strong Entity, Rectangle)
* **Primary Key:** <u>`room_type_id`</u> (Underlined Oval)
* **Attributes:** `price`, `capacity` (Simple Ovals)
* **💡 Teacher Explanation:**  
  *"Instead of redundantly repeating prices and capacities for every physical room, we extracted `ROOM_TYPE` as a distinct entity to eliminate transitive dependencies and ensure 3NF compliance."*

---

### Rule 3: Room Entity & Categorization (Total Participation)
> *"Rooms have a room ID, a room number, and a floor. Each room belongs to exactly one room type, and one room type can categorize many rooms (total participation of room)."*

* **Entity:** `ROOM` (Strong Entity, Rectangle)
* **Attributes:** <u>`room_id`</u>, `room_no`, `floor`
* **Relationship:** `CATEGORIZES` (Diamond between `ROOM_TYPE` and `ROOM`)
* **Cardinality:** **$1 : N$** ($1$ Room Type categorizes $N$ Rooms)
* **Participation:**  
  - **`ROOM` has Total Participation (Double Line `===`):** Every room in the hotel **must** belong to a valid room type; an uncategorized room cannot legally exist.
  - **`ROOM_TYPE` has Partial Participation (Single Line `───`):** A newly added luxury suite type might exist in the catalog before rooms are assigned to it.

---

### Rule 4: Booking Entity & Derived Stay Duration
> *"Bookings have a booking ID, a check-in date, and a check-out date. The stay duration is a derived attribute calculated from the check-in and check-out dates."*

* **Entity:** `BOOKING` (Strong Entity, Rectangle)
* **Primary Key:** <u>`booking_id`</u> (Underlined Oval)
* **Stored Attributes:** `check_in_date`, `check_out_date` (Simple Ovals)
* **Derived Attribute:** `duration` (**Dashed Oval**)
* **💡 Teacher Explanation:**  
  *"Stay duration is not stored statically in the database to prevent redundant calculation anomalies. It is dynamically derived via the formula: `duration = DATEDIFF(check_out_date, check_in_date)`. In Chen notation, this is symbolized with a **dashed oval**."*

---

### Rule 5: Customer-Booking Relationship (Total Participation of Booking)
> *"One customer can make many bookings, but each booking belongs to exactly one customer (total participation of booking)."*

* **Relationship:** `MAKES` / `BOOKS` (Diamond between `CUSTOMER` and `BOOKING`)
* **Cardinality:** **$1 : N$** ($1$ Customer makes $N$ Bookings)
* **Participation:**
  - **`BOOKING` has Total Participation (Double Line `===`):** An orphan booking with no registered customer is strictly forbidden by referential integrity.
  - **`CUSTOMER` has Partial Participation (Single Line `───`):** A registered profile can exist in the hotel database before an active booking is reserved.

---

### Rule 6: Booking to Room Many-to-Many Relationship
> *"A booking can contain multiple rooms, and each room can appear in multiple bookings across different dates."*

* **Relationship:** `RESERVES` / `ALLOTS` (Diamond between `BOOKING` and `ROOM`)
* **Cardinality:** **$M : N$** ($M$ Bookings reserve $N$ Rooms)
* **💡 Teacher Explanation:**  
  *"A corporate family booking can reserve multiple rooms (e.g. Room 101 and Room 102). Conversely, Room 101 can be reserved by Rahul in October and by Priya in November. In relational database mapping, this $M:N$ relationship is decomposed into the junction table `Booking_Room`."*

---

### Rule 7: Services Catalog & Usage
> *"Services (such as laundry, spa, and room service) have a service ID, a service name, and a cost. A booking can use multiple services, and each service can be used across many bookings."*

* **Entity:** `SERVICE` (Strong Entity, Rectangle)
* **Attributes:** <u>`service_id`</u>, `service_name`, `cost`
* **Relationship:** `USES` (Diamond between `BOOKING` and `SERVICE`)
* **Cardinality:** **$M : N$** ($M$ Bookings consume $N$ Services)
* **💡 Teacher Explanation:**  
  *"A guest staying under Booking #501 can order both Breakfast Buffet and Spa. At the same time, the Spa service is ordered by dozens of other bookings. Resolved into the junction table `Booking_Service`."*

---

### Rule 8: Bill as a Weak Entity
> *"Bills have a bill ID, a total amount, and a generation date. Each booking generates exactly one bill, where the bill acts as a weak entity dependent on the booking."*

* **Weak Entity:** `BILL` (**Double Rectangle**)
* **Identifying Relationship:** `GENERATES` (**Double Diamond**)
* **Identifying Parent:** `BOOKING` (Strong Entity)
* **Partial Key (Discriminator):** `bill_id` (**Dashed Underline**)
* **Attributes:** `total_amount`, `generation_date`
* **Cardinality:** **$1 : 1$** ($1$ Booking generates exactly $1$ Bill)
* **Participation:** **`BILL` has Total Participation (Double Line `===`):**  
  - A bill has no independent existence without its originating booking transaction.
  - If a booking record is purged, its dependent invoice bill must cascade and cease to exist.

---

### Rule 9: Payments Linked to Bill
> *"Payments have a payment ID, an amount, a payment method, and a payment date. A bill can have one or multiple payments associated with it."*

* **Entity:** `PAYMENT` (Strong Entity, Rectangle)
* **Attributes:** <u>`payment_id`</u>, `amount`, `payment_method`, `payment_date`
* **Relationship:** `SETTLED_BY` (Diamond between `BILL` and `PAYMENT`)
* **Cardinality:** **$1 : N$** ($1$ Bill can receive $N$ Payments)
* **💡 Teacher Explanation:**  
  *"Guests often pay an advance deposit via UPI at check-in, and settle the remaining net balance at checkout via Credit Card or Cash. Thus, a single invoice bill maps to multiple payment transactions."*

---

### Rule 10: Department and Staff Hierarchy
> *"Departments have a department ID and a name. Staff members have a staff ID, a name, a role, and a phone number. A department can have many staff members, and each staff member belongs to exactly one department (total participation of staff)."*

* **Entities:** `DEPARTMENT` and `STAFF` (Strong Entities, Rectangles)
* **Attributes of `DEPARTMENT`:** <u>`department_id`</u>, `name`
* **Attributes of `STAFF`:** <u>`staff_id`</u>, `name`, `role`, `phone_no`
* **Relationship:** `EMPLOYS` / `BELONGS_TO` (Diamond between `DEPARTMENT` and `STAFF`)
* **Cardinality:** **$1 : N$** ($1$ Department employs $N$ Staff)
* **Participation:**  
  - **`STAFF` has Total Participation (Double Line `===`):** Every employee on payroll **must** be formally assigned to a department (Front Desk, Housekeeping, F&B, Maintenance). An unassigned staff member is not allowed.
  - **`DEPARTMENT` has Partial Participation (Single Line `───`):** A newly opened department might be configured before staff are assigned.

---

## 📊 3. Visual Entity-Relationship Architecture (Mermaid Diagram)

```mermaid
erDiagram
    CUSTOMER ||--o{ CUSTOMER_ALTERNATE_NO : "has alternate"
    CUSTOMER ||--|{ BOOKING : "makes (1:N)"
    
    ROOM_TYPE ||--|{ ROOM : "categorizes (1:N, Total)"
    
    BOOKING }|--|{ ROOM : "reserves (M:N)"
    BOOKING }|--|{ SERVICE : "uses (M:N)"
    
    BOOKING ||--|| BILL : "identifies (1:1 Weak Entity)"
    BILL ||--o{ PAYMENT : "settled by (1:N)"
    
    DEPARTMENT ||--|{ STAFF : "employs (1:N, Total Staff)"

    CUSTOMER {
        int customer_id PK
        string name
        string contact_no
        string email
    }

    CUSTOMER_ALTERNATE_NO {
        int customer_id FK
        string alternate_no PK
    }

    ROOM_TYPE {
        int room_type_id PK
        decimal price
        int capacity
    }

    ROOM {
        int room_id PK
        string room_no
        int floor
        int room_type_id FK
    }

    BOOKING {
        int booking_id PK
        int customer_id FK
        date check_in_date
        date check_out_date
        int duration_derived
    }

    SERVICE {
        int service_id PK
        string service_name
        decimal cost
    }

    BILL {
        int booking_id PK_FK
        int bill_id PK
        decimal total_amount
        date generation_date
    }

    PAYMENT {
        int payment_id PK
        decimal amount
        string payment_method
        date payment_date
    }

    DEPARTMENT {
        int department_id PK
        string name
    }

    STAFF {
        int staff_id PK
        string name
        string role
        string phone_no
        int department_id FK
    }
```

---

## 🎓 4. Rapid Oral Exam / Viva Voce Cheat Sheet

| Question | What the Examiner is Testing | Your Confident Answer |
| :--- | :--- | :--- |
| **"Why is `BILL` a Weak Entity?"** | Understanding of entity existence dependencies. | *"Because an invoice bill cannot exist in the real world without a booking transaction. If the booking is deleted, the bill loses all legal meaning and primary key context."* |
| **"Why is stay duration drawn with a dashed oval?"** | Knowledge of Chen attribute classifications. | *"Because duration is a **derived attribute**. It is computed dynamically at query time (`check_out_date - check_in_date`) to prevent data redundancy and update anomalies."* |
| **"How do you resolve the $M:N$ relationship between Booking and Room?"** | Understanding of Relational Decomposition. | *"Relational database engines cannot directly enforce $M:N$ foreign keys. We resolve this by introducing an associative junction entity (`Booking_Room`) with composite foreign keys."* |
| **"What does the double line on `ROOM` $\rightarrow$ `ROOM_TYPE` indicate?"** | Understanding of participation constraints. | *"It signifies **Total / Mandatory Participation**. Every room must belong to a room type; an unclassified room is illegal in the database schema."* |
| **"Why is `alternate_no` in a double oval?"** | First Normal Form (1NF) normalization theory. | *"It is a **multivalued attribute**. Storing comma-separated phone numbers violates 1NF atomicity, so Chen notation flags it with a double oval, decomposed into `Customer_Alternate_No`."* |
