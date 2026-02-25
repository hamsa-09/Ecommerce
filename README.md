# VertexSpace Server

Spring Boot backend for workspace resource management, bookings, waitlists, notifications, and desk assignments with role-based access (System Admin, Department Admin, User).

## Quick Start

```bash
# from repo root
./mvnw clean install
./mvnw spring-boot:run
```

Prereqs: JDK 17+, Maven Wrapper (included), PostgreSQL configured as per `src/main/resources/application.properties`.

## Key Features
- Auth & roles: System Admin, Department Admin, User.
- Resource management: buildings, floors, resources (desks/rooms), search.
- Booking: one-time/recurring, conflict checks, cancel (single/series), role-based cancellation rules.
- Waitlist: join by resource name, offers with provisional bookings, status checks with expiry handling.
- Desk assignments: assign/unassign desks (ASSIGNED mode), fetch assigned desks/users scoped by role.
- Notifications: stored + WebSocket push; fetch current user notifications.

## Useful Endpoints (authenticated unless noted)
- Auth: `/api/auth/**`
- Resources: `/api/resources/**`
- Bookings: `/api/bookings/**`
- Waitlist: `/api/waitlist/**`
- Desk assignments: `/api/desk-assignments/**`
- Notifications: `/api/notifications`

## Role Notes
- System Admin: full access.
- Department Admin: scoped to their department for bookings, desk assignments, and assigned-desk/user listings.
- Users: manage own bookings, view resources, join waitlists (not their own booking).

## Migration
- Run the sql scripts in `src/main/resources/db/migration` to set up the database schema and initial data.
