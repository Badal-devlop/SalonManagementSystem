# Changelog - Salon Management System 1.0

## Major fixes

1. Added a proper `Payment` class and persistent payment records.
2. Added payment method and payment status.
3. Added booking-payment linkage.
4. Added service duration and overlap-aware staff scheduling.
5. Added service specialization requirements and eligible-staff filtering.
6. Added a Beautician demo staff member so Facial has an eligible staff member.
7. Fixed appointment completion so staff can complete an appointment only after its end time.
8. Fixed cancellation so already-started appointments cannot be cancelled.
9. Added refund status for cancelled paid demo bookings.
10. Added customer profile editing.
11. Added service reactivation.
12. Added staff reactivation.
13. Improved staff deactivation so only future booked appointments block deactivation.
14. Added admin payment viewing.
15. Improved service management with price, duration and specialization editing.
16. Made username login case-insensitive.
17. Added visible default Admin/Staff demo credentials to solve the initial-login problem.
18. Added PowerShell and Windows batch run instructions.
19. Added backward-compatible loading for the old 4-column service format.
20. Added automatic legacy payment records for existing non-cancelled bookings when upgrading from the earlier version.

## Deliberate design choice

Staff and Admin are not publicly registered. Customers can self-register. Admin creates Staff accounts, while the application seeds the initial Admin account and demo Staff accounts.
