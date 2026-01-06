package com.student.learncraft;

import java.util.*;

public class MCQGenerator {

    private Random random;

    public MCQGenerator() {
        this.random = new Random();
    }

    /**
     * Generates MCQ questions from PPT content - SIMPLIFIED VERSION
     */
    public List<MCQQuestion> generateQuestions(PPTContent pptContent, int numQuestions) {
        List<MCQQuestion> questions = new ArrayList<>();

        try {
            // Get all text content
            List<String> allText = pptContent.getAllTextContent();

            if (allText == null || allText.isEmpty()) {
                return questions;
            }

            // Filter out very short text
            List<String> validText = new ArrayList<>();
            for (String text : allText) {
                if (text != null && text.trim().length() > 10) {
                    validText.add(text.trim());
                }
            }

            if (validText.size() < 4) {
                // Not enough content
                return questions;
            }

            // Generate questions
            int questionsGenerated = 0;
            int attempts = 0;
            int maxAttempts = numQuestions * 3;

            while (questionsGenerated < numQuestions && attempts < maxAttempts) {
                attempts++;

                try {
                    MCQQuestion question = generateSimpleQuestion(validText);

                    if (question != null && isValidQuestion(question)) {
                        questions.add(question);
                        questionsGenerated++;
                    }
                } catch (Exception e) {
                    // Skip this question and try again
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return questions;
    }

    /**
     * Generate a simple "Which of the following is true?" question
     */
    private MCQQuestion generateSimpleQuestion(List<String> textList) {
        if (textList.size() < 4) {
            return null;
        }

        try {
            // Pick a random correct answer
            int correctIndex = random.nextInt(textList.size());
            String correctAnswer = textList.get(correctIndex);

            // Create question
            String questionText = "Which of the following is correct?";

            // Create options list
            List<String> options = new ArrayList<>();
            options.add(correctAnswer);

            // Add 3 wrong options
            Set<Integer> usedIndices = new HashSet<>();
            usedIndices.add(correctIndex);

            while (options.size() < 4 && usedIndices.size() < textList.size()) {
                int randomIndex = random.nextInt(textList.size());

                if (!usedIndices.contains(randomIndex)) {
                    String wrongOption = textList.get(randomIndex);

                    // Make sure it's different
                    if (!wrongOption.equals(correctAnswer) && wrongOption.length() < 200) {
                        options.add(wrongOption);
                        usedIndices.add(randomIndex);
                    }
                }
            }

            // Need exactly 4 options
            if (options.size() < 4) {
                return null;
            }

            // Shuffle options
            Collections.shuffle(options);
            int newCorrectIndex = options.indexOf(correctAnswer);

            // Create question
            MCQQuestion question = new MCQQuestion();
            question.setQuestion(questionText);
            question.setOptions(options);
            question.setCorrectAnswerIndex(newCorrectIndex);
            question.setExplanation(correctAnswer);
            question.setType(MCQQuestion.QuestionType.FACT);

            return question;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate that question is properly formed
     */
    private boolean isValidQuestion(MCQQuestion question) {
        if (question == null) return false;
        if (question.getQuestion() == null || question.getQuestion().isEmpty()) return false;
        if (question.getOptions() == null || question.getOptions().size() != 4) return false;
        if (question.getCorrectAnswerIndex() < 0 || question.getCorrectAnswerIndex() >= 4) return false;

        // Check all options are unique and not empty
        Set<String> uniqueOptions = new HashSet<>(question.getOptions());
        if (uniqueOptions.size() != 4) return false;

        for (String option : question.getOptions()) {
            if (option == null || option.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}