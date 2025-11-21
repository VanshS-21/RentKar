# RentKar Borrow Workflow Guide

## Table of Contents
1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Borrowing Items](#borrowing-items)
4. [Lending Items](#lending-items)
5. [Request Statuses](#request-statuses)
6. [Frequently Asked Questions](#frequently-asked-questions)

---

## Overview

The RentKar borrow workflow enables you to share items with other students in your campus community. Whether you're looking to borrow something you need temporarily or lend out items you're not using, our platform makes peer-to-peer sharing simple and secure.

### Key Benefits
- **Save Money**: Borrow instead of buying items you only need temporarily
- **Earn Karma**: Help fellow students by lending items you're not using
- **Build Community**: Connect with other students on campus
- **Track Everything**: Monitor all your transactions in one place

---

## Getting Started

### Prerequisites
- You must have a RentKar account (sign up at the registration page)
- You must be logged in to create or respond to borrow requests
- Items must be marked as "Available" to be borrowed

### Quick Start
1. **Browse Items**: Visit the home page to see available items
2. **Request to Borrow**: Click on an item and submit a borrow request
3. **Wait for Approval**: The item owner will review your request
4. **Complete the Transaction**: Return the item and confirm completion

---

## Borrowing Items

### How to Request an Item

#### Step 1: Find an Item
- Browse the item catalog on the home page
- Use search and filters to find what you need
- Click on an item to view its details

#### Step 2: Submit a Request
1. Click the **"Request to Borrow"** button on the item detail page
2. Fill out the request form:
   - **Borrow Date**: When you need the item (cannot be in the past)
   - **Return Date**: When you'll return it (must be after borrow date)
   - **Message** (optional): Explain why you need the item
3. Click **"Submit Request"**

**Tips for a Successful Request:**
- Be specific about why you need the item
- Choose realistic dates
- Be polite and professional in your message
- Provide context (e.g., "I have exams next week")

#### Step 3: Track Your Request
- Go to **"My Requests"** in the navigation menu
- View all your sent requests and their current status
- Filter by status to see pending, approved, or completed requests

### Managing Your Requests

#### Viewing Request Details
- Click on any request card to see full details
- View the lender's response message (if provided)
- See contact information once approved

#### Cancelling a Request
- You can cancel a request while it's still **PENDING**
- Click the **"Cancel"** button on the request
- Confirm the cancellation in the dialog
- The request will be permanently deleted

**Note**: You cannot cancel requests that have already been approved, rejected, or returned.

#### After Approval
Once your request is approved:
1. **Contact the Lender**: Use the provided contact information (email/phone)
2. **Arrange Pickup**: Coordinate a time and place to get the item
3. **Take Care of the Item**: Treat it as if it were your own
4. **Return on Time**: Respect the agreed return date

#### Confirming Return
After the lender marks the item as returned:
1. Go to **"My Requests"**
2. Find the request with status **RETURNED**
3. Click **"Confirm Return"**
4. The transaction will be marked as **COMPLETED**

---

## Lending Items

### How to Manage Incoming Requests

#### Step 1: View Incoming Requests
- Click **"Incoming Requests"** in the navigation menu
- See all requests for items you own
- A notification badge shows pending requests

#### Step 2: Review Request Details
- Click on a request to see full details
- Review the borrower's information
- Read their request message
- Check the requested dates

#### Step 3: Make a Decision

##### Approving a Request
1. Click the **"Approve"** button
2. Optionally add a response message (e.g., "Sure! Let's meet at the library")
3. Confirm the approval
4. The item status changes to **BORROWED**
5. Contact information is shared with the borrower

**What Happens After Approval:**
- The item is marked as borrowed and unavailable to others
- Your contact information is shared with the borrower
- The borrower can now arrange pickup with you

##### Rejecting a Request
1. Click the **"Reject"** button
2. Optionally add a reason (e.g., "Sorry, I need it during those dates")
3. Confirm the rejection
4. The item remains **AVAILABLE** for other requests

**When to Reject:**
- You need the item during the requested dates
- You're uncomfortable with the borrower's request
- The dates don't work for you
- Any other valid reason

#### Step 4: Mark as Returned
After the borrower returns your item:
1. Go to **"Incoming Requests"**
2. Find the approved request
3. Click **"Mark as Returned"**
4. The item becomes **AVAILABLE** again
5. Wait for the borrower to confirm

**Important**: Always inspect your item before marking it as returned to ensure it's in the expected condition.

### Best Practices for Lenders

#### Before Approving
- Check the borrower's profile and history
- Verify the dates work for your schedule
- Consider the value and condition of your item
- Trust your instincts

#### During the Loan
- Arrange a clear pickup time and location
- Document the item's condition (photos can help)
- Set clear expectations about care and return
- Keep communication open

#### After Return
- Inspect the item carefully
- Mark as returned promptly
- Provide feedback if needed

---

## Request Statuses

Understanding request statuses helps you track where each transaction stands in the workflow.

### Status Definitions

#### 🟡 PENDING
- **What it means**: Request is waiting for the lender's decision
- **Borrower actions**: Can cancel the request
- **Lender actions**: Can approve or reject the request
- **Duration**: Until lender responds

#### 🟢 APPROVED
- **What it means**: Lender has approved the request
- **Borrower actions**: Contact lender and arrange pickup
- **Lender actions**: Can mark as returned when item comes back
- **Item status**: Marked as BORROWED (unavailable to others)

#### 🔴 REJECTED
- **What it means**: Lender has declined the request
- **Borrower actions**: None (final status)
- **Lender actions**: None (final status)
- **Item status**: Remains AVAILABLE

#### 🔵 RETURNED
- **What it means**: Lender has marked the item as returned
- **Borrower actions**: Must confirm the return
- **Lender actions**: Wait for borrower confirmation
- **Item status**: Back to AVAILABLE

#### ⚪ COMPLETED
- **What it means**: Transaction is complete
- **Borrower actions**: None (final status)
- **Lender actions**: None (final status)
- **Item status**: AVAILABLE for new requests

### Status Flow Diagram

```
┌─────────┐
│ PENDING │ ──── Borrower cancels ──→ [Deleted]
└────┬────┘
     │
     ├──── Lender approves ──→ ┌──────────┐
     │                          │ APPROVED │
     │                          └────┬─────┘
     │                               │
     │                               │ Lender marks returned
     │                               ↓
     │                          ┌──────────┐
     │                          │ RETURNED │
     │                          └────┬─────┘
     │                               │
     │                               │ Borrower confirms
     │                               ↓
     │                          ┌───────────┐
     │                          │ COMPLETED │
     │                          └───────────┘
     │
     └──── Lender rejects ──→ ┌──────────┐
                               │ REJECTED │
                               └──────────┘
```

---

## Frequently Asked Questions

### General Questions

**Q: Can I borrow my own items?**  
A: No, you cannot create borrow requests for items you own.

**Q: Can I request an item that's currently borrowed?**  
A: No, items must have "Available" status to be requested. You can check back later when the item becomes available.

**Q: How many requests can I make?**  
A: There's no limit on the number of requests you can create, but be respectful and only request items you genuinely need.

**Q: Is there a fee to use RentKar?**  
A: No, RentKar is completely free for all students.

### Borrower Questions

**Q: What if the lender doesn't respond to my request?**  
A: You can cancel your pending request and try requesting a different item. We recommend waiting 24-48 hours before cancelling.

**Q: Can I extend my borrow period?**  
A: Not through the platform currently. Contact the lender directly to discuss extensions.

**Q: What if I damage the item?**  
A: Be honest with the lender and discuss how to resolve the situation. RentKar facilitates connections but doesn't mediate disputes.

**Q: Can I cancel an approved request?**  
A: No, once approved, you cannot cancel through the platform. Contact the lender directly if circumstances change.

**Q: When should I confirm return?**  
A: Confirm return only after you've physically returned the item to the lender and they've marked it as returned in the system.

### Lender Questions

**Q: Can I approve multiple requests for the same item?**  
A: No, once you approve a request, the item is marked as borrowed and other requests cannot be approved until it's returned.

**Q: What if the borrower doesn't return my item?**  
A: Contact the borrower using the provided contact information. If issues persist, contact campus security or administration.

**Q: Can I change my mind after approving?**  
A: Not through the platform. Contact the borrower directly if you need to cancel an approved request.

**Q: What if I reject a request by mistake?**  
A: Rejection is final in the system. The borrower would need to create a new request.

**Q: Do I have to provide a reason when rejecting?**  
A: No, the reason message is optional, but it's courteous to provide one.

### Technical Questions

**Q: Why can't I see the "Request to Borrow" button?**  
A: This could be because:
- You're not logged in
- You're the item owner
- The item is currently borrowed or unavailable

**Q: Where can I see my request history?**  
A: Go to "My Requests" to see all requests you've sent, or "Incoming Requests" to see requests for your items.

**Q: Can I filter my requests?**  
A: Yes, both "My Requests" and "Incoming Requests" pages have status filters.

**Q: How do I get notifications?**  
A: The platform shows a notification badge on "Incoming Requests" when you have pending requests. Check regularly for updates.

**Q: What if I encounter an error?**  
A: Try refreshing the page. If the problem persists, contact support or check your internet connection.

### Safety and Trust

**Q: How do I know if someone is trustworthy?**  
A: Check their profile, read their request message carefully, and trust your instincts. You're never obligated to approve a request.

**Q: Should I meet in public places?**  
A: Yes, we recommend meeting in public, well-lit areas on campus for item exchanges.

**Q: What information is shared?**  
A: Contact information (email and phone) is only shared after a request is approved. Your information is never shared for rejected or pending requests.

**Q: Can I report inappropriate behavior?**  
A: Yes, contact campus administration or platform administrators if you experience any issues.

---

## Need More Help?

### Contact Support
- **Email**: support@rentkar.edu
- **Campus Office**: Student Services Building, Room 201
- **Hours**: Monday-Friday, 9 AM - 5 PM

### Additional Resources
- [API Documentation](API_CONTRACT.md) - For developers
- [Setup Guide](SETUP_GUIDE.md) - Installation instructions
- [Project Status](PROJECT_STATUS.md) - Current features and roadmap

---

*Last Updated: November 21, 2025*
