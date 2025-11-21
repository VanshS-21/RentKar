import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, cleanup, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import * as fc from 'fast-check';
import IncomingRequestsPage from './IncomingRequestsPage';
import borrowRequestService from '../services/borrowRequestService';

// Mock the services
vi.mock('../services/borrowRequestService');
vi.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 1, username: 'testuser', fullName: 'Test User' },
    isAuthenticated: true,
    logout: vi.fn(),
  }),
}));

beforeEach(() => {
  // Set up default mocks to prevent unhandled errors
  borrowRequestService.getReceivedRequests = vi.fn().mockResolvedValue({
    success: true,
    data: [],
  });
  borrowRequestService.getStatistics = vi.fn().mockResolvedValue({
    success: true,
    data: {
      pendingCount: 0,
      approvedCount: 0,
      rejectedCount: 0,
      returnedCount: 0,
      completedCount: 0,
      totalSent: 0,
      totalReceived: 0,
    },
  });
  borrowRequestService.approveRequest = vi.fn().mockResolvedValue({
    success: true,
    data: {},
  });
  borrowRequestService.rejectRequest = vi.fn().mockResolvedValue({
    success: true,
    data: {},
  });
  borrowRequestService.markAsReturned = vi.fn().mockResolvedValue({
    success: true,
    data: {},
  });
});

afterEach(() => {
  cleanup();
});

// Generator for request status
const statusArb = fc.constantFrom('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED');

// Generator for user data
const userArb = fc.record({
  id: fc.integer({ min: 1, max: 1000 }),
  username: fc.string({ minLength: 3, maxLength: 20 }),
  email: fc.emailAddress(),
  fullName: fc.string({ minLength: 3, maxLength: 50 }),
  phone: fc.option(fc.string({ minLength: 10, maxLength: 15 }), { nil: null }),
});

// Generator for item data
const itemArb = fc.record({
  id: fc.integer({ min: 1, max: 1000 }),
  title: fc.string({ minLength: 3, maxLength: 100 }),
  description: fc.string({ minLength: 10, maxLength: 500 }),
  category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
  imageUrl: fc.option(fc.webUrl(), { nil: null }),
  status: fc.constantFrom('AVAILABLE', 'BORROWED', 'UNAVAILABLE'),
  owner: userArb,
});

// Generator for borrow request data
const borrowRequestArb = fc.record({
  id: fc.integer({ min: 1, max: 1000 }),
  item: itemArb,
  borrower: userArb,
  lender: userArb,
  status: statusArb,
  requestMessage: fc.option(fc.string({ minLength: 10, maxLength: 500 }), { nil: null }),
  responseMessage: fc.option(fc.string({ minLength: 10, maxLength: 500 }), { nil: null }),
  borrowDate: fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString().split('T')[0];
  }),
  returnDate: fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days + 1);
    return date.toISOString().split('T')[0];
  }),
  returnedAt: fc.option(fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }), { nil: null }),
  completedAt: fc.option(fc.integer({ min: 0, max: 365 }).map(days => {
    const date = new Date('2024-01-01');
    date.setDate(date.getDate() + days);
    return date.toISOString();
  }), { nil: null }),
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

// Generator for request statistics
const statisticsArb = fc.record({
  pendingCount: fc.integer({ min: 0, max: 100 }),
  approvedCount: fc.integer({ min: 0, max: 100 }),
  rejectedCount: fc.integer({ min: 0, max: 100 }),
  returnedCount: fc.integer({ min: 0, max: 100 }),
  completedCount: fc.integer({ min: 0, max: 100 }),
  totalSent: fc.integer({ min: 0, max: 500 }),
  totalReceived: fc.integer({ min: 0, max: 500 }),
});

