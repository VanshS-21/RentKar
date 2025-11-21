import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import * as fc from 'fast-check';
import Pagination from './Pagination';

describe('Pagination Property Tests', () => {
  /**
   * Feature: item-management, Property: Page navigation boundaries
   * Validates: Requirements 3.2
   * 
   * For any current page and total pages, the Previous button should be
   * disabled at the first page and Next button disabled at the last page
   */
  it('should handle page boundaries correctly', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 2, max: 100 }),
        fc.integer({ min: 0, max: 99 }),
        (totalPages, currentPage) => {
          // Ensure currentPage is within valid range
          const validCurrentPage = Math.min(currentPage, totalPages - 1);
          
          const onPageChange = vi.fn();
          
          const { unmount } = render(
            <Pagination
              currentPage={validCurrentPage}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          );
          
          const prevButton = screen.getByText('Previous');
          const nextButton = screen.getByText('Next');
          
          // Previous button should be disabled on first page
          if (validCurrentPage === 0) {
            expect(prevButton).toBeDisabled();
          } else {
            expect(prevButton).not.toBeDisabled();
          }
          
          // Next button should be disabled on last page
          if (validCurrentPage >= totalPages - 1) {
            expect(nextButton).toBeDisabled();
          } else {
            expect(nextButton).not.toBeDisabled();
          }
          
          unmount();
        }
      ),
      { numRuns: 50 }
    );
  });

  /**
   * Feature: item-management, Property: Page display
   * Validates: Requirements 3.2
   * 
   * For any current page and total pages, the page display should show
   * the correct page numbers (1-indexed for display)
   */
  it('should display correct page numbers', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 2, max: 100 }),
        fc.integer({ min: 0, max: 99 }),
        (totalPages, currentPage) => {
          // Ensure currentPage is within valid range
          const validCurrentPage = Math.min(currentPage, totalPages - 1);
          
          const onPageChange = vi.fn();
          
          const { unmount } = render(
            <Pagination
              currentPage={validCurrentPage}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          );
          
          const expectedText = `Page ${validCurrentPage + 1} of ${totalPages}`;
          expect(screen.getByText(expectedText)).toBeInTheDocument();
          
          unmount();
        }
      ),
      { numRuns: 50 }
    );
  });

  /**
   * Feature: item-management, Property: Previous button navigation
   * Validates: Requirements 3.2
   * 
   * For any page > 0, clicking Previous should navigate to previous page
   */
  it('should navigate to previous page when Previous is clicked', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.integer({ min: 2, max: 50 }),
        fc.integer({ min: 1, max: 49 }),
        async (totalPages, currentPage) => {
          // Ensure currentPage is within valid range and > 0
          const validCurrentPage = Math.min(Math.max(currentPage, 1), totalPages - 1);
          
          const onPageChange = vi.fn();
          const user = userEvent.setup();
          
          const { unmount } = render(
            <Pagination
              currentPage={validCurrentPage}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          );
          
          const prevButton = screen.getByText('Previous');
          await user.click(prevButton);
          
          expect(onPageChange).toHaveBeenCalledWith(validCurrentPage - 1);
          
          unmount();
        }
      ),
      { numRuns: 30 }
    );
  });

  /**
   * Feature: item-management, Property: Next button navigation
   * Validates: Requirements 3.2
   * 
   * For any page < totalPages - 1, clicking Next should navigate to next page
   */
  it('should navigate to next page when Next is clicked', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.integer({ min: 3, max: 50 }),
        fc.integer({ min: 0, max: 47 }),
        async (totalPages, currentPage) => {
          // Ensure currentPage is within valid range and < totalPages - 1
          const validCurrentPage = Math.min(currentPage, totalPages - 2);
          
          const onPageChange = vi.fn();
          const user = userEvent.setup();
          
          const { unmount } = render(
            <Pagination
              currentPage={validCurrentPage}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          );
          
          const nextButton = screen.getByText('Next');
          await user.click(nextButton);
          
          expect(onPageChange).toHaveBeenCalledWith(validCurrentPage + 1);
          
          unmount();
        }
      ),
      { numRuns: 30 }
    );
  });

  /**
   * Feature: item-management, Property: Jump to page functionality
   * Validates: Requirements 3.2
   * 
   * For any valid page number, jumping to that page should work correctly
   */
  it('should jump to valid page number', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.integer({ min: 5, max: 50 }),
        fc.integer({ min: 1, max: 50 }),
        async (totalPages, targetPage) => {
          // Ensure targetPage is within valid range
          const validTargetPage = Math.min(Math.max(targetPage, 1), totalPages);
          
          const onPageChange = vi.fn();
          const user = userEvent.setup();
          
          const { unmount } = render(
            <Pagination
              currentPage={0}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          );
          
          const jumpInput = screen.getByPlaceholderText('Page');
          const goButton = screen.getByText('Go');
          
          await user.type(jumpInput, validTargetPage.toString());
          await user.click(goButton);
          
          // Should navigate to 0-indexed page
          expect(onPageChange).toHaveBeenCalledWith(validTargetPage - 1);
          
          unmount();
        }
      ),
      { numRuns: 30 }
    );
  });

  /**
   * Feature: item-management, Property: No pagination for single page
   * Validates: Requirements 3.2
   * 
   * For totalPages <= 1, pagination should not be rendered
   */
  it('should not render pagination for single page or no pages', () => {
    fc.assert(
      fc.property(
        fc.constantFrom(0, 1),
        (totalPages) => {
          const onPageChange = vi.fn();
          
          const { container, unmount } = render(
            <Pagination
              currentPage={0}
              totalPages={totalPages}
              onPageChange={onPageChange}
            />
          );
          
          expect(container.firstChild).toBeNull();
          
          unmount();
        }
      ),
      { numRuns: 10 }
    );
  });
});
