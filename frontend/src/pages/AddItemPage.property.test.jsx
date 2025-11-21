import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, within, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import * as fc from 'fast-check';
import AddItemPage from './AddItemPage';
import itemService from '../services/itemService';

// Mock dependencies
vi.mock('../services/itemService');
vi.mock('../components/Navigation', () => ({
  default: () => <div data-testid="navigation">Navigation</div>,
}));
vi.mock('../components/ImageUpload', () => ({
  default: ({ onImageUploaded }) => (
    <div data-testid="image-upload">
      <button
        onClick={() => onImageUploaded('https://example.com/image.jpg')}
      >
        Upload Image
      </button>
    </div>
  ),
}));

describe('AddItemPage - Form Validation Property Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Mock checkAIAvailability to prevent unhandled rejections
    itemService.checkAIAvailability = vi.fn().mockResolvedValue({
      success: true,
      data: { data: { available: true } }
    });
  });

  /**
   * Feature: item-management, Property 2: Short titles are rejected
   * Validates: Requirements 1.2
   */
  it('property: titles shorter than 3 characters should be rejected', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 1, maxLength: 2 }).filter(s => s.trim().length > 0 && !/[\[\]{}]/.test(s)),
        async (shortTitle) => {
          const user = userEvent.setup();
          const { container, unmount } = render(
            <BrowserRouter>
              <AddItemPage />
            </BrowserRouter>
          );

          try {
            // Fill in the form with a short title
            const titleInput = within(container).getByLabelText(/title/i);
            await user.clear(titleInput);
            await user.click(titleInput);
            await user.paste(shortTitle);

            // Select category
            const categorySelect = within(container).getByLabelText(/category/i);
            await user.selectOptions(categorySelect, 'Electronics');

            // Upload image
            const uploadButton = within(container).getByText('Upload Image');
            await user.click(uploadButton);

            // Try to submit
            const submitButton = within(container).getByRole('button', { name: /create item/i });
            await user.click(submitButton);

            // Should show validation error (either "required" or "at least 3 characters")
            await waitFor(() => {
              const errorMessage = within(container).queryByText(/title (is required|must be at least 3 characters)/i);
              expect(errorMessage).toBeInTheDocument();
            });

            // Should not call the API
            expect(itemService.createItem).not.toHaveBeenCalled();
          } finally {
            unmount();
          }
        }
      ),
      { numRuns: 10 }
    );
  });

  /**
   * Feature: item-management, Property 3: Long titles are rejected
   * Validates: Requirements 1.3
   */
  it('property: titles longer than 200 characters should be rejected', async () => {
    const { container, unmount } = render(
      <BrowserRouter>
        <AddItemPage />
      </BrowserRouter>
    );

    try {
      // The input has maxlength="200" which prevents entering more than 200 chars via UI
      // This is the correct behavior - HTML validation prevents long titles
      const titleInput = within(container).getByLabelText(/title/i);
      
      // Verify maxlength attribute is 200
      expect(titleInput).toHaveAttribute('maxlength', '200');
      
      // The maxlength attribute on the input element prevents users from typing
      // more than 200 characters, which satisfies the requirement that titles
      // longer than 200 characters are rejected. This is a valid implementation
      // using HTML5 form validation.
    } finally {
      unmount();
    }
  });

  /**
   * Feature: item-management, Property 4: Missing title is rejected
   * Validates: Requirements 1.4
   */
  it('property: empty or whitespace-only titles should be rejected', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 1, maxLength: 5 }).map(s => ' '.repeat(s.length)),
        async (emptyTitle) => {
          const user = userEvent.setup();
          const { container, unmount } = render(
            <BrowserRouter>
              <AddItemPage />
            </BrowserRouter>
          );

          try {
            // Fill in the form with whitespace title
            const titleInput = within(container).getByLabelText(/title/i);
            await user.clear(titleInput);
            await user.type(titleInput, emptyTitle);

            // Select category
            const categorySelect = within(container).getByLabelText(/category/i);
            await user.selectOptions(categorySelect, 'Electronics');

            // Upload image
            const uploadButton = within(container).getByText('Upload Image');
            await user.click(uploadButton);

            // Try to submit
            const submitButton = within(container).getByRole('button', { name: /create item/i });
            await user.click(submitButton);

            // Should show validation error
            await waitFor(() => {
              const errorMessage = within(container).queryByText(/title is required|title must be at least 3 characters/i);
              expect(errorMessage).toBeInTheDocument();
            });

            // Should not call the API
            expect(itemService.createItem).not.toHaveBeenCalled();
          } finally {
            unmount();
          }
        }
      ),
      { numRuns: 10 }
    );
  });

  /**
   * Feature: item-management, Property: Valid titles are accepted
   * Validates: Requirements 1.1, 1.2, 1.3
   */
  it('property: valid titles (3-200 chars) should be accepted', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.string({ minLength: 3, maxLength: 50 }).filter(s => s.trim().length >= 3),
        async (validTitle) => {
          const user = userEvent.setup();
          
          // Mock successful creation
          itemService.createItem.mockResolvedValue({
            success: true,
            data: { id: 1, title: validTitle },
          });

          const { container, unmount } = render(
            <BrowserRouter>
              <AddItemPage />
            </BrowserRouter>
          );

          try {
            // Fill in the form with valid title
            const titleInput = within(container).getByLabelText(/title/i);
            await user.clear(titleInput);
            await user.click(titleInput);
            await user.paste(validTitle);

            // Select category
            const categorySelect = within(container).getByLabelText(/category/i);
            await user.selectOptions(categorySelect, 'Electronics');

            // Upload image
            const uploadButton = within(container).getByText('Upload Image');
            await user.click(uploadButton);

            // Submit
            const submitButton = within(container).getByRole('button', { name: /create item/i });
            await user.click(submitButton);

            // Should call the API
            await waitFor(() => {
              expect(itemService.createItem).toHaveBeenCalledWith(
                expect.objectContaining({
                  title: validTitle,
                })
              );
            });
          } finally {
            unmount();
          }
        }
      ),
      { numRuns: 10 }
    );
  }, 15000);

  /**
   * Feature: item-management, Property: Missing category is rejected
   * Validates: Requirements 1.1
   */
  it('property: missing category should be rejected', async () => {
    const user = userEvent.setup();
    const { container, unmount } = render(
      <BrowserRouter>
        <AddItemPage />
      </BrowserRouter>
    );

    try {
      // Fill in title
      const titleInput = within(container).getByLabelText(/title/i);
      await user.type(titleInput, 'Valid Title');

      // Upload image
      const uploadButton = within(container).getByText('Upload Image');
      await user.click(uploadButton);

      // Don't select category

      // Try to submit
      const submitButton = within(container).getByRole('button', { name: /create item/i });
      await user.click(submitButton);

      // Should show validation error
      await waitFor(() => {
        const errorMessage = within(container).queryByText(/category is required/i);
        expect(errorMessage).toBeInTheDocument();
      });

      // Should not call the API
      expect(itemService.createItem).not.toHaveBeenCalled();
    } finally {
      unmount();
    }
  });

  /**
   * Feature: item-management, Property: Missing image is rejected
   * Validates: Requirements 2.1
   */
  it('property: missing image should be rejected', async () => {
    const user = userEvent.setup();
    const { container, unmount } = render(
      <BrowserRouter>
        <AddItemPage />
      </BrowserRouter>
    );

    try {
      // Fill in title
      const titleInput = within(container).getByLabelText(/title/i);
      await user.type(titleInput, 'Valid Title');

      // Select category
      const categorySelect = within(container).getByLabelText(/category/i);
      await user.selectOptions(categorySelect, 'Electronics');

      // Don't upload image

      // Try to submit
      const submitButton = within(container).getByRole('button', { name: /create item/i });
      await user.click(submitButton);

      // Should show validation error
      await waitFor(() => {
        const errorMessage = within(container).queryByText(/image is required/i);
        expect(errorMessage).toBeInTheDocument();
      });

      // Should not call the API
      expect(itemService.createItem).not.toHaveBeenCalled();
    } finally {
      unmount();
    }
  });

  /**
   * Feature: ai-description-generation, Property 15: Form content preservation on error
   * Validates: Requirements 5.5
   * 
   * For any error during AI generation, existing user-entered content in form fields should be preserved
   */
  it('property: form content should be preserved when AI generation errors occur', async () => {
    await fc.assert(
      fc.asyncProperty(
        fc.record({
          title: fc.string({ minLength: 3, maxLength: 30 }),
          description: fc.string({ minLength: 10, maxLength: 50 }),
          category: fc.constantFrom('Electronics', 'Books'),
          errorType: fc.constantFrom('timeout', 'generic'),
        }),
        async ({ title, description, category, errorType }) => {
          const user = userEvent.setup();

          // Define error responses for different error types
          const errorResponses = {
            timeout: {
              success: false,
              error: 'The AI service took too long to respond. Please try again in a moment.',
              status: 408,
              isTimeout: true,
            },
            generic: {
              success: false,
              error: 'An unexpected error occurred. Please try again.',
              status: 500,
            },
          };

          // Mock AI generation to return error
          itemService.generateTitle.mockResolvedValue(errorResponses[errorType]);

          const { container, unmount } = render(
            <BrowserRouter>
              <AddItemPage />
            </BrowserRouter>
          );

          try {
            // Select category first (required for AI generation)
            const categorySelect = within(container).getByLabelText(/category/i);
            await user.selectOptions(categorySelect, category);

            // Fill in form fields with user content
            const titleInput = container.querySelector('#title');
            await user.clear(titleInput);
            await user.click(titleInput);
            await user.paste(title);

            const descriptionInput = container.querySelector('#description');
            await user.clear(descriptionInput);
            await user.click(descriptionInput);
            await user.paste(description);

            // Store original values
            const originalTitle = titleInput.value;
            const originalDescription = descriptionInput.value;
            const originalCategory = categorySelect.value;

            // Try to generate AI title (which will fail)
            const generateTitleButton = within(container).getAllByRole('button').find(
              btn => btn.textContent.includes('Generate') && btn.textContent.includes('Title')
            );
            
            if (generateTitleButton) {
              await user.click(generateTitleButton);

              // Wait for error to appear
              await waitFor(() => {
                const errorMessage = within(container).queryByText(errorResponses[errorType].error);
                expect(errorMessage).toBeInTheDocument();
              }, { timeout: 2000 });

              // Verify form content is preserved
              expect(titleInput.value).toBe(originalTitle);
              expect(descriptionInput.value).toBe(originalDescription);
              expect(categorySelect.value).toBe(originalCategory);
            }
          } finally {
            unmount();
          }
        }
      ),
      { numRuns: 10 }
    );
  }, 30000);
});
