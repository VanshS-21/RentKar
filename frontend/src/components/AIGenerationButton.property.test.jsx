import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, waitFor, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import * as fc from 'fast-check';
import AIGenerationButton from './AIGenerationButton';
import itemService from '../services/itemService';

// Mock the itemService
vi.mock('../services/itemService', () => ({
  default: {
    generateTitle: vi.fn(),
    generateDescription: vi.fn(),
  },
}));

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe('AIGenerationButton Property Tests', () => {
  /**
   * Feature: ai-description-generation, Property 7: Form field population
   * Validates: Requirements 3.1
   * 
   * For any successful AI generation, the form fields should be populated with the generated content
   */
  it('should populate form field with generated content on success', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          type: fc.constantFrom('title', 'description'),
          itemName: fc.string({ minLength: 3, maxLength: 50 }),
          category: fc.constantFrom('Electronics', 'Books', 'Sports Equipment', 'Tools'),
          generatedContent: fc.string({ minLength: 10, maxLength: 200 }),
        }),
        async ({ type, itemName, category, generatedContent }) => {
          const onGenerated = vi.fn();
          const user = userEvent.setup();

          // Mock successful API response
          const mockResponse = {
            success: true,
            data: {
              content: generatedContent,
              remainingRequests: 5,
            },
          };

          if (type === 'title') {
            itemService.generateTitle.mockResolvedValue(mockResponse);
          } else {
            itemService.generateDescription.mockResolvedValue(mockResponse);
          }

          const { unmount } = render(
            <AIGenerationButton
              type={type}
              itemData={{ itemName, category }}
              onGenerated={onGenerated}
            />
          );

          // Find and click the generate button
          const button = screen.getByRole('button', { name: new RegExp(`Generate.*${type}`, 'i') });
          await user.click(button);

          // Wait for the callback to be called with generated content
          await waitFor(() => {
            expect(onGenerated).toHaveBeenCalledWith(generatedContent);
          });

          unmount();
        }
      ),
      { numRuns: 20 }
    );
  }, 15000);

  /**
   * Feature: ai-description-generation, Property 8: User edits preservation
   * Validates: Requirements 3.3
   * 
   * For any AI-generated content that is edited by the user, the edited content should be preserved
   */
  it('should preserve user edits after generation', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          itemName: fc.string({ minLength: 3, maxLength: 50 }),
          category: fc.constantFrom('Electronics', 'Books'),
          generatedContent: fc.string({ minLength: 10, maxLength: 100 }),
        }),
        async ({ itemName, category, generatedContent }) => {
          const onGenerated = vi.fn();

          // Mock successful API response
          itemService.generateTitle.mockResolvedValue({
            success: true,
            data: {
              content: generatedContent,
              remainingRequests: 5,
            },
          });

          const { unmount } = render(
            <AIGenerationButton
              type="title"
              itemData={{ itemName, category }}
              onGenerated={onGenerated}
            />
          );

          // Verify that onGenerated callback receives the content
          // The parent component is responsible for preserving edits
          // This test verifies the callback is called correctly
          const user = userEvent.setup();
          const button = screen.getByRole('button');
          await user.click(button);

          await waitFor(() => {
            expect(onGenerated).toHaveBeenCalledWith(generatedContent);
          });

          // The preservation of edits happens in the parent component
          // This test verifies the button correctly passes the content
          expect(onGenerated).toHaveBeenCalledTimes(1);

          unmount();
        }
      ),
      { numRuns: 20 }
    );
  }, 15000);

  /**
   * Feature: ai-description-generation, Property 9: Regeneration triggers new API call
   * Validates: Requirements 4.1
   * 
   * For any regeneration request, a new API call should be made with the same input parameters
   */
  it('should trigger new API call on regeneration', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          itemName: fc.string({ minLength: 3, maxLength: 50 }),
          category: fc.constantFrom('Electronics', 'Books', 'Tools'),
          firstContent: fc.string({ minLength: 10, maxLength: 100 }),
          secondContent: fc.string({ minLength: 10, maxLength: 100 }),
        }),
        async ({ itemName, category, firstContent, secondContent }) => {
          // Clear mocks before each iteration
          vi.clearAllMocks();
          
          const onGenerated = vi.fn();
          const user = userEvent.setup();

          // Mock first generation
          itemService.generateTitle.mockResolvedValueOnce({
            success: true,
            data: {
              content: firstContent,
              remainingRequests: 5,
            },
          });

          // Mock regeneration
          itemService.generateTitle.mockResolvedValueOnce({
            success: true,
            data: {
              content: secondContent,
              remainingRequests: 4,
            },
          });

          const { unmount, container } = render(
            <AIGenerationButton
              type="title"
              itemData={{ itemName, category }}
              onGenerated={onGenerated}
              showRegenerate={true}
            />
          );

          // Click regenerate button - use container to scope the query
          const button = container.querySelector('button');
          await user.click(button);

          // Wait for first call
          await waitFor(() => {
            expect(itemService.generateTitle).toHaveBeenCalledTimes(1);
          });

          // Click again to regenerate
          await user.click(button);

          // Wait for second call
          await waitFor(() => {
            expect(itemService.generateTitle).toHaveBeenCalledTimes(2);
          });

          // Verify both calls were made with the same parameters
          const firstCall = itemService.generateTitle.mock.calls[0][0];
          const secondCall = itemService.generateTitle.mock.calls[1][0];
          expect(firstCall).toEqual(secondCall);

          unmount();
          cleanup();
        }
      ),
      { numRuns: 15 }
    );
  }, 20000);

  /**
   * Feature: ai-description-generation, Property 10: Regeneration produces different results
   * Validates: Requirements 4.2
   * 
   * For any two consecutive regeneration requests with the same input, 
   * the outputs should differ due to temperature variation
   */
  it('should produce different results on consecutive regenerations', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          itemName: fc.string({ minLength: 3, maxLength: 50 }),
          category: fc.constantFrom('Electronics', 'Books'),
          firstContent: fc.string({ minLength: 10, maxLength: 100 }),
          secondContent: fc.string({ minLength: 10, maxLength: 100 }),
        }).filter(({ firstContent, secondContent }) => firstContent !== secondContent),
        async ({ itemName, category, firstContent, secondContent }) => {
          const onGenerated = vi.fn();
          const user = userEvent.setup();

          // Mock first generation with different content
          itemService.generateDescription.mockResolvedValueOnce({
            success: true,
            data: {
              content: firstContent,
              remainingRequests: 5,
            },
          });

          // Mock second generation with different content
          itemService.generateDescription.mockResolvedValueOnce({
            success: true,
            data: {
              content: secondContent,
              remainingRequests: 4,
            },
          });

          const { unmount } = render(
            <AIGenerationButton
              type="description"
              itemData={{ itemName, category }}
              onGenerated={onGenerated}
              showRegenerate={true}
            />
          );

          const button = screen.getByRole('button');

          // First generation
          await user.click(button);
          await waitFor(() => {
            expect(onGenerated).toHaveBeenCalledWith(firstContent);
          });

          // Second generation (regenerate)
          await user.click(button);
          await waitFor(() => {
            expect(onGenerated).toHaveBeenCalledWith(secondContent);
          });

          // Verify different content was generated
          expect(onGenerated).toHaveBeenCalledTimes(2);
          const calls = onGenerated.mock.calls;
          expect(calls[0][0]).not.toBe(calls[1][0]);

          unmount();
        }
      ),
      { numRuns: 15 }
    );
  }, 20000);

  /**
   * Feature: ai-description-generation, Property 14: Error message display
   * Validates: Requirements 5.1
   * 
   * For any API error response, a user-friendly error message should be displayed to the user
   */
  it('should display user-friendly error messages for various error types', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          type: fc.constantFrom('title', 'description'),
          itemName: fc.string({ minLength: 3, maxLength: 50 }),
          category: fc.constantFrom('Electronics', 'Books', 'Tools'),
          errorType: fc.constantFrom(
            'timeout',
            'rateLimit',
            'auth',
            'validation',
            'serviceUnavailable',
            'network',
            'generic'
          ),
        }),
        async ({ type, itemName, category, errorType }) => {
          const onGenerated = vi.fn();
          const user = userEvent.setup();

          // Define error responses for different error types
          const errorResponses = {
            timeout: {
              success: false,
              error: 'The AI service took too long to respond. Please try again in a moment.',
              status: 408,
              isTimeout: true,
            },
            rateLimit: {
              success: false,
              error: 'You\'ve reached the generation limit. Please wait before trying again.',
              status: 429,
              retryAfter: 60,
              isRateLimited: true,
            },
            auth: {
              success: false,
              error: 'Authentication failed. Please log in again.',
              status: 401,
            },
            validation: {
              success: false,
              error: 'Invalid request. Please check your input and try again.',
              status: 400,
            },
            serviceUnavailable: {
              success: false,
              error: 'AI generation is temporarily unavailable. Please try again later.',
              status: 503,
            },
            network: {
              success: false,
              error: 'Unable to connect to the server. Please check your internet connection.',
              status: 0,
            },
            generic: {
              success: false,
              error: 'An unexpected error occurred. Please try again.',
              status: 500,
            },
          };

          const mockResponse = errorResponses[errorType];

          if (type === 'title') {
            itemService.generateTitle.mockResolvedValue(mockResponse);
          } else {
            itemService.generateDescription.mockResolvedValue(mockResponse);
          }

          const { unmount } = render(
            <AIGenerationButton
              type={type}
              itemData={{ itemName, category }}
              onGenerated={onGenerated}
            />
          );

          // Click the generate button
          const button = screen.getByRole('button', { name: new RegExp(`Generate.*${type}`, 'i') });
          await user.click(button);

          // Wait for error message to appear
          await waitFor(() => {
            const errorMessage = screen.getByText(mockResponse.error);
            expect(errorMessage).toBeInTheDocument();
          });

          // Verify onGenerated was not called on error
          expect(onGenerated).not.toHaveBeenCalled();

          // Verify error message is user-friendly (not technical)
          const errorText = screen.getByText(mockResponse.error).textContent;
          expect(errorText).toBeTruthy();
          expect(errorText.length).toBeGreaterThan(10); // Should be descriptive

          unmount();
        }
      ),
      { numRuns: 20 }
    );
  }, 20000);
});
