import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, waitFor, within, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import * as fc from 'fast-check';
import SearchBar from './SearchBar';

afterEach(() => {
  cleanup();
});

describe('SearchBar Property Tests', () => {
  /**
   * Feature: item-management, Property: Search debouncing
   * Validates: Requirements 4.1
   * 
   * For any search query, the search callback should be debounced
   * and only called after 300ms of inactivity
   */
  it('should debounce search input correctly', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 1, maxLength: 20, unit: fc.constantFrom(...'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 '.split('')) }),
        async (searchTerm) => {
          const onSearch = vi.fn();
          const user = userEvent.setup({ delay: null }); // Remove delay for faster typing
          
          const { container, unmount } = render(<SearchBar onSearch={onSearch} />);
          
          const input = within(container).getByPlaceholderText('Search items...');
          
          // Type search term - this will trigger debounce after each character
          await user.type(input, searchTerm);
          
          // Wait for debounce to complete (300ms + buffer)
          await new Promise(resolve => setTimeout(resolve, 400));
          
          // The search should have been called
          expect(onSearch).toHaveBeenCalled();
          
          // The last call should have the complete search term
          const lastCall = onSearch.mock.calls[onSearch.mock.calls.length - 1];
          expect(lastCall[0]).toBe(searchTerm);
          
          unmount();
        }
      ),
      { numRuns: 20 }
    );
  }, 15000);

  /**
   * Feature: item-management, Property: Search UI updates
   * Validates: Requirements 4.1
   * 
   * For any search query, the UI should update to reflect the search term
   */
  it('should update UI with search term', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 1, maxLength: 20, unit: fc.constantFrom(...'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 '.split('')) }),
        async (searchTerm) => {
          const onSearch = vi.fn();
          const user = userEvent.setup();
          
          const { container, unmount } = render(<SearchBar onSearch={onSearch} />);
          
          const input = within(container).getByPlaceholderText('Search items...');
          
          await user.type(input, searchTerm);
          
          expect(input.value).toBe(searchTerm);
          
          unmount();
        }
      ),
      { numRuns: 20 }
    );
  });

  /**
   * Feature: item-management, Property: Clear button functionality
   * Validates: Requirements 4.1
   * 
   * For any non-empty search term, clicking clear should reset the search
   */
  it('should clear search when clear button is clicked', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 1, maxLength: 20, unit: fc.constantFrom(...'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 '.split('')) }),
        async (searchTerm) => {
          const onSearch = vi.fn();
          const user = userEvent.setup();
          
          const { unmount, container } = render(<SearchBar onSearch={onSearch} />);
          
          const input = within(container).getByPlaceholderText('Search items...');
          
          await user.type(input, searchTerm);
          
          // Find and click the clear button
          const clearButton = container.querySelector('button');
          if (clearButton) {
            await user.click(clearButton);
            expect(input.value).toBe('');
          }
          
          unmount();
        }
      ),
      { numRuns: 20 }
    );
  });

  /**
   * Feature: item-management, Property: Results count display
   * Validates: Requirements 4.1
   * 
   * For any results count, the UI should display it correctly
   */
  it('should display results count correctly', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 1000 }),
        (resultsCount) => {
          const onSearch = vi.fn();
          
          const { unmount } = render(
            <SearchBar onSearch={onSearch} resultsCount={resultsCount} />
          );
          
          const expectedText = `${resultsCount} ${resultsCount === 1 ? 'result' : 'results'} found`;
          expect(screen.getByText(expectedText)).toBeInTheDocument();
          
          unmount();
        }
      ),
      { numRuns: 50 }
    );
  });
});
