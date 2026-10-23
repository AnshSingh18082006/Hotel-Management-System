package com.hotel;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

/**
 * HotelApp - Multi-Role Interactive Console Terminal for Hotel Management System
 * Supports Role-Based Access: Receptionist, Housekeeper, Cashier, General Manager, DBA
 */
public class HotelApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final RoomDAO roomDAO = new RoomDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();
    private static final BillingDAO billingDAO = new BillingDAO();
    private static final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("       GRAND PALACE HOTEL & RESIDENCES (RDBMS SYSTEM)       ");
        System.out.println("            Core Java JDBC Multi-Role Terminal              ");
        System.out.println("============================================================");

        verifyOrConfigureConnection();

        boolean appRunning = true;
        while (appRunning) {
            printRoleSelectionMenu();
            System.out.print("Select Terminal Role [1-6]: ");
            String roleChoice = scanner.nextLine().trim();

            switch (roleChoice) {
                case "1":
                    runReceptionistTerminal();
                    break;
                case "2":
                    runHousekeeperTerminal();
                    break;
                case "3":
                    runCashierTerminal();
                    break;
                case "4":
                    runGeneralManagerTerminal();
                    break;
                case "5":
                    runDBATerminal();
                    break;
                case "6":
                    System.out.println("\n[INFO] Exiting Grand Palace Hotel System. Goodbye!");
                    appRunning = false;
                    break;
                default:
                    System.out.println("[WARN] Invalid selection. Please enter a number between 1 and 6.");
            }
        }
    }

    private static void verifyOrConfigureConnection() {
        if (!DBConnection.testConnection()) {
            System.out.println("\n[!] Default MySQL connection failed.");
            System.out.print("Enter MySQL root password: ");
            String pass = scanner.nextLine().trim();
            DBConnection.setCredentials("root", pass);

            if (!DBConnection.testConnection()) {
                System.out.println("[ERROR] Could not connect to MySQL hotel_management_db with provided password.");
                System.out.println("Please check your credentials or start your MySQL server.");
                System.exit(1);
            } else {
                DBConnection.saveCredentialsToFile("root", pass);
                System.out.println("[SAVED] Password saved to db.properties. You won't have to enter it again!");
            }
        }
        System.out.println("[SUCCESS] Connected to MySQL: hotel_management_db (Port 3306)\n");
    }

    private static void printRoleSelectionMenu() {
        System.out.println("\n============================================================");
        System.out.println("         SELECT DEPARTMENT TERMINAL / USER ROLE             ");
        System.out.println("============================================================");
        System.out.println(" [1] 🏨 Receptionist Terminal       (reception_priya)");
        System.out.println(" [2] 🧹 Housekeeping Terminal       (hk_ramesh)");
        System.out.println(" [3] 💳 Billing & Cashier Terminal  (cashier_vikas)");
        System.out.println(" [4] 📊 General Manager Terminal    (manager_amit)");
        System.out.println(" [5] 🛡️ Database Administrator      (admin_ananya)");
        System.out.println(" [6] 🚪 Exit System");
        System.out.println("============================================================");
    }

    // =========================================================================
    // 1. RECEPTIONIST TERMINAL
    // =========================================================================
    private static void runReceptionistTerminal() {
        boolean inTerminal = true;
        while (inTerminal) {
            System.out.println("\n--- [RECEPTION DESK TERMINAL - Priya Sharma] ---");
            System.out.println(" [1] Check Room Availability (Call sp_CheckRoomAvailability)");
            System.out.println(" [2] Walk-In Guest Check-In & Room Allotment (ACID Tx)");
            System.out.println(" [3] Register Co-Occupant / Couple Govt ID (Capacity Trigger)");
            System.out.println(" [4] View Registered Customers (Multi-Contact 1NF)");
            System.out.println(" [5] View All Active Bookings (Lookup Booking ID)");
            System.out.println(" [6] Cancel Reservation & Process Refund (ACID Tx)");
            System.out.println(" [7] View Room Inventory");
            System.out.println(" [8] 🔙 Switch Role / Logout");
            System.out.print("Enter Choice [1-8]: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1": handleCheckAvailability(); break;
                case "2": handleWalkInCheckIn(); break;
                case "3": handleAddCoOccupant(); break;
                case "4": handleViewCustomers(); break;
                case "5": handleViewBookings(); break;
                case "6": handleCancelReservation(); break;
                case "7": handleViewRooms(); break;
                case "8": inTerminal = false; break;
                default: System.out.println("[WARN] Invalid option. Enter 1-8.");
            }
        }
    }

    // =========================================================================
    // 2. HOUSEKEEPING TERMINAL
    // =========================================================================
    private static void runHousekeeperTerminal() {
        boolean inTerminal = true;
        while (inTerminal) {
            System.out.println("\n--- [HOUSEKEEPING SUPERVISOR TERMINAL - Ramesh Kumar] ---");
            System.out.println(" [1] View Real-Time Room Inspection Status (Occupied vs Vacant)");
            System.out.println(" [2] View Housekeeping Staff List");
            System.out.println(" [3] 🔙 Switch Role / Logout");
            System.out.print("Enter Choice [1-3]: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    try {
                        List<AnalyticsDAO.RoomStatusRecord> list = analyticsDAO.getHousekeepingRoomList();
                        System.out.printf("\n%-10s %-8s %-20s %-25s\n", "Room No", "Floor", "Room Type", "Housekeeping Status");
                        System.out.println("------------------------------------------------------------------");
                        for (AnalyticsDAO.RoomStatusRecord r : list) {
                            System.out.printf("%-10s %-8d %-20s %-25s\n", r.roomNo, r.floor, r.typeName, r.status);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "2":
                    try {
                        List<AnalyticsDAO.StaffRecord> staff = analyticsDAO.getHousekeepingStaff();
                        System.out.printf("\n%-10s %-22s %-16s %-22s %-15s\n", "Staff ID", "Staff Name", "Department", "Role", "Phone No");
                        System.out.println("---------------------------------------------------------------------------------------------");
                        for (AnalyticsDAO.StaffRecord s : staff) {
                            System.out.printf("%-10d %-22s %-16s %-22s %-15s\n", s.staffId, s.name, s.deptName, s.role, s.phoneNo);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "3":
                    inTerminal = false;
                    break;
                default:
                    System.out.println("[WARN] Invalid option. Enter 1-3.");
            }
        }
    }

    // =========================================================================
    // 3. CASHIER TERMINAL
    // =========================================================================
    private static void runCashierTerminal() {
        boolean inTerminal = true;
        while (inTerminal) {
            System.out.println("\n--- [ACCOUNTS & CASHIER TERMINAL - Vikas Verma] ---");
            System.out.println(" [1] Checkout & Generate Final Tax Bill (Call sp_CalculateFinalBill)");
            System.out.println(" [2] Order Room Amenity / Service Charge (Auto-Increment Bill)");
            System.out.println(" [3] Process Booking Cancellation & Refund (ACID Tx)");
            System.out.println(" [4] View Payment Audit Log (Trigger trg_AuditPaymentLog)");
            System.out.println(" [5] View All Settled Tax Invoices");
            System.out.println(" [6] 🔙 Switch Role / Logout");
            System.out.print("Enter Choice [1-6]: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1": handleCheckoutAndBilling(); break;
                case "2": handleOrderAmenity(); break;
                case "3": handleCancelReservation(); break;
                case "4":
                    try {
                        List<AnalyticsDAO.AuditLogRecord> logs = analyticsDAO.getPaymentAuditLogs();
                        System.out.printf("\n%-8s %-12s %-15s %-15s %-25s\n", "Log ID", "Payment ID", "Amount", "Method", "Recorded At");
                        System.out.println("--------------------------------------------------------------------------------");
                        for (AnalyticsDAO.AuditLogRecord a : logs) {
                            System.out.printf("%-8d #%-11d Rs. %-11.2f %-15s %-25s\n", a.logId, a.paymentId, a.amount, a.method, a.recordedAt);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "5":
                    try {
                        analyticsDAO.printSettledBills();
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "6": inTerminal = false; break;
                default: System.out.println("[WARN] Invalid option. Enter 1-6.");
            }
        }
    }

    // =========================================================================
    // 4. GENERAL MANAGER TERMINAL
    // =========================================================================
    private static void runGeneralManagerTerminal() {
        boolean inTerminal = true;
        while (inTerminal) {
            System.out.println("\n--- [GENERAL MANAGER EXECUTIVE TERMINAL - Amit Patel] ---");
            System.out.println(" [1] Executive Performance Dashboard (Total Revenue & Metrics)");
            System.out.println(" [2] Customer Lifetime Stay History (Call sp_GetCustomerHistory)");
            System.out.println(" [3] View All Active Bookings");
            System.out.println(" [4] View All Customers & Contacts");
            System.out.println(" [5] 🔙 Switch Role / Logout");
            System.out.print("Enter Choice [1-5]: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    try {
                        AnalyticsDAO.ManagerKpis kpi = analyticsDAO.getExecutiveKPIs();
                        System.out.println("\n============================================================");
                        System.out.println("            HOTEL EXECUTIVE REVENUE DASHBOARD               ");
                        System.out.println("============================================================");
                        System.out.println(" Total Reservations Handled   : " + kpi.totalBookings);
                        System.out.println(" Total Registered Customers   : " + kpi.totalCustomers);
                        System.out.printf(" Total Advance Collected      : Rs. %.2f\n", kpi.totalAdvance);
                        System.out.printf(" Total Final Invoiced Revenue : Rs. %.2f\n", kpi.totalBilled);
                        System.out.println("============================================================");
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "2":
                    try {
                        System.out.print("Enter Customer ID to view history: ");
                        int cid = Integer.parseInt(scanner.nextLine().trim());
                        analyticsDAO.printCustomerHistory(cid);
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "3": handleViewBookings(); break;
                case "4": handleViewCustomers(); break;
                case "5": inTerminal = false; break;
                default: System.out.println("[WARN] Invalid option. Enter 1-5.");
            }
        }
    }

    // =========================================================================
    // 5. DATABASE ADMINISTRATOR (DBA) TERMINAL
    // =========================================================================
    private static void runDBATerminal() {
        boolean inTerminal = true;
        while (inTerminal) {
            System.out.println("\n--- [DATABASE ADMINISTRATOR TERMINAL - Ananya Sen] ---");
            System.out.println(" [1] Verify Active Business Rule Triggers");
            System.out.println(" [2] Database Table Health & Row Counts");
            System.out.println(" [3] View Payment Audit Log");
            System.out.println(" [4] 🔙 Switch Role / Logout");
            System.out.print("Enter Choice [1-4]: ");
            String ch = scanner.nextLine().trim();

            switch (ch) {
                case "1":
                    try {
                        List<AnalyticsDAO.TriggerRecord> triggers = analyticsDAO.getTriggers();
                        System.out.printf("\n%-35s %-10s %-20s %-10s\n", "Trigger Name", "Timing", "Table", "Event");
                        System.out.println("----------------------------------------------------------------------------------");
                        for (AnalyticsDAO.TriggerRecord t : triggers) {
                            System.out.printf("%-35s %-10s %-20s %-10s\n", t.name, t.timing, t.table, t.event);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "2":
                    try {
                        analyticsDAO.printTableHealth();
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "3":
                    try {
                        List<AnalyticsDAO.AuditLogRecord> logs = analyticsDAO.getPaymentAuditLogs();
                        System.out.printf("\n%-8s %-12s %-15s %-15s %-25s\n", "Log ID", "Payment ID", "Amount", "Method", "Recorded At");
                        System.out.println("--------------------------------------------------------------------------------");
                        for (AnalyticsDAO.AuditLogRecord a : logs) {
                            System.out.printf("%-8d #%-11d Rs. %-11.2f %-15s %-25s\n", a.logId, a.paymentId, a.amount, a.method, a.recordedAt);
                        }
                    } catch (Exception e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                    break;
                case "4": inTerminal = false; break;
                default: System.out.println("[WARN] Invalid option. Enter 1-4.");
            }
        }
    }

    // =========================================================================
    // COMMON HANDLERS
    // =========================================================================
    private static void handleCheckAvailability() {
        System.out.println("\n--- Check Room Availability ---");
        try {
            System.out.println("Room Types: 1=Single (Rs. 2,500), 2=Double (Rs. 4,500), 3=Executive (Rs. 7,500), 4=Suite (Rs. 12,000)");
            System.out.print("Select Room Type ID (1-4): ");
            int typeId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Check-In Date (YYYY-MM-DD): ");
            Date inDate = Date.valueOf(scanner.nextLine().trim());

            System.out.print("Enter Check-Out Date (YYYY-MM-DD): ");
            Date outDate = Date.valueOf(scanner.nextLine().trim());

            List<RoomDAO.RoomRecord> rooms = roomDAO.checkAvailableRooms(typeId, inDate, outDate);
            if (rooms.isEmpty()) {
                System.out.println("[RESULT] No vacant rooms found for Type " + typeId + " between " + inDate + " and " + outDate + ".");
            } else {
                System.out.printf("\n%-10s %-10s %-10s %-15s %-10s\n", "Room ID", "Room No", "Floor", "Price/Night", "Capacity");
                System.out.println("------------------------------------------------------------");
                for (RoomDAO.RoomRecord r : rooms) {
                    System.out.printf("%-10d %-10s %-10d Rs. %-10.2f %-10d\n", r.roomId, r.roomNo, r.floor, r.price, r.capacity);
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleWalkInCheckIn() {
        System.out.println("\n--- Walk-In Guest Check-In (ACID Transaction) ---");
        try {
            String name;
            while (true) {
                System.out.print("Enter Guest Full Name       : ");
                name = scanner.nextLine().trim();
                if (!name.isEmpty()) break;
                System.out.println("[ERROR] Guest name cannot be blank!");
            }

            String phone;
            while (true) {
                System.out.print("Enter Contact Number (Phone): ");
                phone = scanner.nextLine().trim();
                if (phone.matches("^[6-9]\\d{9}$")) {
                    break;
                }
                System.out.println("[ERROR] Invalid phone number! Must be exactly 10 digits starting with 6, 7, 8, or 9.");
            }

            String email;
            while (true) {
                System.out.print("Enter Email Address         : ");
                email = scanner.nextLine().trim();
                if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    break;
                }
                System.out.println("[ERROR] Invalid email format (e.g. guest@example.com). Please re-enter.");
            }

            String altPhone;
            while (true) {
                System.out.print("Enter Alternate Phone (opt) : ");
                altPhone = scanner.nextLine().trim();
                if (altPhone.isEmpty() || altPhone.matches("^[6-9]\\d{9}$")) {
                    break;
                }
                System.out.println("[ERROR] Invalid alternate phone! Must be exactly 10 digits or left empty.");
            }

            System.out.print("Enter Room ID to Allot      : ");
            int roomId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Check-In Date (YYYY-MM-DD) : ");
            Date inDate = Date.valueOf(scanner.nextLine().trim());

            System.out.print("Enter Check-Out Date (YYYY-MM-DD): ");
            Date outDate = Date.valueOf(scanner.nextLine().trim());

            System.out.print("Enter Advance Deposit (Rs)  : ");
            double advance = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Payment Method (UPI/Card/Cash): ");
            String method = scanner.nextLine().trim();

            System.out.println("\n[TRANSACTION] Registering customer and allotting room...");
            int customerId = customerDAO.registerCustomer(name, phone, email, altPhone);
            System.out.println(" -> Customer Profile Created (Customer ID: " + customerId + ")");

            BookingDAO.BookingResult result = bookingDAO.allotRoomToGuest(customerId, roomId, inDate, outDate, advance, method);
            if (result.success) {
                System.out.println("\n[SUCCESS] Reservation Confirmed & Committed to Database!");
                System.out.println(" -> Booking ID : " + result.bookingId);
                System.out.println(" -> Payment ID : " + result.paymentId + " (Rs. " + advance + " via " + method + ")");
                System.out.println(" -> Room ID    : " + roomId + " successfully locked.");
            } else {
                System.out.println("\n[TRANSACTION ABORTED] Database Rollback Triggered!");
                System.out.println("Reason: " + result.errorMessage);
            }

        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleOrderAmenity() {
        System.out.println("\n--- Order Room Amenity / Service ---");
        try {
            System.out.println("Services: 1=Breakfast Buffet (Rs. 500), 2=Airport Shuttle (Rs. 1,200), 3=Laundry (Rs. 400), 4=Spa (Rs. 2,500)");
            System.out.print("Enter Booking ID            : ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Service ID (1-4)      : ");
            int serviceId = Integer.parseInt(scanner.nextLine().trim());

            boolean ok = bookingDAO.orderAmenityService(bookingId, serviceId);
            if (ok) {
                System.out.println("\n[SUCCESS] Service ordered! Trigger trg_AutoUpdateBillOnService incremented the invoice subtotal.");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleCheckoutAndBilling() {
        System.out.println("\n--- Checkout & Final Bill Calculation ---");
        try {
            System.out.print("Enter Booking ID to checkout: ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            BillingDAO.InvoiceSummary inv = billingDAO.generateFinalBill(bookingId);
            if (inv == null) {
                System.out.println("[FAIL] No active booking or bill found for Booking #" + bookingId);
                return;
            }

            System.out.println("\n============================================================");
            System.out.println("              GRAND PALACE HOTEL & RESIDENCES               ");
            System.out.println("                FINAL SETTLEMENT TAX INVOICE                ");
            System.out.println("============================================================");
            System.out.println(" Booking ID         : #" + inv.bookingId);
            System.out.println(" Guest Full Name    : " + inv.customerName);
            System.out.println(" Contact Phone No   : " + inv.contactNo);
            System.out.println(" Email Address      : " + inv.email);
            System.out.println("------------------------------------------------------------");
            System.out.println(" Allotted Room      : Room " + inv.roomNo + " (" + inv.roomType + ", Floor " + inv.floor + ")");
            System.out.println(" Check-In Date      : " + inv.checkIn);
            System.out.println(" Check-Out Date     : " + inv.checkOut + " (" + inv.nights + " Night(s) Stay)");
            System.out.println("------------------------------------------------------------");
            System.out.printf(" Room Stay Subtotal : Rs. %.2f\n", inv.roomSubtotal);
            System.out.printf(" Amenities Subtotal : Rs. %.2f\n", inv.servicesSubtotal);
            System.out.println("------------------------------------------------------------");
            System.out.printf(" Final Billed Amount: Rs. %.2f (incl. 18%% GST)\n", inv.finalTotal);
            System.out.printf(" Advance Deposit    : Rs. %.2f\n", inv.advancePaid);
            System.out.println("------------------------------------------------------------");
            System.out.printf(" Net Balance Due    : Rs. %.2f\n", inv.balancePayable);
            System.out.println(" Settlement Status  : " + inv.status);
            System.out.println("============================================================");

        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleAddCoOccupant() {
        System.out.println("\n--- Register Co-Occupant / Couple Government ID ---");
        try {
            System.out.print("Enter Booking ID            : ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Occupant Full Name    : ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter ID Proof (Aadhaar/Pass): ");
            String idType = scanner.nextLine().trim();

            System.out.print("Enter ID Document Number    : ");
            String idNum = scanner.nextLine().trim();

            boolean ok = bookingDAO.addCoOccupant(bookingId, name, idType, idNum, false);
            if (ok) {
                System.out.println("\n[SUCCESS] Co-occupant government ID successfully recorded!");
            }
        } catch (Exception e) {
            System.out.println("\n[BLOCKED BY TRIGGER] " + e.getMessage());
        }
    }

    private static void handleViewCustomers() {
        System.out.println("\n--- Registered Customers (1NF Multivalued Contact Phone View) ---");
        try {
            List<CustomerDAO.CustomerRecord> list = customerDAO.getAllCustomers();
            System.out.printf("\n%-6s %-20s %-15s %-25s %-20s\n", "ID", "Name", "Primary Phone", "Email", "Alternate Numbers");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (CustomerDAO.CustomerRecord c : list) {
                System.out.printf("%-6d %-20s %-15s %-25s %-20s\n", c.id, c.name, c.contactNo, c.email, c.alternateNo);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleCancelReservation() {
        System.out.println("\n--- Booking Cancellation & Automated Refund Engine ---");
        try {
            System.out.print("Enter Booking ID to Cancel: ");
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            BookingDAO.CancellationPreview cp = bookingDAO.getCancellationPreview(bookingId);
            if (cp == null) {
                System.out.println("[FAIL] Booking #" + bookingId + " not found in system!");
                return;
            }

            if ("CANCELLED".equalsIgnoreCase(cp.status)) {
                System.out.println("\n[!] Notice: Booking #" + bookingId + " has already been cancelled.");
                return;
            }

            System.out.println("\n============================================================");
            System.out.println("               CANCELLATION & REFUND ASSESSMENT              ");
            System.out.println("============================================================");
            System.out.println(" Booking ID          : #" + cp.bookingId);
            System.out.println(" Guest Full Name     : " + cp.guestName);
            System.out.println(" Contact Phone       : " + cp.contactNo);
            System.out.println(" Allotted Room       : Room " + cp.roomNo);
            System.out.println(" Check-In Date       : " + cp.checkIn + " (in " + cp.daysUntilCheckIn + " day(s))");
            System.out.println(" Check-Out Date      : " + cp.checkOut);
            System.out.println("------------------------------------------------------------");
            System.out.printf(" Advance Deposit Paid: Rs. %.2f\n", cp.advancePaid);
            System.out.println(" Applicable Policy   : " + cp.policyName);
            System.out.printf(" Cancellation Charge : Rs. %.2f (%.0f%% retained)\n", cp.cancellationFee, (1.0 - cp.refundRate) * 100);
            System.out.println("------------------------------------------------------------");
            System.out.printf(" Net Refund Eligible : Rs. %.2f (%.0f%% refunded)\n", cp.refundAmount, cp.refundRate * 100);
            System.out.println("============================================================");

            System.out.print("\nConfirm Cancellation and Process Transaction? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("[INFO] Cancellation aborted. Reservation remains active.");
                return;
            }

            String refundMethod = "UPI";
            if (cp.refundAmount > 0) {
                System.out.print("Select Refund Method (UPI/Card/Cash) [Default: UPI]: ");
                String m = scanner.nextLine().trim();
                if (!m.isEmpty()) refundMethod = m;
            }

            System.out.println("\n[TRANSACTION] Processing cancellation and releasing room inventory...");
            boolean success = bookingDAO.executeCancellation(bookingId, cp.refundAmount, refundMethod);
            if (success) {
                System.out.println("\n============================================================");
                System.out.println("              CANCELLATION RECEIPT & AUDIT LOG              ");
                System.out.println("============================================================");
                System.out.println(" [SUCCESS] Booking #" + bookingId + " marked as CANCELLED.");
                System.out.println(" -> Room " + cp.roomNo + " has been freed and returned to VACANT pool.");
                if (cp.refundAmount > 0) {
                    System.out.printf(" -> Refund of Rs. %.2f successfully issued via REFUND_%s.\n", cp.refundAmount, refundMethod.toUpperCase());
                    System.out.println(" -> Audit entry automatically recorded in Payment_Audit_Log.");
                } else {
                    System.out.println(" -> No refund issued per non-refundable / late cancellation policy.");
                }
                System.out.println("============================================================");
            } else {
                System.out.println("[FAIL] Cancellation transaction failed. Database rolled back.");
            }

        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleViewBookings() {
        System.out.println("\n--- All Reservations & Booking Status ---");
        try {
            List<BookingDAO.BookingSummary> list = bookingDAO.getAllBookings();
            if (list.isEmpty()) {
                System.out.println("[INFO] No bookings recorded in the system yet.");
                return;
            }
            System.out.printf("\n%-12s %-20s %-15s %-10s %-12s %-12s %-15s %-12s\n", 
                "Booking ID", "Guest Name", "Contact No", "Room No", "Check-In", "Check-Out", "Advance Paid", "Status");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------");
            for (BookingDAO.BookingSummary b : list) {
                System.out.printf("%-12d %-20s %-15s %-10s %-12s %-12s Rs. %-11.2f %-12s\n", 
                    b.bookingId, b.customerName, b.contactNo, b.roomNo, b.checkIn, b.checkOut, b.advancePaid, b.status);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleViewRooms() {
        System.out.println("\n--- All Hotel Rooms Catalog ---");
        try {
            List<RoomDAO.RoomRecord> list = roomDAO.getAllRooms();
            System.out.printf("\n%-8s %-10s %-8s %-18s %-15s %-10s\n", "Room ID", "Room No", "Floor", "Room Type", "Price/Night", "Capacity");
            System.out.println("-----------------------------------------------------------------------------");
            for (RoomDAO.RoomRecord r : list) {
                System.out.printf("%-8d %-10s %-8d %-18s Rs. %-11.2f %-10d\n", r.roomId, r.roomNo, r.floor, r.typeName, r.price, r.capacity);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}
