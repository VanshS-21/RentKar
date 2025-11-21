import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ItemDetailPage from './ItemDetailPage';
import itemService from '../services/itemService';
import borrowRequestService from '../services/borrowRequestService';
import * as fc from 'fast-check';

// Mock services
vi.mock('../services/itemService');
vi.mock('../services/borrowRequestService');
vi.mock('../components/Navigation', () => ({
  default: () => <div data-testid="navigation">Navigation</div>,
}));

// Mock react-router-dom hooks
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useParams: () => ({ id: '1' }),
    useNavigate: () => vi.fn(),
  };
});

// Mock useAuth hook
let mockUser = null;
vi.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: mockUser,
    login: vi.fn(),
    logout: vi.fn(),
    loading: false,
  }),
}));

const renderWithAuth = (user = null) => {
  mockUser = user;
  return render(
    <BrowserRouter>
      <ItemDetailPage />
    </BrowserRouter>
  );
};

describe('ItemDetailPage Property Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    borrowRequestService.getSentRequests.mockResolvedValue({
      success: true,
      data: [],
    });
  });

  afterEach(() => {
    cleanup();
  });

  /**
   * Feature: borrow-workflow, Property 27: Borrowed item display
   * Validates: Requirements 11.1
   * 
   * For any item with status BORROWED, the UI should display "Currently Borrowed" 
   * instead of "Request to Borrow"
   */
  it('Property 27: should display "Currently Borrowed" for any borrowed item', async () => {
    // Generate test data using fast-check
    const itemGenerator = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      title: fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
      description: fc.string({ minLength: 0, maxLength: 500 }),
      category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
      imageUrl: fc.option(fc.webUrl(), { nil: null }),
      owner: fc.record({
        id: fc.integer({ min: 1, max: 1000 }),
        username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
        fullName: fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
        email: fc.emailAddress(),
      }),
      createdAt: fc.constantFrom('2024-01-01T00:00:00.000Z', '2024-06-15T12:00:00.000Z', '2025-01-01T00:00:00.000Z'),
      updatedAt: fc.constantFrom('2024-01-01T00:00:00.000Z', '2024-06-15T12:00:00.000Z', '2025-01-01T00:00:00.000Z'),
    });

    // Test with multiple generated items
    const samples = fc.sample(itemGenerator, 50);
    
    for (const itemData of samples) {
      // Set item status to BORROWED
      const borrowedItem = {
        ...itemData,
        status: 'BORROWED',
      };

      itemService.getItemById.mockResolvedValue({
        success: true,
        data: borrowedItem,
      });

      // Mock empty sent requests
      borrowRequestService.getSentRequests.mockResolvedValue({
        success: true,
        data: [],
      });

      // User is not the owner
      const user = {
        id: itemData.owner.id + 1,
        username: 'testuser',
        email: 'test@example.com',
      };

      renderWithAuth(user);

      await waitFor(() => {
        expect(screen.getByText('Currently Borrowed')).toBeInTheDocument();
      }, { timeout: 2000 });

      expect(screen.queryByText('Request to Borrow')).not.toBeInTheDocument();
      
      cleanup();
    }
  }, 120000);

  /**
   * Feature: borrow-workflow, Property 28: Return date display
   * Validates: Requirements 11.2
   * 
   * For any borrowed item, the expected return date should be displayed
   */
  it('Property 28: should display return date for any borrowed item', async () => {
    // Use fixed valid dates to avoid date parsing issues
    const validDates = [
      { borrow: '2024-01-01', return: '2024-01-08' },
      { borrow: '2024-06-15', return: '2024-06-22' },
      { borrow: '2025-01-01', return: '2025-01-10' },
    ];

    // Generate test data using fast-check
    const itemWithDateGenerator = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      title: fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
      description: fc.string({ minLength: 0, maxLength: 500 }),
      category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
      imageUrl: fc.option(fc.webUrl(), { nil: null }),
      owner: fc.record({
        id: fc.integer({ min: 1, max: 1000 }),
        username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
        fullName: fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
        email: fc.emailAddress(),
      }),
      dates: fc.constantFrom(...validDates),
      createdAt: fc.constantFrom('2024-01-01T00:00:00.000Z', '2024-06-15T12:00:00.000Z'),
      updatedAt: fc.constantFrom('2024-01-01T00:00:00.000Z', '2024-06-15T12:00:00.000Z'),
    });

    // Test with multiple generated items
    const samples = fc.sample(itemWithDateGenerator, 50);
    
    for (const itemData of samples) {
      // Set item status to BORROWED with valid dates
      const borrowedItem = {
        ...itemData,
        status: 'BORROWED',
        borrowDate: itemData.dates.borrow,
        returnDate: itemData.dates.return,
      };

      itemService.getItemById.mockResolvedValue({
        success: true,
        data: borrowedItem,
      });

      // Mock empty sent requests
      borrowRequestService.getSentRequests.mockResolvedValue({
        success: true,
        data: [],
      });

      // User is not the owner
      const user = {
        id: itemData.owner.id + 1,
        username: 'testuser',
        email: 'test@example.com',
      };

      renderWithAuth(user);

      await waitFor(() => {
        expect(screen.getByText('Currently Borrowed')).toBeInTheDocument();
      }, { timeout: 2000 });

      // Check that return date is displayed
      const formattedReturnDate = new Date(itemData.dates.return).toLocaleDateString();
      await waitFor(() => {
        expect(screen.getByText(new RegExp(formattedReturnDate))).toBeInTheDocument();
      }, { timeout: 2000 });
      
      cleanup();
    }
  }, 120000);

  /**
   * Feature: borrow-workflow, Property 29: Borrowed item request prevention
   * Validates: Requirements 11.3
   * 
   * For any item with status BORROWED, new borrow requests should not be allowed
   */
  it('Property 29: should not allow borrow requests for any borrowed item', async () => {
    // Generate test data using fast-check
    const itemGenerator = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      title: fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
      description: fc.string({ minLength: 0, maxLength: 500 }),
      category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
      imageUrl: fc.option(fc.webUrl(), { nil: null }),
      owner: fc.record({
        id: fc.integer({ min: 1, max: 1000 }),
        username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
        fullName: fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
        email: fc.emailAddress(),
      }),
      createdAt: fc.constant('2024-01-01T00:00:00.000Z'),
      updatedAt: fc.constant('2024-01-01T00:00:00.000Z'),
    });

    // Test with multiple generated items
    const samples = fc.sample(itemGenerator, 100);
    
    for (const itemData of samples) {
      // Set item status to BORROWED
      const borrowedItem = {
        ...itemData,
        status: 'BORROWED',
      };

      itemService.getItemById.mockResolvedValue({
        success: true,
        data: borrowedItem,
      });

      // User is not the owner
      const user = {
        id: itemData.owner.id + 1,
        username: 'testuser',
        email: 'test@example.com',
      };

      renderWithAuth(user);

      await waitFor(() => {
        expect(screen.getByText('Currently Borrowed')).toBeInTheDocument();
      }, { timeout: 2000 });

      // Verify "Request to Borrow" button is NOT present
      expect(screen.queryByText('Request to Borrow')).not.toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /request to borrow/i })).not.toBeInTheDocument();
      
      cleanup();
    }
  }, 120000);

  /**
   * Feature: borrow-workflow, Property 30: Immediate availability after return
   * Validates: Requirements 11.4
   * 
   * For any item that is marked as returned, it should immediately show as available
   */
  it('Property 30: should show item as available immediately after return', async () => {
    // Generate test data using fast-check
    const itemGenerator = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      title: fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
      description: fc.string({ minLength: 0, maxLength: 500 }),
      category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
      imageUrl: fc.option(fc.webUrl(), { nil: null }),
      owner: fc.record({
        id: fc.integer({ min: 1, max: 1000 }),
        username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
        fullName: fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
        email: fc.emailAddress(),
      }),
      createdAt: fc.constant('2024-01-01T00:00:00.000Z'),
      updatedAt: fc.constant('2024-01-01T00:00:00.000Z'),
    });

    // Test with multiple generated items
    const samples = fc.sample(itemGenerator, 100);
    
    for (const itemData of samples) {
      // Set item status to AVAILABLE (after return)
      const availableItem = {
        ...itemData,
        status: 'AVAILABLE',
      };

      itemService.getItemById.mockResolvedValue({
        success: true,
        data: availableItem,
      });

      // User is not the owner
      const user = {
        id: itemData.owner.id + 1,
        username: 'testuser',
        email: 'test@example.com',
      };

      renderWithAuth(user);

      await waitFor(() => {
        expect(screen.getByText('Available')).toBeInTheDocument();
      }, { timeout: 2000 });

      // Verify "Request to Borrow" button IS present for available items
      await waitFor(() => {
        expect(screen.getByText('Request to Borrow')).toBeInTheDocument();
      }, { timeout: 2000 });
      
      cleanup();
    }
  }, 120000);

  /**
   * Feature: borrow-workflow, Property 31: Borrower information privacy
   * Validates: Requirements 11.5
   * 
   * For any borrowed item, borrower information should only be visible to the lender,
   * not to other users
   */
  it('Property 31: should not display borrower information to non-lender users', async () => {
    // Generate test data using fast-check
    const itemGenerator = fc.record({
      id: fc.integer({ min: 1, max: 10000 }),
      title: fc.string({ minLength: 1, maxLength: 100 }).filter(s => s.trim().length > 0),
      description: fc.string({ minLength: 0, maxLength: 500 }),
      category: fc.constantFrom('Electronics', 'Books', 'Tools', 'Sports', 'Other'),
      imageUrl: fc.option(fc.webUrl(), { nil: null }),
      owner: fc.record({
        id: fc.integer({ min: 1, max: 1000 }),
        username: fc.string({ minLength: 3, maxLength: 20 }).filter(s => s.trim().length >= 3),
        fullName: fc.string({ minLength: 1, maxLength: 50 }).filter(s => s.trim().length > 0),
        email: fc.emailAddress(),
      }),
      createdAt: fc.constant('2024-01-01T00:00:00.000Z'),
      updatedAt: fc.constant('2024-01-01T00:00:00.000Z'),
    });

    // Test with multiple generated items
    const samples = fc.sample(itemGenerator, 100);
    
    for (const itemData of samples) {
      // Set item status to BORROWED
      const borrowedItem = {
        ...itemData,
        status: 'BORROWED',
        borrower: {
          id: 9999,
          username: 'borrower123',
          fullName: 'Test Borrower',
          email: 'borrower@test.com',
        },
      };

      itemService.getItemById.mockResolvedValue({
        success: true,
        data: borrowedItem,
      });

      // User is NOT the owner (different user viewing the item)
      const user = {
        id: itemData.owner.id + 1,
        username: 'otheruser',
        email: 'other@example.com',
      };

      renderWithAuth(user);

      await waitFor(() => {
        expect(screen.getByText('Currently Borrowed')).toBeInTheDocument();
      }, { timeout: 2000 });

      // Verify borrower information is NOT displayed
      expect(screen.queryByText('borrower123')).not.toBeInTheDocument();
      expect(screen.queryByText('Test Borrower')).not.toBeInTheDocument();
      expect(screen.queryByText('borrower@test.com')).not.toBeInTheDocument();
      
      cleanup();
    }
  }, 120000);
});
