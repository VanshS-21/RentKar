import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, within, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import * as fc from 'fast-check';
import FilterPanel from './FilterPanel';

afterEach(() => {
  cleanup();
});

describe('FilterPanel Property Tests', () => {
  const categories = [
    'Electronics',
    'Books',
    'Accessories',
    'Sports Equipment',
    'Musical Instruments',
    'Tools',
    'Other',
  ];

  const statuses = ['AVAILABLE', 'BORROWED', 'UNAVAILABLE'];

  /**
   * Feature: item-management, Property: Filter combinations
   * Validates: Requirements 5.1, 6.1
   * 
   * For any combination of category and status filters,
   * the filter change callback should be called with correct values
   */
  it('should handle filter combinations correctly', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.option(fc.constantFrom(...categories), { nil: null }),
        fc.option(fc.constantFrom(...statuses), { nil: null }),
        async (category, status) => {
          const onFilterChange = vi.fn();
          const onClearFilters = vi.fn();
          const user = userEvent.setup();
          
          const filters = { category: null, status: null };
          
          const { container, unmount } = render(
            <FilterPanel
              filters={filters}
              onFilterChange={onFilterChange}
              onClearFilters={onClearFilters}
            />
          );
          
          // Select category if provided
          if (category) {
            const categorySelect = within(container).getByLabelText('Category');
            await user.selectOptions(categorySelect, category);
            expect(onFilterChange).toHaveBeenCalledWith('category', category);
          }
          
          // Select status if provided
          if (status) {
            const statusSelect = within(container).getByLabelText('Status');
            await user.selectOptions(statusSelect, status);
            expect(onFilterChange).toHaveBeenCalledWith('status', status);
          }
          
          unmount();
        }
      ),
      { numRuns: 30 }
    );
  });

  /**
   * Feature: item-management, Property: Active filter count
   * Validates: Requirements 5.1, 6.1
   * 
   * For any combination of filters, the active filter count should be correct
   */
  it('should display correct active filter count', () => {
    fc.assert(
      fc.property(
        fc.option(fc.constantFrom(...categories), { nil: null }),
        fc.option(fc.constantFrom(...statuses), { nil: null }),
        (category, status) => {
          const onFilterChange = vi.fn();
          const onClearFilters = vi.fn();
          
          const filters = { category, status };
          const expectedCount = [category, status].filter(Boolean).length;
          
          const { unmount } = render(
            <FilterPanel
              filters={filters}
              onFilterChange={onFilterChange}
              onClearFilters={onClearFilters}
            />
          );
          
          if (expectedCount > 0) {
            const clearButton = screen.getByText(`Clear all (${expectedCount})`);
            expect(clearButton).toBeInTheDocument();
          } else {
            expect(screen.queryByText(/Clear all/)).not.toBeInTheDocument();
          }
          
          unmount();
        }
      ),
      { numRuns: 50 }
    );
  });

  /**
   * Feature: item-management, Property: Clear filters functionality
   * Validates: Requirements 5.1, 6.1
   * 
   * For any filter state with at least one active filter,
   * clicking clear should call the clear callback
   */
  it('should clear filters when clear button is clicked', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.constantFrom(...categories),
        fc.constantFrom(...statuses),
        async (category, status) => {
          const onFilterChange = vi.fn();
          const onClearFilters = vi.fn();
          const user = userEvent.setup();
          
          const filters = { category, status };
          
          const { container, unmount } = render(
            <FilterPanel
              filters={filters}
              onFilterChange={onFilterChange}
              onClearFilters={onClearFilters}
            />
          );
          
          const clearButton = within(container).getByText(/Clear all/);
          await user.click(clearButton);
          
          expect(onClearFilters).toHaveBeenCalled();
          
          unmount();
        }
      ),
      { numRuns: 20 }
    );
  });

  /**
   * Feature: item-management, Property: Filter reset to null
   * Validates: Requirements 5.1, 6.1
   * 
   * For any active filter, selecting "All" should reset it to null
   */
  it('should reset filter to null when selecting "All" option', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.constantFrom('category', 'status'),
        async (filterType) => {
          const onFilterChange = vi.fn();
          const onClearFilters = vi.fn();
          const user = userEvent.setup();
          
          const filters = {
            category: filterType === 'category' ? 'Electronics' : null,
            status: filterType === 'status' ? 'AVAILABLE' : null,
          };
          
          const { container, unmount } = render(
            <FilterPanel
              filters={filters}
              onFilterChange={onFilterChange}
              onClearFilters={onClearFilters}
            />
          );
          
          if (filterType === 'category') {
            const categorySelect = within(container).getByLabelText('Category');
            await user.selectOptions(categorySelect, '');
            expect(onFilterChange).toHaveBeenCalledWith('category', null);
          } else {
            const statusSelect = within(container).getByLabelText('Status');
            await user.selectOptions(statusSelect, '');
            expect(onFilterChange).toHaveBeenCalledWith('status', null);
          }
          
          unmount();
        }
      ),
      { numRuns: 20 }
    );
  });
});
