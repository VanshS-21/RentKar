package com.rentkar.service;

import com.rentkar.dto.AIGenerationRequest;

/**
 * Utility class for building AI prompts with category-specific instructions
 */
public class PromptBuilder {
    
    /**
     * Build a prompt for title generation
     */
    public static String buildTitlePrompt(AIGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an AI assistant helping college students list items for borrowing on a peer-to-peer sharing platform.\n\n");
        prompt.append("Generate a concise, attention-grabbing title for the following item:\n");
        prompt.append("- Item Name: ").append(request.getItemName()).append("\n");
        prompt.append("- Category: ").append(request.getCategory()).append("\n");
        
        if (request.getAdditionalInfo() != null && !request.getAdditionalInfo().isEmpty()) {
            prompt.append("- Additional Info: ").append(request.getAdditionalInfo()).append("\n");
        }
        
        prompt.append("\nRequirements:\n");
        prompt.append("- Length: 3-200 characters\n");
        prompt.append("- Be specific and descriptive\n");
        prompt.append("- Include key identifying features\n");
        prompt.append("- Use clear, professional language\n");
        prompt.append("- Target audience: college students\n");
        
        // Add category-specific instructions
        String categoryInstructions = getCategoryInstructions(request.getCategory());
        if (!categoryInstructions.isEmpty()) {
            prompt.append("\nCategory-specific guidelines:\n");
            prompt.append(categoryInstructions);
        }
        
        prompt.append("\nGenerate only the title, no additional text.");
        
        return prompt.toString();
    }
    
    /**
     * Build a prompt for description generation
     */
    public static String buildDescriptionPrompt(AIGenerationRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an AI assistant helping college students list items for borrowing on a peer-to-peer sharing platform.\n\n");
        prompt.append("Generate a compelling description for the following item:\n");
        prompt.append("- Item Name: ").append(request.getItemName()).append("\n");
        prompt.append("- Category: ").append(request.getCategory()).append("\n");
        
        if (request.getCondition() != null && !request.getCondition().isEmpty()) {
            prompt.append("- Condition: ").append(request.getCondition()).append("\n");
        }
        
        if (request.getSpecifications() != null && !request.getSpecifications().isEmpty()) {
            prompt.append("- Specifications: ").append(request.getSpecifications()).append("\n");
        }
        
        if (request.getAdditionalInfo() != null && !request.getAdditionalInfo().isEmpty()) {
            prompt.append("- Additional Info: ").append(request.getAdditionalInfo()).append("\n");
        }
        
        prompt.append("\nRequirements:\n");
        prompt.append("- Length: 50-1000 characters\n");
        prompt.append("- Highlight key features and benefits for borrowers\n");
        prompt.append("- Mention condition and any important details\n");
        prompt.append("- Use friendly, informative tone\n");
        prompt.append("- Structure with clear sentences\n");
        prompt.append("- Target audience: college students\n");
        
        // Add category-specific instructions
        String categoryInstructions = getCategoryInstructions(request.getCategory());
        if (!categoryInstructions.isEmpty()) {
            prompt.append("\nCategory-specific guidelines:\n");
            prompt.append(categoryInstructions);
        }
        
        prompt.append("\nGenerate only the description, no additional text.");
        
        return prompt.toString();
    }
    
    /**
     * Get category-specific instructions
     */
    public static String getCategoryInstructions(String category) {
        if (category == null) {
            return "";
        }
        
        switch (category.toLowerCase()) {
            case "electronics":
                return "- Emphasize technical specifications (model, features, capacity)\n" +
                       "- Mention condition clearly (new, like-new, good, fair)\n" +
                       "- Highlight any accessories included";
                
            case "books":
                return "- Mention subject matter and topic\n" +
                       "- Include edition information if relevant\n" +
                       "- Note condition (highlighting, annotations, wear)";
                
            case "sports equipment":
                return "- Describe usage scenarios and activities\n" +
                       "- Mention size/fit information\n" +
                       "- Note condition and any wear";
                
            case "tools":
                return "- Describe functionality and applications\n" +
                       "- Mention brand and model if relevant\n" +
                       "- Note condition and completeness";
                
            case "musical instruments":
                return "- Specify instrument type and brand\n" +
                       "- Mention skill level suitability\n" +
                       "- Note condition and any accessories";
                
            case "accessories":
                return "- Describe compatibility and use cases\n" +
                       "- Mention brand and model\n" +
                       "- Note condition and completeness";
                
            case "other":
                return "- Be descriptive about the item's purpose\n" +
                       "- Highlight unique features\n" +
                       "- Mention condition clearly";
                
            default:
                return "";
        }
    }
}
