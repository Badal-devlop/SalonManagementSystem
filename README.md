# Salon Management System 2.0 (Swing Edition)

Desktop Java Swing application with MVC architecture for managing salon customers, staff, services, appointments, payments, and invoices. Developed for Techno International New Town (TINT), Department of Information Technology.

## Requirements

- JDK 11 or newer (JDK 21 compatible)
- No external libraries or frameworks required (uses standard Java SE & Swing)

## Package & Architecture

- **Root Package**: `in.edu.tint.it.salon`
- **Architecture**: MVC (Model - View - Controller / Manager)
  - **Model (`model`)**: JavaBean / POJO domain entities (`Person`, `Customer`, `Staff`, `Admin`, `ServiceItem`, `Booking`, `Payment`).
  - **Interfaces (`interfaces`)**: Role-based contracts (`CustomerInt4Salon`, `StaffInt4Salon`, `AdminInt4Salon`).
  - **Managers (`manager`)**: Business logic & collection management with thread-safe `Vector<T>` (`CustomerManager`, `StaffManager`, `AdminManager`, `ServiceManager`, `BookingManager`, `PaymentManager`).
  - **View (`view`)**: Java Swing GUI screens (`LoginFrame`, `CustomerFrame`, `StaffFrame`, `AdminFrame`, `CustomerRegistrationDialog`, `InvoiceDialog`, `UIUtils`).
  - **Persistence**: Text file persistence in `data/` (`admins.txt`, `staff.txt`, `customers.txt`, `services.txt`, `bookings.txt`, `payments.txt`).

## How to Run

### Windows Batch File
Double click or execute `run.bat` from the project root:
```cmd
run.bat
```

### Windows PowerShell
From the project root:
```powershell
if (-not (Test-Path out)) { New-Item -ItemType Directory -Path out }
$sources = (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
javac -d out $sources
java -cp out in.edu.tint.it.salon.SalonApp
```

## Default Demo Accounts

| Role | Portal Selection | Username | Password |
|---|---|---|---|
| Admin | Admin | admin | admin123 |
| Staff (Hair Stylist) | Staff | staff1 | staff123 |
| Staff (Barber) | Staff | staff2 | staff123 |
| Staff (Beautician) | Staff | staff3 | staff123 |

*Customers can register new accounts directly from the login screen or use existing accounts.*

## Key Features

- **Customer Portal**:
  - Registration with input validations (10-digit phone, unique username, secure password).
  - Browse available services with pricing, duration, and specialization details.
  - Interactive appointment booking with automatic slot availability filtering (10:00 to 18:00).
  - Staff selection filtered by service qualification.
  - Demo payment via UPI, Card, or Cash.
  - Real-time invoice generation and printing.
  - Booking cancellation with automatic payment refunding.
  - Profile details & password management.

- **Staff Workspace**:
  - View assigned upcoming appointments.
  - Mark appointments as completed (validated against scheduled end time).
  - View completed work history and revenue handled.
  - Staff account security and password change.

- **Admin Console**:
  - Service management (Add, Edit, Deactivate, Reactivate).
  - Staff management (Add, Deactivate with booking protection, Reactivate).
  - View registered customers and detailed salon-wide bookings.
  - View transaction & payment logs.
  - Revenue analytics and staff performance reporting.
  - Administrator password management.
