# Salon Management System 1.0

Console-based Java application for managing customers, staff, services, appointments and payments.

## Requirements

- JDK 11 or newer
- No external libraries

## Run on Windows PowerShell

Open the terminal in the project root (the folder containing `src`).

```powershell
javac -d .\out (Get-ChildItem -Path .\src\salon -Filter *.java | ForEach-Object { $_.FullName })
java -cp .\out salon.SalonApp
```

If your terminal is one folder above the project, first run:

```powershell
cd .\SalonManagementSystem
```

## Default demo accounts

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Staff | staff1 | staff123 |
| Staff | staff2 | staff123 |
| Staff | staff3 | staff123 |

Customers register themselves from the start screen.

Staff accounts are created by the Admin. Admin accounts are seeded by the application rather than publicly registered.

## Main features

### Customer
- Register and log in
- View active services and prices
- Book a service with an eligible staff member
- See available time slots
- Make a demo payment using UPI, Card or Cash
- View and cancel bookings
- View payment history
- Edit name/phone
- Change password

### Staff
- Log in
- View upcoming appointments
- Complete an appointment only after its scheduled end time
- View completed work
- Change password

### Admin
- Manage services
- Add, edit, deactivate and reactivate services
- Set service price, duration and required specialization
- Manage staff
- Add, deactivate and reactivate staff
- View customers
- View all bookings
- View all payments
- Revenue report
- Change password

## Business rules in 1.0

- A staff member cannot be double-booked.
- Multi-hour services block all overlapping hourly slots.
- Booking slots are available from 10:00 to 18:00, with the last start time depending on service duration.
- Bookings can be made up to 30 days ahead.
- A customer cannot choose staff whose specialization does not match the service.
- A booking stores its original price and duration, so later service edits do not change old bookings.
- Customers cannot cancel an appointment that has already started.
- Cancelled paid bookings are marked as refunded in the demo payment record.
- Staff cannot mark an appointment completed until its end time has passed.
- Staff/Admin are not publicly registered.
- Passwords are stored as SHA-256 hashes.
- Data is stored locally in a `data` folder.

## Data files

The application creates:

- `admins.txt`
- `staff.txt`
- `customers.txt`
- `services.txt`
- `bookings.txt`
- `payments.txt`

## Version

**1.0**
