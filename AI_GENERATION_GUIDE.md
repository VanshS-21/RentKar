# AI Generation User Guide

## Overview

RentKar's AI Generation feature helps you create compelling item titles and descriptions automatically using Google's Gemini AI. This guide explains how to use the feature effectively and get the best results.

## What is AI Generation?

AI Generation uses artificial intelligence to automatically create:
- **Titles**: Concise, attention-grabbing names for your items (3-200 characters)
- **Descriptions**: Detailed, professional descriptions highlighting key features (50-1000 characters)

The AI analyzes the information you provide and generates content tailored to college students looking to borrow items.

## How to Use AI Generation

### When Creating a New Item

1. **Navigate to Add Item Page**
   - Click "List an Item" or "Add Item" from the navigation menu

2. **Fill in Basic Information**
   - Enter the item name (e.g., "Scientific Calculator")
   - Select the category (e.g., "Electronics")
   - Optionally add additional details like condition or specifications

3. **Generate Title**
   - Click the "Generate with AI" button next to the title field
   - Wait 1-3 seconds for the AI to generate a title
   - The generated title will automatically populate the title field
   - You can edit the generated title if needed

4. **Generate Description**
   - Click the "Generate with AI" button next to the description field
   - Wait 1-3 seconds for the AI to generate a description
   - The generated description will automatically populate the description field
   - You can edit the generated description if needed

5. **Review and Submit**
   - Review the AI-generated content
   - Make any edits you want
   - Add an image
   - Click "Create Item" to publish your listing

### When Editing an Existing Item

1. **Navigate to Edit Item Page**
   - Go to "My Items" and click "Edit" on any item

2. **Use AI Generation**
   - The AI generation buttons work the same way as when creating a new item
   - The AI will use your existing item information to generate new suggestions
   - Your current content won't be overwritten until you click the generate button

## Regeneration Feature

If you're not satisfied with the first AI-generated result, you can regenerate:

1. **Click the Regenerate Button**
   - After initial generation, a "Regenerate" button appears
   - Click it to get a different suggestion

2. **Multiple Attempts**
   - You can regenerate multiple times to explore different options
   - Each regeneration counts toward your rate limit (see below)

3. **Temperature Variation**
   - Each regeneration uses slightly different AI parameters to produce varied results
   - Results will be different but still relevant to your item

## Rate Limits

To ensure fair usage and control costs, AI generation has rate limits:

### Limits
- **10 requests per hour** per user
- Applies to both title and description generation
- Regenerations count as new requests

### What Happens When You Hit the Limit
- You'll see a message: "Rate limit exceeded. Please try again later."
- The generate button will be temporarily disabled
- A countdown timer shows when you can make new requests

### Rate Limit Reset
- Rate limits reset on a rolling 1-hour window
- After 1 hour from your first request, you can make new requests
- The remaining request count is displayed near the generate button

### Tips to Manage Rate Limits
- Use AI generation thoughtfully - don't spam the regenerate button
- Edit AI-generated content instead of regenerating repeatedly
- Plan your item listings to stay within the limit
- If you need more generations, wait for the cooldown period

## Tips for Best Results

### Provide Good Input Information

**Item Name**
- Be specific: "Casio FX-991EX Calculator" is better than "Calculator"
- Include brand/model when relevant
- Use proper capitalization

**Category**
- Select the most accurate category
- The AI tailors content based on category (e.g., Electronics emphasizes specs)

**Additional Information**
- Add condition details: "Like New", "Gently Used", "Good Condition"
- Include specifications: "552 functions, solar powered"
- Mention accessories: "Includes protective case"
- Note any unique features or selling points

### Category-Specific Tips

**Electronics**
- Mention brand, model, and technical specifications
- Include condition and any accessories
- Example: "MacBook Pro 2020, 16GB RAM, includes charger, excellent condition"

**Books**
- Include subject matter and edition
- Mention condition (highlighting, annotations)
- Example: "Calculus textbook, 9th edition, minimal highlighting"

