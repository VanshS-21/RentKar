import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import * as fc from 'fast-check';
import RequestDetailModal from './RequestDetailModal';

afterEach(() => {
  cleanup();
});

// Arbitraries for generating test data
const userArbitrary = fc.record({
  id: fc.integer({ min: 1, max: 1000 }),
  username: fc.stringMatching(/^[a-zA-Z0-9]{3,20}$/),
  email: fc.emailAddress(),
  fullName: fc.stringMatching(/^[a-zA-Z ]{5,50}$/),
  phone: fc.option(fc.stringMatching(/^[0-9]{10,15}$/), { nil: null }),
});

const itemArbitrary = fc.record({
  id: fc.integer({ min: 1, max: 1000 }),
  title: fc.stringMatching(/^[a-zA-Z0-9 ]{3,100}$/),
  description: fc.stringMatching(/^[a-zA-Z0-9 .,!?]{10,500}$/),
  category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
  imageUrl: fc.option(fc.webUrl(), { nil: null }),
  status: fc.constantFrom('AVAILABLE', 'BORROWED', 'UNAVAILABLE'),
  owner: userArbitrary,
});

const requestArbitrary = fc.record({
  id: fc.integer({ min: 1, max: 1000 }),
  item: itemArbitrary,
  borrower: userArbitrary,
  lender: userArbitrary,
  status: fc.constantFrom('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED'),
  requestMessage: fc.option(fc.stringMatching(/^[a-zA-Z0-9 .,!?]{10,500}$/), { nil: null }),
  responseMessage: fc.option(fc.stringMatching(/^[a-zA-Z0-9 .,!?]{10,500}$/), { nil: null }),
  borrowDate: fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString().split('T')[0];
  }),
  returnDate: fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString().split('T')[0];
  }),
  returnedAt: fc.option(fc.integer({ min: 0, max: 365 }), { nil: null }).map(days => {
    if (days === null) return null;
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }),
  completedAt: fc.option(fc.integer({ min: 0, max: 365 }), { nil: null }).map(days => {
    if (days === null) return null;
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }),
  createdAt: fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }),
  updatedAt: fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }),
});

