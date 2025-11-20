# RentKar – Project Requirement Document (PRD)
End-Semester Project | PW IOI – School of Technology  
Domain: Web Development + AI  
Team Size: 3 Members  

---

## 1. Problem Statement
Students often require certain items for short durations—such as books, calculators, accessories, and small electronics.  
Purchasing these items becomes expensive and impractical for temporary needs.

There is currently no simple platform within the campus that enables students to borrow and lend items in an organized, trustworthy way.

A basic system is needed to:
- Allow students to list items
- Enable borrowers to request items
- Provide a simple approval and return workflow
- Maintain transparency between lenders and borrowers

---

## 2. Proposed Solution
RentKar is a minimal peer-to-peer item sharing platform for college students.

The platform enables users to:
- Create an account and log in
- List items available for lending
- Upload item images for clarity
- Browse items listed by others
- Send borrowing requests
- Approve or reject incoming requests
- Mark items as returned
- Use a lightweight AI feature to auto-generate item titles/descriptions
- Allow admin to remove inappropriate items (optional)

The system is simple, practical, and designed to be fully achievable within the semester.

---

## 3. Objectives and Expected Outcomes

### Primary Objectives
- Implement secure login and registration
- Allow users to list items with images
- Enable borrowers to send item requests
- Allow lenders to approve or reject requests
- Support a basic item return workflow
- Use an AI API to auto-generate item descriptions/titles
- Build a clean and functional user interface

### Secondary Objectives (Optional)
- Basic admin controls (remove items/users)
- Simple messaging or chat between users
- Item view counters or basic analytics

### Expected Outcomes
- A functional web application demonstrating full borrowing workflow
- AI-assisted item listing
- Clear lender–borrower interaction flow
- Proper documentation and presentation

---

## 4. Tools, Technologies, and Frameworks

### Frontend
- React
- TailwindCSS

### Backend
- Spring Boot
- Spring Web
- Spring Data JPA

### Database
- MySQL

### External Integrations
- Cloudinary (image storage)
- LLM API (for generating item titles/descriptions)

---

## 5. Tentative Milestones and Timeline (10–12 Weeks)

| Week | Milestone | Description |
|------|-----------|-------------|
| Week 1 | Project Planning | Finalize scope, roles, architecture draft |
| Week 2 | Setup & Authentication | Project setup, DB schema, login/register |
| Week 3 | Item Module – Part 1 | Item schema, backend CRUD, basic forms |
| Week 4 | Item Module – Part 2 | Image upload integration via Cloudinary |
| Week 5 | AI Integration | AI title/description generation endpoint |
| Week 6 | Borrow Workflow – Part 1 | Send request, manage status (Requested/Accepted) |
| Week 7 | Borrow Workflow – Part 2 | Return flow, Completed status, UI completion |
| Week 8 | Optional Features – Part 1 | Admin actions (remove items/users) |
| Week 9 | Optional Features – Part 2 | Basic chat/messaging (if implemented) |
| Week 10 | UI Polishing | Responsive UI, cleanup, UX improvements |
| Week 11 | Testing & Debugging | Functional testing, bug fixes, data prep |
| Week 12 | Documentation & PPT | Final PRD, final report, PPT, demo rehearsal |

---

