package com.student.learncraft;

import java.util.*;

public class ContentAnalyzer {

    // Keywords that indicate definitions
    private static final String[] DEFINITION_KEYWORDS = {
            "is a", "is an", "are", "means", "refers to", "defined as",
            "is the", "represents", "stands for", "known as"
    };

    // Keywords that indicate comparisons
    private static final String[] COMPARISON_KEYWORDS = {
            "difference between", "compared to", "versus", "vs", "while",
            "whereas", "unlike", "similar to", "different from"
    };

    // Keywords for important terms
    private static final String[] IMPORTANT_KEYWORDS = {
            "important", "key", "main", "primary", "essential", "fundamental",
            "critical", "significant", "major", "basic"
    };

    /**
     * Analyzes content and classifies each sentence
     */
    public static class AnalyzedContent {
        public String text;
        public ContentType type;
        public List<String> keywords;
        public int importance; // 1-5

        public AnalyzedContent(String text, ContentType type) {
            this.text = text;
            this.type = type;
            this.keywords = new ArrayList<>();
            this.importance = 3; // default
        }
    }

    public enum ContentType {
        DEFINITION,
        FACT,
        COMPARISON,
        LIST_ITEM,
        GENERAL
    }

    /**
     * Analyzes all text content from PPT
     */
    public List<AnalyzedContent> analyzeContent(List<String> textContent) {
        List<AnalyzedContent> analyzed = new ArrayList<>();

        for (String text : textContent) {
            if (text == null || text.trim().isEmpty()) continue;

            AnalyzedContent content = analyzeText(text);
            analyzed.add(content);
        }

        return analyzed;
    }

    /**
     * Analyzes a single text and determines its type
     */
    private AnalyzedContent analyzeText(String text) {
        String lowerText = text.toLowerCase();

        // Check for definitions
        for (String keyword : DEFINITION_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                AnalyzedContent content = new AnalyzedContent(text, ContentType.DEFINITION);
                content.importance = 5; // Definitions are very important
                extractKeywords(content, text);
                return content;
            }
        }

        // Check for comparisons
        for (String keyword : COMPARISON_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                AnalyzedContent content = new AnalyzedContent(text, ContentType.COMPARISON);
                content.importance = 4;
                extractKeywords(content, text);
                return content;
            }
        }

        // Check if it's a list item (short statement)
        if (text.length() < 100 && !text.contains(".")) {
            AnalyzedContent content = new AnalyzedContent(text, ContentType.LIST_ITEM);
            content.importance = 3;
            extractKeywords(content, text);
            return content;
        }

        // Check for important facts
        for (String keyword : IMPORTANT_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                AnalyzedContent content = new AnalyzedContent(text, ContentType.FACT);
                content.importance = 4;
                extractKeywords(content, text);
                return content;
            }
        }

        // Default to general fact
        AnalyzedContent content = new AnalyzedContent(text, ContentType.FACT);
        content.importance = 3;
        extractKeywords(content, text);
        return content;
    }

    /**
     * Extracts important keywords from text
     */
    private void extractKeywords(AnalyzedContent content, String text) {
        // Remove common words
        String[] stopWords = {"the", "is", "are", "was", "were", "a", "an", "and",
                "or", "but", "in", "on", "at", "to", "for"};

        String[] words = text.split("\\s+");
        Set<String> stopWordSet = new HashSet<>(Arrays.asList(stopWords));

        for (String word : words) {
            // Clean the word
            word = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

            // Add if it's significant
            if (word.length() > 3 && !stopWordSet.contains(word)) {
                content.keywords.add(word);
            }
        }
    }

    /**
     * Gets content by type
     */
    public List<AnalyzedContent> getContentByType(List<AnalyzedContent> analyzed, ContentType type) {
        List<AnalyzedContent> filtered = new ArrayList<>();
        for (AnalyzedContent content : analyzed) {
            if (content.type == type) {
                filtered.add(content);
            }
        }
        return filtered;
    }

    /**
     * Gets high importance content
     */
    public List<AnalyzedContent> getImportantContent(List<AnalyzedContent> analyzed) {
        List<AnalyzedContent> important = new ArrayList<>();
        for (AnalyzedContent content : analyzed) {
            if (content.importance >= 4) {
                important.add(content);
            }
        }
        return important;
    }
}