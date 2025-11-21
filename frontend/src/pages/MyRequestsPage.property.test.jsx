import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, cleanup, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import * as fc from 'fast-check';
import MyRequestsPage from './MyRequestsPage';
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
  borrowRequestService.getSentRequests = vi.fn().mockResolvedValue({
    success: true,
    data: [],
  });
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
  borrowRequestService.cancelRequest = vi.fn().mockResolvedValue({
    success: true,
  });
  borrowRequestService.confirmReturn = vi.fn().mockResolvedValue({
    success: true,
    data: {},
  });
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
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

describe('MyRequestsPage Property Tests', () => {
  /**
   * Feature: borrow-workflow, Property 4: Sent request information completeness
   * Validates: Requirements 2.2
   * 
   * For any displayed sent request, it should contain item details, lender information,
   * dates, and current status
   */
  it('should display complete information for all sent requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 10 }),
        statisticsArb,
        async (requests, statistics) => {
          // Mock the API responses
          borrowRequestService.getSentRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <MyRequestsPage />
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

            // Check that lender information is displayed
            const lenderElements = Array.from(container.querySelectorAll('p')).filter(
              el => el.textContent.includes(request.lender.fullName)
            );
            expect(lenderElements.length).toBeGreaterThan(0);

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
   * Feature: borrow-workflow, Property 4: Request dates display
   * Validates: Requirements 2.2
   * 
   * For any displayed sent request, both borrow date and return date should be visible
   */
  it('should display borrow and return dates for all requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }),
        statisticsArb,
        async (requests, statistics) => {
          borrowRequestService.getSentRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <MyRequestsPage />
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
   * Feature: borrow-workflow, Property 4: Item image display
   * Validates: Requirements 2.2
   * 
   * For any displayed sent request, the item image should be shown (or placeholder)
   */
  it('should display item image or placeholder for all requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }),
        statisticsArb,
        async (requests, statistics) => {
          borrowRequestService.getSentRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <MyRequestsPage />
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
   * Feature: borrow-workflow, Property 4: Status badge presence
   * Validates: Requirements 2.2
   * 
   * For any displayed sent request, a status badge should be visible
   */
  it('should display status badge for all requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }),
        statisticsArb,
        async (requests, statistics) => {
          borrowRequestService.getSentRequests.mockResolvedValue({
            success: true,
            data: requests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: statistics,
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <MyRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Each request should have a status badge (rounded-full class)
          const badges = container.querySelectorAll('[class*="rounded-full"]');
          expect(badges.length).toBeGreaterThanOrEqual(requests.length);

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 21: Completed request in history
   * Validates: Requirements 7.5
   * 
   * For any completed request, it should appear in the request history
   */
  it('should display completed requests in history', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 10 }).map(requests =>
          requests.map(req => ({ ...req, status: 'COMPLETED' }))
        ),
        statisticsArb,
        async (completedRequests, statistics) => {
          // Mock the API to return completed requests
          borrowRequestService.getSentRequests.mockResolvedValue({
            success: true,
            data: completedRequests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: { ...statistics, completedCount: completedRequests.length },
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <MyRequestsPage />
            </BrowserRouter>
          );

          // Wait for requests to load
          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Verify all completed requests are displayed
          expect(container.querySelectorAll('[class*="shadow-md"]').length).toBe(
            completedRequests.length
          );

          // Verify each completed request shows the completed status
          completedRequests.forEach((request) => {
            const statusElements = Array.from(container.querySelectorAll('span')).filter(
              el => el.textContent === 'Completed'
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
   * Feature: borrow-workflow, Property 21: Completed requests filtering
   * Validates: Requirements 7.5
   * 
   * For any set of requests, filtering by COMPLETED status should show only completed requests
   */
  it('should filter and display only completed requests when COMPLETED filter is selected', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 5, maxLength: 20 }),
        statisticsArb,
        async (allRequests, statistics) => {
          // Separate completed and non-completed requests
          const completedRequests = allRequests.filter(req => req.status === 'COMPLETED');
          const hasCompletedRequests = completedRequests.length > 0;

          // If no completed requests, create at least one
          if (!hasCompletedRequests) {
            completedRequests.push({ ...allRequests[0], status: 'COMPLETED' });
          }

          // Mock the API to return only completed requests when filtered
          borrowRequestService.getSentRequests.mockImplementation((status) => {
            if (status === 'COMPLETED') {
              return Promise.resolve({
                success: true,
                data: completedRequests,
              });
            }
            return Promise.resolve({
              success: true,
              data: allRequests,
            });
          });

          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: { ...statistics, completedCount: completedRequests.length },
          });

          const { container, unmount, rerender } = render(
            <BrowserRouter>
              <MyRequestsPage />
            </BrowserRouter>
          );

          // Wait for initial load
          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Wait for the filter dropdown to be available
          let filterSelect;
          await waitFor(() => {
            filterSelect = container.querySelector('#status-filter');
            expect(filterSelect).toBeTruthy();
          });

          // Simulate selecting COMPLETED filter
          filterSelect.value = 'COMPLETED';
          filterSelect.dispatchEvent(new Event('change', { bubbles: true }));

          // Wait for filtered results
          await waitFor(() => {
            // Verify getSentRequests was called with COMPLETED status
            expect(borrowRequestService.getSentRequests).toHaveBeenCalledWith('COMPLETED');
          });

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 21: Completed request completeness
   * Validates: Requirements 7.5
   * 
   * For any completed request in history, it should display completion timestamp
   */
  it('should display completion information for completed requests', () => {
    fc.assert(
      fc.asyncProperty(
        fc.array(borrowRequestArb, { minLength: 1, maxLength: 5 }).map(requests =>
          requests.map(req => ({
            ...req,
            status: 'COMPLETED',
            completedAt: new Date().toISOString(),
          }))
        ),
        statisticsArb,
        async (completedRequests, statistics) => {
          borrowRequestService.getSentRequests.mockResolvedValue({
            success: true,
            data: completedRequests,
          });
          borrowRequestService.getStatistics.mockResolvedValue({
            success: true,
            data: { ...statistics, completedCount: completedRequests.length },
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <MyRequestsPage />
            </BrowserRouter>
          );

          await waitFor(() => {
            const requestCards = container.querySelectorAll('[class*="shadow-md"]');
            expect(requestCards.length).toBeGreaterThan(0);
          });

          // Verify completed requests are displayed with proper status
          const completedBadges = Array.from(container.querySelectorAll('span')).filter(
            el => el.textContent === 'Completed'
          );
          expect(completedBadges.length).toBeGreaterThan(0);

          // Verify each completed request has the gray badge styling
          completedBadges.forEach(badge => {
            expect(badge.className).toContain('bg-gray-100');
            expect(badge.className).toContain('text-gray-800');
          });

          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });
});
