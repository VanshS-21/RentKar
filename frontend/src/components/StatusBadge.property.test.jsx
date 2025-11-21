import { describe, it, expect, afterEach } from 'vitest';
import { render, cleanup } from '@testing-library/react';
import * as fc from 'fast-check';
import StatusBadge from './StatusBadge';

afterEach(() => {
  cleanup();
});

describe('StatusBadge Property Tests', () => {
  /**
   * Feature: borrow-workflow, Property 22: Status badge rendering
   * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5
   * 
   * For any request status, the correct badge color and text should be displayed
   * PENDING=yellow, APPROVED=green, REJECTED=red, RETURNED=blue, COMPLETED=gray
   */
  it('should render correct color and text for each status', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED'),
        (status) => {
          const { container, unmount } = render(<StatusBadge status={status} />);
          
          const badge = container.querySelector('span');
          expect(badge).toBeTruthy();
          
          // Verify correct text is displayed
          const expectedText = {
            'PENDING': 'Pending',
            'APPROVED': 'Approved',
            'REJECTED': 'Rejected',
            'RETURNED': 'Returned',
            'COMPLETED': 'Completed'
          }[status];
          
          expect(badge.textContent).toBe(expectedText);
          
          // Verify correct color classes are applied
          const expectedColorClass = {
            'PENDING': 'bg-yellow-100',
            'APPROVED': 'bg-green-100',
            'REJECTED': 'bg-red-100',
            'RETURNED': 'bg-blue-100',
            'COMPLETED': 'bg-gray-100'
          }[status];
          
          expect(badge.className).toContain(expectedColorClass);
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 22: Status badge text color consistency
   * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5
   * 
   * For any request status, the text color should match the background color theme
   */
  it('should apply consistent text color with background', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED'),
        (status) => {
          const { container, unmount } = render(<StatusBadge status={status} />);
          
          const badge = container.querySelector('span');
          
          // Verify text color matches background theme
          const expectedTextColorClass = {
            'PENDING': 'text-yellow-800',
            'APPROVED': 'text-green-800',
            'REJECTED': 'text-red-800',
            'RETURNED': 'text-blue-800',
            'COMPLETED': 'text-gray-800'
          }[status];
          
          expect(badge.className).toContain(expectedTextColorClass);
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 22: Status badge border consistency
   * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5
   * 
   * For any request status, the border color should match the color theme
   */
  it('should apply consistent border color with theme', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED'),
        (status) => {
          const { container, unmount } = render(<StatusBadge status={status} />);
          
          const badge = container.querySelector('span');
          
          // Verify border color matches theme
          const expectedBorderColorClass = {
            'PENDING': 'border-yellow-300',
            'APPROVED': 'border-green-300',
            'REJECTED': 'border-red-300',
            'RETURNED': 'border-blue-300',
            'COMPLETED': 'border-gray-300'
          }[status];
          
          expect(badge.className).toContain(expectedBorderColorClass);
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });

  /**
   * Feature: borrow-workflow, Property 22: Status badge base styling
   * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5
   * 
   * For any request status, the badge should have consistent base styling
   */
  it('should apply consistent base styling for all statuses', () => {
    fc.assert(
      fc.property(
        fc.constantFrom('PENDING', 'APPROVED', 'REJECTED', 'RETURNED', 'COMPLETED'),
        (status) => {
          const { container, unmount } = render(<StatusBadge status={status} />);
          
          const badge = container.querySelector('span');
          
          // Verify base styling classes
          expect(badge.className).toContain('inline-flex');
          expect(badge.className).toContain('items-center');
          expect(badge.className).toContain('rounded-full');
          expect(badge.className).toContain('text-xs');
          expect(badge.className).toContain('font-medium');
          expect(badge.className).toContain('border');
          
          unmount();
        }
      ),
      { numRuns: 100 }
    );
  });
});
