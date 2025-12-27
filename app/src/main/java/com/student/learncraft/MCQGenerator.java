package com.student.learncraft;

import java.util.*;

public class MCQGenerator {

    private ContentAnalyzer analyzer;
    private Random random;

    // Distribution percentages (Marwadi-style)
    private static final int DEFINITION_PERCENT = 35;
    private static final int FACT_PERCENT = 30;
    private static final int FILL_BLANK_PERCENT = 20;
    private static final int NOT_QUESTION_PERCENT = 15;

    public MCQGenerator() {
        this.analyzer = new ContentAnalyzer();
        this.random = new Random();
    }

    /**
     * Generates MCQ questions from PPT content
     * @param pptContent The PPT content to generate from
     * @param numQuestions Number of questions to generate
     * @return List of MCQ questions
     */
    public List<MCQQuestion> generateQuestions(PPTContent pptContent, int numQuestions) {
        List<MCQQuestion> questions = new ArrayList<>();

        // Get all text content
        List<String> allText = pptContent.getAllTextContent();

        if (allText.isEmpty()) {
            return questions;
        }

        // Analyze content
        List<ContentAnalyzer.AnalyzedContent> analyzed = analyzer.analyzeContent(allText);

        // Calculate distribution
        int defQuestions = (numQuestions * DEFINITION_PERCENT) / 100;
        int factQuestions = (numQuestions * FACT_PERCENT) / 100;
        int fillQuestions = (numQuestions * FILL_BLANK_PERCENT) / 100;
        int notQuestions = numQuestions - (defQuestions + factQuestions + fillQuestions);

        // Generate each type
        questions.addAll(generateDefinitionQuestions(analyzed, defQuestions));
        questions.addAll(generateFactQuestions(analyzed, factQuestions));
        questions.addAll(generateFillBlankQuestions(analyzed, fillQuestions));
        questions.addAll(generateNotQuestions(analyzed, notQuestions));

        // Shuffle questions
        Collections.shuffle(questions);

        return questions.subList(0, Math.min(numQuestions, questions.size()));
    }

    /**
     * Generate definition-type questions
     */
    private List<MCQQuestion> generateDefinitionQuestions(
            List<ContentAnalyzer.AnalyzedContent> analyzed, int count) {

        List<MCQQuestion> questions = new ArrayList<>();
        List<ContentAnalyzer.AnalyzedContent> definitions =
                analyzer.getContentByType(analyzed, ContentAnalyzer.ContentType.DEFINITION);

        if (definitions.isEmpty()) {
            definitions = analyzed; // Fallback to all content
        }

        for (int i = 0; i < Math.min(count, definitions.size()); i++) {
            ContentAnalyzer.AnalyzedContent content = definitions.get(i);
            MCQQuestion question = createDefinitionQuestion(content, analyzed);
            if (question != null) {
                questions.add(question);
            }
        }

        return questions;
    }

    private MCQQuestion createDefinitionQuestion(
            ContentAnalyzer.AnalyzedContent content,
            List<ContentAnalyzer.AnalyzedContent> allContent) {

        String text = content.text;

        // Extract the term being defined
        String[] parts = text.split("(is a|is an|are|means|refers to|defined as|is the)");
        if (parts.length < 2) return null;

        String term = parts[0].trim();
        String definition = parts[1].trim();

        // Create question
        String questionText = "What is " + term + "?";

        // Create options
        List<String> options = new ArrayList<>();
        options.add(definition); // Correct answer

        // Add wrong options from other content
        for (int i = 0; i < 3 && options.size() < 4; i++) {
            int randomIndex = random.nextInt(allContent.size());
            String wrongOption = allContent.get(randomIndex).text;

            if (!options.contains(wrongOption) && wrongOption.length() < 150) {
                options.add(wrongOption);
            }
        }

        // Shuffle options
        Collections.shuffle(options);
        int correctIndex = options.indexOf(definition);

        MCQQuestion question = new MCQQuestion(
                questionText,
                options,
                correctIndex,
                text,
                MCQQuestion.QuestionType.DEFINITION
        );

        return question;
    }

    /**
     * Generate fact-based questions
     */
    private List<MCQQuestion> generateFactQuestions(
            List<ContentAnalyzer.AnalyzedContent> analyzed, int count) {

        List<MCQQuestion> questions = new ArrayList<>();
        List<ContentAnalyzer.AnalyzedContent> facts =
                analyzer.getContentByType(analyzed, ContentAnalyzer.ContentType.FACT);

        if (facts.isEmpty()) {
            facts = analyzed;
        }

        for (int i = 0; i < Math.min(count, facts.size()); i++) {
            ContentAnalyzer.AnalyzedContent content = facts.get(i);
            MCQQuestion question = createFactQuestion(content, analyzed);
            if (question != null) {
                questions.add(question);
            }
        }

        return questions;
    }

