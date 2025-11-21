package com.rentkar.service;

import com.rentkar.dto.AIGenerationRequest;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PromptBuilder
 * Tests prompt engineering requirements
 */
public class PromptBuilderPropertyTest {
    
    // Feature: ai-description-generation, Property 20: Prompt includes format instructions
    // Validates: Requirements 8.1
    @Property(tries = 100)
    void promptIncludesFormatInstructions(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("promptType") String promptType) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory(category);
        
        String prompt;
        if (promptType.equals("title")) {
            prompt = PromptBuilder.buildTitlePrompt(request);
        } else {
            prompt = PromptBuilder.buildDescriptionPrompt(request);
        }
        
        // Verify prompt contains format instructions
        assertThat(prompt).containsIgnoringCase("Requirements:");
        assertThat(prompt).containsIgnoringCase("Length:");
        
        // Verify it specifies output format
        if (promptType.equals("title")) {
            assertThat(prompt).contains("3-200 characters");
            assertThat(prompt).containsIgnoringCase("Generate only the title");
        } else {
            assertThat(prompt).contains("50-1000 characters");
            assertThat(prompt).containsIgnoringCase("Generate only the description");
        }
    }
    
    // Feature: ai-description-generation, Property 21: Title prompts emphasize conciseness
    // Validates: Requirements 8.2
    @Property(tries = 100)
    void titlePromptsEmphasizeConciseness(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory(category);
        
        String prompt = PromptBuilder.buildTitlePrompt(request);
        
        // Verify prompt instructs model to be concise
        assertThat(prompt).containsIgnoringCase("concise");
        assertThat(prompt).containsIgnoringCase("attention-grabbing");
        
        // Verify it emphasizes brevity through character limit
        assertThat(prompt).contains("3-200 characters");
    }
    
    // Feature: ai-description-generation, Property 22: Description prompts highlight benefits
    // Validates: Requirements 8.3
    @Property(tries = 100)
    void descriptionPromptsHighlightBenefits(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory(category);
        
        String prompt = PromptBuilder.buildDescriptionPrompt(request);
        
        // Verify prompt mentions benefits for borrowers
        assertThat(prompt).containsIgnoringCase("benefits");
        assertThat(prompt).containsIgnoringCase("borrowers");
        
        // Verify it asks to highlight key features
        assertThat(prompt).containsIgnoringCase("Highlight key features");
    }
    
    // Feature: ai-description-generation, Property 23: Prompts include audience context
    // Validates: Requirements 8.4
    @Property(tries = 100)
    void promptsIncludeAudienceContext(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("promptType") String promptType) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory(category);
        
        String prompt;
        if (promptType.equals("title")) {
            prompt = PromptBuilder.buildTitlePrompt(request);
        } else {
            prompt = PromptBuilder.buildDescriptionPrompt(request);
        }
        
        // Verify prompt mentions college students as target audience
        assertThat(prompt).containsIgnoringCase("college students");
        assertThat(prompt).containsIgnoringCase("Target audience: college students");
    }
    
    // Feature: ai-description-generation, Property 24: Prompts specify tone
    // Validates: Requirements 8.5
    @Property(tries = 100)
    void promptsSpecifyTone(
            @ForAll("validItemName") String itemName,
            @ForAll("validCategory") String category,
            @ForAll("promptType") String promptType) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory(category);
        
        String prompt;
        if (promptType.equals("title")) {
            prompt = PromptBuilder.buildTitlePrompt(request);
        } else {
            prompt = PromptBuilder.buildDescriptionPrompt(request);
        }
        
        // Verify prompt includes tone instructions
        String promptLower = prompt.toLowerCase();
        boolean hasToneInstruction = promptLower.contains("friendly") ||
                                     promptLower.contains("informative") ||
                                     promptLower.contains("professional") ||
                                     promptLower.contains("clear");
        
        assertThat(hasToneInstruction).isTrue();
        
        // For descriptions, specifically check for "friendly, informative tone"
        if (promptType.equals("description")) {
            assertThat(prompt).containsIgnoringCase("friendly");
            assertThat(prompt).containsIgnoringCase("informative");
            assertThat(prompt).containsIgnoringCase("tone");
        }
    }
    
    // Feature: ai-description-generation, Property 25: Electronics prompts mention specifications
    // Validates: Requirements 9.1
    @Property(tries = 100)
    void electronicsPromptsMentionSpecifications(
            @ForAll("validItemName") String itemName) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory("Electronics");
        
        String titlePrompt = PromptBuilder.buildTitlePrompt(request);
        String descPrompt = PromptBuilder.buildDescriptionPrompt(request);
        
        // Verify Electronics category instructions emphasize technical specifications
        String categoryInstructions = PromptBuilder.getCategoryInstructions("Electronics");
        
        assertThat(categoryInstructions).containsIgnoringCase("technical specifications");
        assertThat(categoryInstructions).containsIgnoringCase("condition");
        
        // Verify the instructions are included in the prompts
        assertThat(titlePrompt).contains(categoryInstructions);
        assertThat(descPrompt).contains(categoryInstructions);
    }
    
    // Feature: ai-description-generation, Property 26: Books prompts mention subject matter
    // Validates: Requirements 9.2
    @Property(tries = 100)
    void booksPromptsMentionSubjectMatter(
            @ForAll("validItemName") String itemName) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory("Books");
        
        String titlePrompt = PromptBuilder.buildTitlePrompt(request);
        String descPrompt = PromptBuilder.buildDescriptionPrompt(request);
        
        // Verify Books category instructions mention subject matter and edition
        String categoryInstructions = PromptBuilder.getCategoryInstructions("Books");
        
        assertThat(categoryInstructions).containsIgnoringCase("subject");
        assertThat(categoryInstructions).containsIgnoringCase("edition");
        
        // Verify the instructions are included in the prompts
        assertThat(titlePrompt).contains(categoryInstructions);
        assertThat(descPrompt).contains(categoryInstructions);
    }
    
    // Feature: ai-description-generation, Property 27: Sports Equipment prompts mention usage
    // Validates: Requirements 9.3
    @Property(tries = 100)
    void sportsEquipmentPromptsMentionUsage(
            @ForAll("validItemName") String itemName) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory("Sports Equipment");
        
        String titlePrompt = PromptBuilder.buildTitlePrompt(request);
        String descPrompt = PromptBuilder.buildDescriptionPrompt(request);
        
        // Verify Sports Equipment category instructions highlight usage scenarios
        String categoryInstructions = PromptBuilder.getCategoryInstructions("Sports Equipment");
        
        assertThat(categoryInstructions).containsIgnoringCase("usage");
        assertThat(categoryInstructions).containsIgnoringCase("scenarios");
        
        // Verify the instructions are included in the prompts
        assertThat(titlePrompt).contains(categoryInstructions);
        assertThat(descPrompt).contains(categoryInstructions);
    }
    
    // Feature: ai-description-generation, Property 28: Tools prompts mention functionality
    // Validates: Requirements 9.4
    @Property(tries = 100)
    void toolsPromptsMentionFunctionality(
            @ForAll("validItemName") String itemName) {
        
        AIGenerationRequest request = new AIGenerationRequest();
        request.setItemName(itemName);
        request.setCategory("Tools");
        
        String titlePrompt = PromptBuilder.buildTitlePrompt(request);
        String descPrompt = PromptBuilder.buildDescriptionPrompt(request);
        
        // Verify Tools category instructions describe functionality
        String categoryInstructions = PromptBuilder.getCategoryInstructions("Tools");
        
        assertThat(categoryInstructions).containsIgnoringCase("functionality");
        assertThat(categoryInstructions).containsIgnoringCase("applications");
        
        // Verify the instructions are included in the prompts
        assertThat(titlePrompt).contains(categoryInstructions);
        assertThat(descPrompt).contains(categoryInstructions);
    }
    
    // Arbitraries
    @Provide
    Arbitrary<String> validItemName() {
        return Arbitraries.of(
            "Laptop", "Textbook", "Calculator", "Guitar", "Bicycle",
            "Drill", "Camera", "Headphones", "Backpack", "Tent",
            "Smartphone", "Tablet", "Monitor", "Keyboard", "Mouse"
        );
    }
    
    @Provide
    Arbitrary<String> validCategory() {
        return Arbitraries.of(
            "Electronics", "Books", "Accessories", "Sports Equipment",
            "Musical Instruments", "Tools", "Other"
        );
    }
    
    @Provide
    Arbitrary<String> promptType() {
        return Arbitraries.of("title", "description");
    }
}