describe('RequestDetailModal Property Tests', () => {
  /**
   * Feature: borrow-workflow, Property 26: Request detail completeness
   * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5
   * 
   * For any request detail view, it should display all status transitions, messages,
   * item information, and appropriate actions
   */
  it('should display all required request information', () => {
    fc.assert(
      fc.property(
        requestArbitrary,
        fc.constantFrom('sent', 'received'),
        (request, viewType) => {
          const onClose = vi.fn();
          const onAction = vi.fn();
          
          const { container, unmount } = render(
            <RequestDetailModal
              isOpen={true}
              onClose={onClose}
              request={request}
              viewType={viewType}
              onAction={onAction}
            />
          );
          
          // Verify item category is displayed (unique identifier)
          const categoryElements = Array.from(container.querySelectorAll('p')).filter(
            p => p.textContent === request.item.category
          );
          expect(categoryElements.length).toBeGreaterThan(0);
          
          // Verify status badge is displayed
          const statusBadge = container.querySelector('span.inline-flex');
          expect(statusBadge).toBeTruthy();
          expect(statusBadge.textContent).toBeTruthy();
          
          // Verify created timestamp heading is displayed
          const createdText = Array.from(container.querySelectorAll('p')).find(
            p => p.textContent.includes('Created')
          );
          expect(createdText).toBeTruthy();
          
          // Verify borrower and lender sections exist
          const borrowerHeading = Array.from(container.querySelectorAll('h3')).find(
            h => h.textContent === 'Borrower'
          );
          const lenderHeading = Array.from(container.querySelectorAll('h3')).find(
            h => h.textContent === 'Lender'
          );
          expect(borrowerHeading).toBeTruthy();
          expect(lenderHeading).toBeTruthy();
          
          unmount();
        }
      ),
      { numRuns: 50 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 26: Request messages display
   * Validates: Requirements 10.3
   * 
   * For any request with messages, both request and response messages should be displayed
   */
  it('should display request and response messages when present', () => {
    fc.assert(
      fc.property(
        requestArbitrary.filter(r => r.requestMessage !== null || r.responseMessage !== null),
        fc.constantFrom('sent', 'received'),
        (request, viewType) => {
          const onClose = vi.fn();
          const onAction = vi.fn();
          
          const { container, unmount } = render(
            <RequestDetailModal
              isOpen={true}
              onClose={onClose}
              request={request}
              viewType={viewType}
              onAction={onAction}
            />
          );
          
          // Verify request message heading is displayed if message present
          if (request.requestMessage) {
            const requestMessageHeadings = Array.from(container.querySelectorAll('h3')).filter(h => h.textContent === 'Request Message');
            expect(requestMessageHeadings.length).toBeGreaterThan(0);
            
            // Verify the message content exists in a blue background div
            const blueDiv = container.querySelector('.bg-blue-50');
            expect(blueDiv).toBeTruthy();
            expect(blueDiv.textContent.trim()).toBe(request.requestMessage.trim());
          }
          
          // Verify response message heading is displayed if message present
          if (request.responseMessage) {
            const responseMessageHeadings = Array.from(container.querySelectorAll('h3')).filter(h => h.textContent === 'Response Message');
            expect(responseMessageHeadings.length).toBeGreaterThan(0);
            
            // Verify the message content exists in a green background div
            const greenDiv = container.querySelector('.bg-green-50');
            expect(greenDiv).toBeTruthy();
            expect(greenDiv.textContent.trim()).toBe(request.responseMessage.trim());
          }
          
          unmount();
        }
      ),
      { numRuns: 50 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 26: Status history display
   * Validates: Requirements 10.2
   * 
   * For any request, status transitions with timestamps should be displayed
   */
  it('should display status history with timestamps', () => {
    fc.assert(
      fc.property(
        requestArbitrary,
        fc.constantFrom('sent', 'received'),
        (request, viewType) => {
          const onClose = vi.fn();
          const onAction = vi.fn();
          
          const { unmount } = render(
            <RequestDetailModal
              isOpen={true}
              onClose={onClose}
              request={request}
              viewType={viewType}
              onAction={onAction}
            />
          );
          
          // Verify status history section exists
          expect(screen.getByText('Status History')).toBeInTheDocument();
          
          // Verify created timestamp is shown
          expect(screen.getByText(/Created:/)).toBeInTheDocument();
          
          // Verify returned timestamp is shown if present
          if (request.returnedAt) {
            expect(screen.getByText(/Returned:/)).toBeInTheDocument();
          }
          
          // Verify completed timestamp is shown if present
          if (request.completedAt) {
            expect(screen.getByText(/Completed:/)).toBeInTheDocument();
          }
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 26: Contact information visibility
   * Validates: Requirements 10.5
   * 
   * For any approved request, contact information should be visible to the appropriate party
   */
  it('should display contact information for approved requests', () => {
    fc.assert(
      fc.property(
        requestArbitrary.filter(r => r.status === 'APPROVED'),
        fc.constantFrom('sent', 'received'),
        (request, viewType) => {
          const onClose = vi.fn();
          const onAction = vi.fn();
          
          // Ensure contact info exists for testing
          const requestWithContact = {
            ...request,
            borrower: { ...request.borrower, email: 'borrower@test.com', phone: '1234567890' },
            lender: { ...request.lender, email: 'lender@test.com', phone: '0987654321' },
          };
          
          const { unmount } = render(
            <RequestDetailModal
              isOpen={true}
              onClose={onClose}
              request={requestWithContact}
              viewType={viewType}
              onAction={onAction}
            />
          );
          
          // For sent view (borrower), lender contact should be visible
          if (viewType === 'sent') {
            expect(screen.getByText('lender@test.com')).toBeInTheDocument();
            expect(screen.getByText('0987654321')).toBeInTheDocument();
          }
          
          // For received view (lender), borrower contact should be visible
          if (viewType === 'received') {
            expect(screen.getByText('borrower@test.com')).toBeInTheDocument();
            expect(screen.getByText('1234567890')).toBeInTheDocument();
          }
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 26: Action buttons based on status and role
   * Validates: Requirements 10.5
   * 
   * For any request, appropriate action buttons should be displayed based on status and user role
   */
  it('should display appropriate action buttons based on status and role', () => {
    fc.assert(
      fc.property(
        requestArbitrary,
        fc.constantFrom('sent', 'received'),
        (request, viewType) => {
          const onClose = vi.fn();
          const onAction = vi.fn();
          
          const { container, unmount } = render(
            <RequestDetailModal
              isOpen={true}
              onClose={onClose}
              request={request}
              viewType={viewType}
              onAction={onAction}
            />
          );
          
          const buttons = container.querySelectorAll('button');
          
          // Verify action buttons exist based on status and view
          if (viewType === 'sent' && request.status === 'PENDING') {
            // Borrower should see cancel button for pending requests
            const cancelButton = Array.from(buttons).find(btn => btn.textContent.includes('Cancel Request'));
            expect(cancelButton).toBeTruthy();
          }
          
          if (viewType === 'sent' && request.status === 'RETURNED') {
            // Borrower should see confirm return button
            const confirmButton = Array.from(buttons).find(btn => btn.textContent.includes('Confirm Return'));
            expect(confirmButton).toBeTruthy();
          }
          
          if (viewType === 'received' && request.status === 'PENDING') {
            // Lender should see approve and reject buttons
            const approveButton = Array.from(buttons).find(btn => btn.textContent.includes('Approve'));
            const rejectButton = Array.from(buttons).find(btn => btn.textContent.includes('Reject'));
            expect(approveButton).toBeTruthy();
            expect(rejectButton).toBeTruthy();
          }
          
          if (viewType === 'received' && request.status === 'APPROVED') {
            // Lender should see mark as returned button
            const returnButton = Array.from(buttons).find(btn => btn.textContent.includes('Mark as Returned'));
            expect(returnButton).toBeTruthy();
          }
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });
});