    private MCQQuestion createFactQuestion(
            ContentAnalyzer.AnalyzedContent content,
            List<ContentAnalyzer.AnalyzedContent> allContent) {

        // Use the fact as question
        String questionText = "Which of the following is true?";

        List<String> options = new ArrayList<>();
        options.add(content.text); // Correct answer

        // Add wrong options
        for (int i = 0; i < 3 && options.size() < 4; i++) {
            int randomIndex = random.nextInt(allContent.size());
            String wrongOption = allContent.get(randomIndex).text;

            if (!options.contains(wrongOption) && wrongOption.length() < 150) {
                options.add(wrongOption);
            }
        }

        Collections.shuffle(options);
        int correctIndex = options.indexOf(content.text);

        return new MCQQuestion(
                questionText,
                options,
                correctIndex,
                content.text,
                MCQQuestion.QuestionType.FACT
        );
    }

    /**
     * Generate fill in the blank questions
     */
    private List<MCQQuestion> generateFillBlankQuestions(
            List<ContentAnalyzer.AnalyzedContent> analyzed, int count) {

        List<MCQQuestion> questions = new ArrayList<>();

        for (int i = 0; i < Math.min(count, analyzed.size()); i++) {
            ContentAnalyzer.AnalyzedContent content = analyzed.get(i);

            if (content.keywords.isEmpty()) continue;

            MCQQuestion question = createFillBlankQuestion(content, analyzed);
            if (question != null) {
                questions.add(question);
            }
        }

        return questions;
    }

    private MCQQuestion createFillBlankQuestion(
            ContentAnalyzer.AnalyzedContent content,
            List<ContentAnalyzer.AnalyzedContent> allContent) {

        if (content.keywords.isEmpty()) return null;

        // Pick a keyword to blank out
        String keyword = content.keywords.get(random.nextInt(content.keywords.size()));

        // Create question with blank
        String questionText = content.text.replaceFirst(
                "(?i)" + keyword,
                "______"
        );

        questionText = "Fill in the blank: " + questionText;

        // Create options
        List<String> options = new ArrayList<>();
        options.add(keyword); // Correct answer

        // Add wrong options from other keywords
        Set<String> usedOptions = new HashSet<>();
        usedOptions.add(keyword.toLowerCase());

        for (ContentAnalyzer.AnalyzedContent other : allContent) {
            if (options.size() >= 4) break;

            for (String otherKeyword : other.keywords) {
                if (!usedOptions.contains(otherKeyword.toLowerCase()) &&
                        otherKeyword.length() > 2) {
                    options.add(otherKeyword);
                    usedOptions.add(otherKeyword.toLowerCase());
                    break;
                }
            }
        }

        if (options.size() < 4) return null;

        Collections.shuffle(options);
        int correctIndex = options.indexOf(keyword);

        return new MCQQuestion(
                questionText,
                options,
                correctIndex,
                content.text,
                MCQQuestion.QuestionType.FILL_IN_BLANK
        );
    }

    /**
     * Generate "NOT" type questions
     */
    private List<MCQQuestion> generateNotQuestions(
            List<ContentAnalyzer.AnalyzedContent> analyzed, int count) {

        List<MCQQuestion> questions = new ArrayList<>();

        for (int i = 0; i < Math.min(count, analyzed.size() - 3); i++) {
            MCQQuestion question = createNotQuestion(analyzed, i);
            if (question != null) {
                questions.add(question);
            }
        }

        return questions;
    }

    private MCQQuestion createNotQuestion(
            List<ContentAnalyzer.AnalyzedContent> analyzed, int startIndex) {

        if (analyzed.size() < 4) return null;

        // Get topic from first content
        ContentAnalyzer.AnalyzedContent first = analyzed.get(startIndex);

        String questionText = "Which of the following is NOT true?";

        List<String> options = new ArrayList<>();

        // Add 3 true statements
        for (int i = 0; i < 3 && (startIndex + i) < analyzed.size(); i++) {
            options.add(analyzed.get(startIndex + i).text);
        }

        // Add 1 false statement (just use random unrelated content)
        int randomIndex = random.nextInt(analyzed.size());
        String falseStatement = "All of the above are correct";
        options.add(falseStatement);

        Collections.shuffle(options);
        int correctIndex = options.indexOf(falseStatement);

        return new MCQQuestion(
                questionText,
                options,
                correctIndex,
                "This is a NOT question type",
                MCQQuestion.QuestionType.NOT_QUESTION
        );
    }
}