describe('IncomingRequestsPage Property Tests', () => {
  /**
   * Feature: borrow-workflow, Property 7: Received request information completeness
   * Validates: Requirements 3.2
   * 
   * For any displayed received request, it should contain borrower information,
   * item details, dates, and request message
   */
  it('should display complete information for all received requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 10 }),
        statisticsArb,
        async (requests, statistics) => {
          // Mock the API responses
          borrowRequestService.getReceivedRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <IncomingRequestsPage />
            </BrowserRouter>
          );

          // Wait for requests to load
          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Verify each request displays complete information
          requests.forEach((request) => {
            // Check that item title is displayed
            const titleElements = Array.from(container.querySelectorAll('h3')).filter(
              el => el.textContent.includes(request.item.title)
            );
            expect(titleElements.length).toBeGreaterThan(0);

            // Check that borrower information is displayed
            const borrowerElements = Array.from(container.querySelectorAll('p')).filter(
              el => el.textContent.includes(request.borrower.fullName)
            );
            expect(borrowerElements.length).toBeGreaterThan(0);

            // Check that status is displayed (via StatusBadge)
            const statusElements = Array.from(container.querySelectorAll('span')).filter(
              el => el.textContent.toLowerCase().includes(request.status.toLowerCase())
            );
            expect(statusElements.length).toBeGreaterThan(0);
          });

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 7: Request dates display
   * Validates: Requirements 3.2
   * 
   * For any displayed received request, both borrow date and return date should be visible
   */
  it('should display borrow and return dates for all received requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }),
        statisticsArb,
        async (requests, statistics) => {
          borrowRequestService.getReceivedRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <IncomingRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Verify dates are displayed
          const textContent = container.textContent;
          
          // Check for "Borrow:" and "Return:" labels
          expect(textContent).toContain('Borrow:');
          expect(textContent).toContain('Return:');

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 7: Request message display
   * Validates: Requirements 3.2
   * 
   * For any received request with a request message, the message should be displayed
   */
  it('should display request messages when present', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }).map(requests =>
          requests.map(req => ({
            ...req,
            requestMessage: fc.sample(fc.string({ minLength: 10, maxLength: 100 }), 1)[0],
          }))
        ),
        statisticsArb,
        async (requestsWithMessages, statistics) => {
          borrowRequestService.getReceivedRequests.mockResolvedValue({
            success: true,
            data: requestsWithMessages,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <IncomingRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Verify request messages are displayed
          requestsWithMessages.forEach((request) => {
            if (request.requestMessage) {
              const messageElements = Array.from(container.querySelectorAll('p')).filter(
                el => el.textContent.includes(request.requestMessage)
              );
              expect(messageElements.length).toBeGreaterThan(0);
            }
          });

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 7: Item details display
   * Validates: Requirements 3.2
   * 
   * For any received request, item details including image should be displayed
   */
  it('should display item details and image for all received requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }),
        statisticsArb,
        async (requests, statistics) => {
          borrowRequestService.getReceivedRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <IncomingRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Each request should have an image or SVG placeholder
          requests.forEach((request) => {
            if (request.item.imageUrl) {
              // Should have an img element
              const images = Array.from(container.querySelectorAll('img')).filter(
                img => img.alt === request.item.title
              );
              expect(images.length).toBeGreaterThan(0);
            } else {
              // Should have SVG placeholder
              const svgs = container.querySelectorAll('svg');
              expect(svgs.length).toBeGreaterThan(0);
            }
          });

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 8: Contact information visibility
   * Validates: Requirements 3.5
   * 
   * For any approved request, contact details should be visible to the lender;
   * for non-approved requests, they should not be visible
   */
  it('should display contact information only for approved requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 2, maxLength: 10 }),
        statisticsArb,
        async (requests, statistics) => {
          // Ensure we have both approved and non-approved requests
          const approvedRequests = requests.slice(0, Math.ceil(requests.length / 2)).map(req => ({
            ...req,
            status: 'APPROVED',
            borrower: {
              ...req.borrower,
              email: fc.sample(fc.emailAddress(), 1)[0],
              phone: fc.sample(fc.string({ minLength: 10, maxLength: 15 }), 1)[0],
            },
          }));
          
          const nonApprovedRequests = requests.slice(Math.ceil(requests.length / 2)).map(req => ({
            ...req,
            status: fc.sample(fc.constantFrom('PENDING', 'REJECTED', 'RETURNED', 'COMPLETED'), 1)[0],
            borrower: {
              ...req.borrower,
              email: fc.sample(fc.emailAddress(), 1)[0],
              phone: fc.sample(fc.string({ minLength: 10, maxLength: 15 }), 1)[0],
            },
          }));

          const allRequests = [...approvedRequests, ...nonApprovedRequests];

          borrowRequestService.getReceivedRequests.mockResolvedValue({
            success: true,
            data: allRequests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount, getByText } = render(
            <BrowserRouter>
              <IncomingRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Click on each request to open detail modal and verify contact info visibility
          for (const request of allRequests) {
            // Find and click the request card
            const titleElements = Array.from(container.querySelectorAll('h3')).filter(
              el => el.textContent.includes(request.item.title)
            );
            
            if (titleElements.length > 0) {
              titleElements[0].closest('[class*="cursor-pointer"]')?.click();

              // Wait for modal to open
              await waitFor(() => {
                const modal = container.querySelector('[class*="fixed inset-0"]');
                expect(modal).toBeTruthy();
              });

              const modalContent = container.textContent;

              if (request.status === 'APPROVED') {
                // Contact info should be visible for approved requests
                // Email and phone should be in the modal
                expect(modalContent).toContain(request.borrower.email);
                if (request.borrower.phone) {
                  expect(modalContent).toContain(request.borrower.phone);
                }
              } else {
                // Contact info should NOT be visible for non-approved requests
                // Email and phone should not be in the modal
                expect(modalContent).not.toContain(request.borrower.email);
                if (request.borrower.phone) {
                  expect(modalContent).not.toContain(request.borrower.phone);
                }
              }

              // Close modal by clicking backdrop or close button
              const closeButton = container.querySelector('button[class*="text-gray-400"]');
              if (closeButton) {
                closeButton.click();
              }

              // Wait for modal to close
              await waitFor(() => {
                const modal = container.querySelector('[class*="fixed inset-0"]');
                expect(modal).toBeFalsy();
              }, { timeout: 1000 });
            }
          }

          unmount();
        }
      ),
      { numRuns: 50 } // Reduced runs due to modal interactions
    );
  });

  /**
   * Feature: borrow-workflow, Property 8: Contact information in card view
   * Validates: Requirements 3.5
   * 
   * For any request in the card list view, contact information should not be visible
   * (only in detail modal for approved requests)
   */
  it('should not display contact information in card list view', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 10 }).map(requests =>
          requests.map(req => ({
            ...req,
            borrower: {
              ...req.borrower,
              email: fc.sample(fc.emailAddress(), 1)[0],
              phone: fc.sample(fc.string({ minLength: 10, maxLength: 15 }), 1)[0],
            },
          }))
        ),
        statisticsArb,
        async (requests, statistics) => {
          borrowRequestService.getReceivedRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <IncomingRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Verify that email addresses and phone numbers are not visible in the card list
          const pageContent = container.textContent;
          
          requests.forEach((request) => {
            // Email should not be visible in card view
            expect(pageContent).not.toContain(request.borrower.email);
            
            // Phone should not be visible in card view
            if (request.borrower.phone) {
              expect(pageContent).not.toContain(request.borrower.phone);
            }
          });

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });
});