**Sports Equipment**
- Describe usage scenarios and size/fit
- Note condition and wear
- Example: "Tennis racket, Wilson Pro Staff, grip size 4, good condition"

**Tools**
- Describe functionality and applications
- Mention brand and completeness
- Example: "Cordless drill, DeWalt 20V, includes battery and charger"

**Musical Instruments**
- Specify instrument type and brand
- Mention skill level suitability
- Example: "Acoustic guitar, Yamaha FG800, beginner-friendly"

### Editing AI-Generated Content

**Do Edit**
- Fix any inaccuracies
- Add specific details the AI might have missed
- Adjust tone to match your style
- Correct any formatting issues

**Don't Worry About**
- Minor edits won't affect future generations
- Your edits are always preserved
- You can always regenerate if you want to start over

## Error Handling

### Common Errors and Solutions

**"AI generation service is currently unavailable"**
- The Gemini API might be down or not configured
- Solution: Try again later or enter content manually
- Item creation still works without AI

**"Request timed out"**
- The AI took too long to respond (>30 seconds)
- Solution: Click the generate button again
- Usually works on retry

**"Rate limit exceeded"**
- You've made 10 requests in the past hour
- Solution: Wait for the cooldown period (shown in the message)
- Plan your listings to stay within limits

**"Validation failed"**
- Required fields are missing (item name or category)
- Solution: Fill in the required fields before generating

**Network Errors**
- Your internet connection might be unstable
- Solution: Check your connection and try again

### What Happens on Error
- Your form content is always preserved
- You won't lose any information you've entered
- You can try again or enter content manually
- Item creation works normally even if AI generation fails

## Privacy and Data Usage

### What Information is Sent to AI
- Item name
- Category
- Additional information you provide (condition, specifications)
- No personal information is sent

### What is Logged
- User ID and timestamp (for rate limiting)
- Request type (title or description)
- Response time and token count (for monitoring)
- Error messages (for debugging)

### What is NOT Logged
- Your personal information
- API keys
- Sensitive user data

## Frequently Asked Questions

**Q: Is AI generation required?**
A: No, it's completely optional. You can always enter titles and descriptions manually.

**Q: Can I edit AI-generated content?**
A: Yes! Feel free to edit, modify, or completely rewrite AI-generated content.

**Q: How accurate is the AI?**
A: The AI generates relevant, professional content, but always review and edit as needed. It doesn't know specific details about your item that you haven't provided.

**Q: Why is there a rate limit?**
A: Rate limits ensure fair usage for all users and help control API costs.

**Q: What if I don't like the generated content?**
A: You can regenerate for a different suggestion, edit the content, or write your own from scratch.

**Q: Does AI generation work offline?**
A: No, it requires an internet connection to access the Gemini API.

**Q: Can I use AI generation for all categories?**
A: Yes! The AI adapts its output based on the category you select.

**Q: What happens if the AI service is down?**
A: You can still create and edit items manually. The platform works normally without AI.

**Q: How long does generation take?**
A: Typically 1-3 seconds. Maximum timeout is 30 seconds.

**Q: Can I see how many requests I have left?**
A: Yes, the remaining request count is displayed near the generate button.

## Best Practices

1. **Start with Good Input**: Provide detailed information for better results
2. **Review Before Saving**: Always review AI-generated content before publishing
3. **Edit as Needed**: Don't hesitate to modify the generated content
4. **Use Regeneration Wisely**: Try regenerating if the first result isn't quite right
5. **Stay Within Limits**: Plan your listings to avoid hitting rate limits
6. **Provide Feedback**: If you notice issues, report them to improve the feature

## Support

If you encounter issues with AI generation:
- Check this guide for common solutions
- Verify your internet connection
- Try regenerating or refreshing the page
- Contact support if problems persist

Remember: AI generation is a tool to help you, not replace you. Always review and personalize the content to make your listings stand out!
