package com.student.learncraft;

import java.util.*;

public class MCQGenerator {

    private final Random random = new Random();

    // ==================================================================================
    // 🧠 THE "TECH DICTIONARY" - Used to build fake, convincing full forms
    // ==================================================================================
    private static final Map<Character, List<String>> TECH_TERMS = new HashMap<>();

    static {
        TECH_TERMS.put('A', Arrays.asList("Application", "Architecture", "Access", "Active", "Algorithm", "Allocation", "Analysis", "Array", "Async", "Attribute", "Authentication", "Authorization", "Adapter"));
        TECH_TERMS.put('B', Arrays.asList("Base", "Binary", "Block", "Bridge", "Browser", "Buffer", "Bus", "Byte", "Back", "Backend", "Bean", "Bit", "Bootstrap"));
        TECH_TERMS.put('C', Arrays.asList("Control", "Central", "Computer", "Code", "Cache", "Client", "Common", "Core", "Cyber", "Class", "Component", "Configuration", "Connection", "Container", "Context"));
        TECH_TERMS.put('D', Arrays.asList("Data", "Digital", "Direct", "Domain", "Dynamic", "Device", "Driver", "Disk", "Database", "Definition", "Dependency", "Deployment", "Directory"));
        TECH_TERMS.put('E', Arrays.asList("Electronic", "Engine", "End", "Error", "Event", "External", "Execution", "Extended", "Element", "Entity", "Environment", "Exception", "Encapsulation"));
        TECH_TERMS.put('F', Arrays.asList("File", "Format", "Frame", "Function", "Field", "Flow", "Form", "Frequency", "Front", "Factory", "Filter", "Framework", "Fetch"));
        TECH_TERMS.put('G', Arrays.asList("Global", "Graphic", "Gateway", "Group", "Grid", "Generate", "General", "Generic", "Graph"));
        TECH_TERMS.put('H', Arrays.asList("Hyper", "Hardware", "Host", "Heap", "Hash", "Handler", "High", "Header", "Handle"));
        TECH_TERMS.put('I', Arrays.asList("Interface", "Internet", "Information", "Input", "Internal", "Instruction", "Image", "Index", "Integrated", "Implementation", "Instance", "Isolation", "Injection"));
        TECH_TERMS.put('J', Arrays.asList("Java", "Job", "Join", "Journal", "Just", "JSON", "Jvm"));
        TECH_TERMS.put('K', Arrays.asList("Key", "Kernel", "Knowledge", "Kit"));
        TECH_TERMS.put('L', Arrays.asList("Language", "Local", "Link", "Logic", "Layer", "List", "Load", "Loop", "Library", "Lock", "Latency"));
        TECH_TERMS.put('M', Arrays.asList("Memory", "Machine", "Model", "Module", "Main", "Media", "Method", "Management", "Message", "Mode", "Mapping", "Middleware", "Monitor"));
        TECH_TERMS.put('N', Arrays.asList("Network", "Node", "Name", "Native", "Null", "Number", "Net", "Namespace", "Notation"));
        TECH_TERMS.put('O', Arrays.asList("Object", "Output", "Operating", "Open", "Option", "Order", "Organization", "Operation", "Orchestration"));
        TECH_TERMS.put('P', Arrays.asList("Protocol", "Program", "Processing", "Page", "Path", "Port", "Public", "Packet", "Platform", "Procedure", "Package", "Parameter", "Pipeline"));
        TECH_TERMS.put('Q', Arrays.asList("Query", "Queue", "Quality", "Quick"));
        TECH_TERMS.put('R', Arrays.asList("Resource", "Read", "Real", "Remote", "Root", "Runtime", "Random", "Register", "Request", "Response", "Reference", "Repository", "Routing"));
        TECH_TERMS.put('S', Arrays.asList("System", "Server", "Software", "Source", "Standard", "Storage", "Script", "Security", "Service", "Session", "Socket", "Stack", "Scope", "State", "Structure", "Schema"));
        TECH_TERMS.put('T', Arrays.asList("Transfer", "Text", "Type", "Thread", "Time", "Tool", "Token", "Transaction", "Table", "Target", "Template", "Technology"));
        TECH_TERMS.put('U', Arrays.asList("User", "Unit", "Universal", "Url", "Utility", "Update", "Unified", "Upload"));
        TECH_TERMS.put('V', Arrays.asList("View", "Virtual", "Variable", "Value", "Vector", "Video", "Visual", "Version", "Volume"));
        TECH_TERMS.put('W', Arrays.asList("Web", "Wide", "Window", "Word", "Wireless", "Write", "Wrapper", "Worker"));
        TECH_TERMS.put('X', Arrays.asList("Extensible", "Xml", "Xerox"));
    }

    public List<MCQQuestion> generateQuestions(PPTContent pptContent, int requestedCount) {

        List<PPTContent.SlideContent> slides = pptContent.getSlides();
        if (slides == null || slides.isEmpty()) return new ArrayList<>();

        List<PPTContent.SlideContent> shuffledSlides = new ArrayList<>(slides);
        Collections.shuffle(shuffledSlides, random);

        List<MCQQuestion> result = new ArrayList<>();
        int slideIndex = 0;
        int attempts = 0;
        int maxAttempts = requestedCount * 3;

        while (result.size() < requestedCount && attempts < maxAttempts) {
            PPTContent.SlideContent slide = shuffledSlides.get(slideIndex);

            MCQQuestion q = generateFromSlide(slide, shuffledSlides);
            if (q != null) {
                result.add(q);
            }

            slideIndex++;
            attempts++;

            if (slideIndex >= shuffledSlides.size()) {
                slideIndex = 0;
                Collections.shuffle(shuffledSlides, random);
            }
        }

        return result;
    }

    /* ================= SLIDE-BASED GENERATION ================= */

    private MCQQuestion generateFromSlide(PPTContent.SlideContent slide, List<PPTContent.SlideContent> allSlides) {
        String topic = extractTopic(slide);
        List<String> sentences = cleanSentences(slide.getContentPoints());

        if (sentences.isEmpty()) return null;

        // 🚀 PRIORITY CHECK: Does this slide contain Full Forms/Acronyms?
        // If yes, FORCE the Full Form generator (Type 4) instead of picking randomly.
        if (containsAcronymPattern(sentences)) {
            MCQQuestion q = fullFormMCQ(topic, sentences);
            if (q != null) return q; // Return immediately if successful
        }

        // If no acronym found (or generation failed), fall back to random types
        int typeChoice = random.nextInt(5);

        switch (typeChoice) {
            case 0: return definitionMCQ(topic, sentences, allSlides);
            case 1: return trueStatementMCQ(topic, sentences);
            case 2: return notTypeMCQ(topic, slide, allSlides);
            case 3: return fillBlankMCQ(topic, sentences);
            // Case 4 is usually covered by the priority check, but we keep it just in case
            case 4: return fullFormMCQ(topic, sentences);
            default: return trueStatementMCQ(topic, sentences);
        }
    }

    // Helper to detect if we should force Full Form generation
    private boolean containsAcronymPattern(List<String> sentences) {
        for (String s : sentences) {
            // Check for "X stands for Y" or "X (Y)" patterns
            if (s.matches("(?i).*\\b[A-Z]{2,}\\b.*(stands for|is short for|full form is).*")) return true;
            if (s.matches("(?i).*\\b[A-Z]{2,}\\s*\\([A-Z][a-z]+.*\\).*")) return true;
        }
        return false;
    }

    /* ================= 1️⃣ DEFINITION MCQs ================= */

    private MCQQuestion definitionMCQ(String topic, List<String> sentences, List<PPTContent.SlideContent> allSlides) {
        String definition = null;
        for (String s : sentences) {
            if (s.matches("(?i).*(is a|is the|refers to|means|defined as|is an technique|is an architectural).*")) {
                definition = s;
                break;
            }
        }
        if (definition == null) return null;

        String question = "What is " + topic + "?";
        List<String> options = new ArrayList<>();
        options.add(cleanAnswer(definition));

        for (PPTContent.SlideContent other : allSlides) {
            if (options.size() >= 3) break;
            String otherTopic = extractTopic(other);
            if (otherTopic.equals(topic)) continue;

            for (String s : other.getContentPoints()) {
                String cleaned = cleanSentences(Arrays.asList(s)).isEmpty() ? null : cleanSentences(Arrays.asList(s)).get(0);
                if (cleaned != null && cleaned.matches("(?i).*(is a|is the|refers to|means|defined as).*")) {
                    String cleanOpt = cleanAnswer(cleaned);
                    if(!options.contains(cleanOpt)) options.add(cleanOpt);
                    break;
                }
            }
        }

        fillOptionsWithGenerated(options, topic, "definition");
        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(cleanAnswer(definition));
        return buildQuestion(question, options, correctIndex, MCQQuestion.QuestionType.DEFINITION);
    }

    /* ================= 2️⃣ TRUE STATEMENT MCQs ================= */

    private MCQQuestion trueStatementMCQ(String topic, List<String> sentences) {
        if (sentences.size() < 3) return null;

        String question = "Which statement is true about " + topic + "?";
        List<String> shuffled = new ArrayList<>(sentences);
        Collections.shuffle(shuffled, random);

        List<String> options = new ArrayList<>();
        options.add(cleanAnswer(shuffled.get(0)));

        if (shuffled.size() > 1) options.add(cleanAnswer(shuffled.get(1)));
        if (shuffled.size() > 2) options.add(cleanAnswer(shuffled.get(2)));

        while(options.size() < 4) {
            String wrong = makeSimilarButWrong(shuffled.get(0), topic);
            if(!options.contains(wrong)) {
                options.add(wrong);
            } else {
                if(!options.contains("None of the above")) options.add("None of the above");
                break;
            }
        }

        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(cleanAnswer(shuffled.get(0)));
        return buildQuestion(question, options, correctIndex, MCQQuestion.QuestionType.FACT);
    }

    /* ================= 3️⃣ NOT-TYPE MCQs ================= */

    private MCQQuestion notTypeMCQ(String topic, PPTContent.SlideContent currentSlide, List<PPTContent.SlideContent> allSlides) {
        List<String> sentences = cleanSentences(currentSlide.getContentPoints());
        if (sentences.size() < 3) return null;

        String question = "Which of the following is NOT related to " + topic + "?";
        List<String> options = new ArrayList<>();

        for (int i = 0; i < 3 && i < sentences.size(); i++) {
            options.add(cleanAnswer(sentences.get(i)));
        }

        String differentOption = null;
        for (PPTContent.SlideContent other : allSlides) {
            String otherTopic = extractTopic(other);
            if (!otherTopic.equals(topic) && !other.getContentPoints().isEmpty()) {
                List<String> otherSentences = cleanSentences(other.getContentPoints());
                if (!otherSentences.isEmpty()) {
                    differentOption = cleanAnswer(otherSentences.get(0));
                    if(!options.contains(differentOption)) {
                        options.add(differentOption);
                        break;
                    }
                }
            }
        }

        if (options.size() < 4 || differentOption == null) return null;

        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(differentOption);
        return buildQuestion(question, options, correctIndex, MCQQuestion.QuestionType.NOT_QUESTION);
    }

    /* ================= 4️⃣ FILL-IN-THE-BLANK MCQs ================= */

    private MCQQuestion fillBlankMCQ(String topic, List<String> sentences) {
        String targetSentence = null;
        for (String s : sentences) {
            if (s.matches("(?i).*\\b(is|are)\\b.*") && s.split("\\s+").length > 5) {
                targetSentence = s;
                break;
            }
        }
        if (targetSentence == null) return null;

        String[] parts = targetSentence.split("(?i)\\s+(is|are)\\s+", 2);
        if (parts.length < 2) return null;

        String answer = parts[0].trim();
        String restOfSentence = parts[1].trim();
        if (!restOfSentence.endsWith(".")) restOfSentence += ".";

        String question = "_____ is " + restOfSentence;

        List<String> options = new ArrayList<>();
        options.add(answer);

        for (String s : sentences) {
            if (options.size() >= 4) break;
            if (!s.equals(targetSentence)) {
                String keyword = extractKeyword(s);
                if (keyword != null && !options.contains(keyword) && !keyword.equalsIgnoreCase(answer)) {
                    options.add(keyword);
                }
            }
        }

        while (options.size() < 4) {
            String fakeKeyword = generateMeaningfulKeyword(answer, topic);
            if (!options.contains(fakeKeyword)) {
                options.add(fakeKeyword);
            } else {
                if (!options.contains("None of the above")) {
                    options.add("None of the above");
                } else {
                    break;
                }
            }
        }

        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(answer);
        return buildQuestion(question, options, correctIndex, MCQQuestion.QuestionType.FILL_IN_BLANK);
    }

    /* ================= 5️⃣ FULL-FORM MCQs (PRIORITIZED & ROBUST) ================= */

    private MCQQuestion fullFormMCQ(String topic, List<String> sentences) {

        String acronym = null;
        String fullForm = null;

        // Shuffle sentences to ensure if a slide has multiple acronyms,
        // we don't always pick the same first one.
        List<String> shuffledSentences = new ArrayList<>(sentences);
        Collections.shuffle(shuffledSentences, random);

        for (String s : shuffledSentences) {
            // Pattern 1: "MVC stands for Model-View-Controller"
            if (s.matches("(?i).*\\b[A-Z]{2,}\\b.*(stands for|is short for|full form is).*")) {
                String[] words = s.split("\\s+");
                for (String w : words) {
                    if (w.matches("[A-Z]{2,}")) {
                        acronym = w;
                        int patternIndex = -1;
                        int skipLength = 0;
                        if (s.toLowerCase().contains("stands for")) { patternIndex = s.toLowerCase().indexOf("stands for"); skipLength = 11; }
                        else if (s.toLowerCase().contains("is short for")) { patternIndex = s.toLowerCase().indexOf("is short for"); skipLength = 13; }
                        else if (s.toLowerCase().contains("full form is")) { patternIndex = s.toLowerCase().indexOf("full form is"); skipLength = 13; }

                        if (patternIndex != -1) {
                            fullForm = s.substring(patternIndex + skipLength).trim().replaceAll("\\.$", "");
                            break;
                        }
                    }
                }
            }
            if (acronym != null) break;

            // Pattern 2: "AJAX (Asynchronous JavaScript and XML)"
            if (s.matches("(?i).*\\b[A-Z]{2,}\\s*\\([A-Z][a-zA-Z\\s]+\\).*")) {
                String[] words = s.split("\\s+");
                for (String w : words) {
                    if (w.matches("[A-Z]{2,}")) {
                        acronym = w;
                        int openParen = s.indexOf("(");
                        int closeParen = s.indexOf(")");
                        if (openParen != -1 && closeParen != -1 && closeParen > openParen) {
                            String potentialFull = s.substring(openParen + 1, closeParen).trim();
                            // Sanity check: ensure the full form is reasonably long (avoids "ID (User)")
                            if (potentialFull.length() > 3) {
                                fullForm = potentialFull;
                                break;
                            }
                        }
                    }
                }
            }
            if (acronym != null) break;
        }

        if (acronym == null || fullForm == null) return null;

        String question = "What does " + acronym + " stand for?";
        List<String> options = new ArrayList<>();
        options.add(fullForm);

        // --- DECEPTIVE GENERATION (NO REPEAT "NONE OF ABOVE") ---
        int attempts = 0;
        while (options.size() < 4 && attempts < 50) {
            String deceptive = generateDeceptiveFullForm(fullForm);

            // Critical: Don't let the acronym itself be an option
            if (deceptive.equalsIgnoreCase(acronym)) {
                attempts++;
                continue;
            }

            if (!deceptive.equalsIgnoreCase(fullForm) && !options.contains(deceptive)) {
                options.add(deceptive);
            }
            attempts++;
        }

        // Only add None of the above if strict necessity
        while(options.size() < 4) {
            if(!options.contains("None of the above")) options.add("None of the above");
            else break;
        }

        Collections.shuffle(options, random);
        int correctIndex = options.indexOf(fullForm);

        return buildQuestion(question, options, correctIndex, MCQQuestion.QuestionType.FACT);
    }

    /**
     * GENERATES CONVINCING FAKE FULL FORMS
     * Input: "Model View Controller" -> Output: "Module Virtual Connector"
     */
    private String generateDeceptiveFullForm(String correctFullForm) {
        String[] words = correctFullForm.split("[\\s\\-]+");
        StringBuilder deceptive = new StringBuilder();

        boolean changeAll = random.nextBoolean();
        int wordToChangeIndex = random.nextInt(words.length);

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String cleanWord = word.replaceAll("[^a-zA-Z]", "");

            if (cleanWord.isEmpty()) {
                deceptive.append(word);
                continue;
            }

            boolean shouldChange = changeAll || (i == wordToChangeIndex);

            if (shouldChange) {
                char firstChar = Character.toUpperCase(cleanWord.charAt(0));
                List<String> candidates = TECH_TERMS.get(firstChar);

                if (candidates != null && !candidates.isEmpty()) {
                    String replacement = candidates.get(random.nextInt(candidates.size()));
                    if (replacement.equalsIgnoreCase(cleanWord)) {
                        replacement = candidates.get(random.nextInt(candidates.size()));
                    }
                    deceptive.append(replacement);
                } else {
                    deceptive.append(word);
                }
            } else {
                deceptive.append(word);
            }

            if (i < words.length - 1) {
                if (correctFullForm.contains("-")) deceptive.append("-");
                else deceptive.append(" ");
            }
        }
        return deceptive.toString();
    }

    /* ================= HELPER METHODS ================= */

    private void fillOptionsWithGenerated(List<String> options, String topic, String type) {
        while (options.size() < 4) {
            String filler;
            if (type.equals("definition")) {
                filler = generateConfusingDefinition(topic, options.get(0));
            } else {
                filler = generatePlausibleWrongAnswer(topic);
            }

            if (!options.contains(filler)) {
                options.add(filler);
            } else {
                if (!options.contains("None of the above")) {
                    options.add("None of the above");
                }
                break;
            }
        }
    }

    private String extractTopic(PPTContent.SlideContent slide) {
        String title = slide.getTitle();
        if (title != null && !title.isEmpty()) {
            title = title.replaceAll("(?i)(introduction of|concept of|using)\\s+", "").trim();
            return title;
        }
        List<String> points = slide.getContentPoints();
        if (!points.isEmpty()) {
            String[] words = points.get(0).split("\\s+");
            return words.length > 0 ? words[0] : "the topic";
        }
        return "the topic";
    }

    private List<String> cleanSentences(List<String> raw) {
        List<String> cleaned = new ArrayList<>();
        for (String s : raw) {
            if (s.matches(".*[<>{}\\[\\]].*") || s.trim().isEmpty()) continue;
            if (s.length() < 15) continue;
            s = s.replaceAll("\\s+", " ").trim();
            s = s.replaceAll("^[*\\-•]+\\s*", "");
            if (s.split("\\s+").length < 4) continue;
            if (!s.endsWith(".") && !s.endsWith("?") && !s.endsWith("!")) {
                s += ".";
            }
            cleaned.add(s);
        }
        return cleaned;
    }

    private String cleanAnswer(String text) {
        if (text == null || text.isEmpty()) return "";
        text = text.replaceAll("\\s+", " ").trim();
        text = text.replaceAll("^[*\\-•]+\\s*", "");
        text = text.replaceAll("[.:;,!?]+$", "");
        String[] words = text.split("\\s+");
        if (words.length > 12) {
            text = String.join(" ", Arrays.copyOf(words, 12));
        }
        return text.trim();
    }

    private String extractKeyword(String sentence) {
        String[] words = sentence.split("\\s+");
        for (String w : words) {
            w = w.replaceAll("[^a-zA-Z0-9]", "");
            if (w.length() > 3 && Character.isUpperCase(w.charAt(0)) && isMeaningfulKeyword(w)) {
                return w;
            }
        }
        if (words.length > 0) {
            String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
            if (isMeaningfulKeyword(firstWord)) {
                return firstWord;
            }
        }
        return null;
    }

    private boolean isMeaningfulKeyword(String word) {
        if (word == null || word.length() < 3) return false;
        String[] garbage = {"the", "and", "for", "are", "but", "not", "you", "all", "can",
                "her", "was", "one", "our", "out", "day", "get", "has", "him", "what", "when",
                "his", "how", "its", "may", "new", "now", "old", "see", "two", "uses", "used",
                "way", "who", "boy", "did", "let", "put", "say", "she", "too", "defined",
                "use", "this", "that", "from", "they", "been", "have", "were"};
        for (String g : garbage) {
            if (word.equalsIgnoreCase(g)) return false;
        }
        return word.matches(".*[a-zA-Z].*");
    }

    private String generateMeaningfulKeyword(String correctAnswer, String topic) {
        String[] webTerms = {"Controller", "Model", "View", "Handler", "Service", "Component",
                "Module", "Framework", "Pattern", "Interface", "Protocol", "Method",
                "Request", "Response", "Client", "Server", "Browser", "Application",
                "Database", "Session", "Cookie", "Authentication", "Validation"};
        String generated;
        int attempts = 0;
        do {
            generated = webTerms[random.nextInt(webTerms.length)];
            attempts++;
        } while (generated.equalsIgnoreCase(correctAnswer) && attempts < 10);
        return generated;
    }

    private MCQQuestion buildQuestion(String question, List<String> options,
                                      int correctIndex, MCQQuestion.QuestionType type) {
        MCQQuestion mcq = new MCQQuestion();
        mcq.setQuestion(question);
        mcq.setOptions(options);
        mcq.setCorrectAnswerIndex(correctIndex);
        mcq.setType(type);
        mcq.setDifficulty("MEDIUM");
        String explanation = options.get(correctIndex);
        if (!explanation.endsWith(".") && !explanation.equals("None of the above")) {
            explanation += ".";
        }
        mcq.setExplanation(explanation);
        return mcq;
    }

    private String makeSimilarButWrong(String correctStatement, String topic) {
        String[] confusingVerbs = {"enables", "prevents", "limits", "restricts", "allows", "blocks"};
        String[] confusingTerms = {"client", "server", "browser", "application", "system", "framework"};
        String wrong = correctStatement;
        if (random.nextBoolean() && wrong.contains(" is ")) {
            int verbIndex = random.nextInt(confusingVerbs.length);
            wrong = wrong.replaceFirst("(provides|allows|enables|prevents)", confusingVerbs[verbIndex]);
        }
        if (random.nextBoolean()) {
            int termIndex = random.nextInt(confusingTerms.length);
            wrong = wrong.replaceFirst("(client|server|browser|application)", confusingTerms[termIndex]);
        }
        return wrong;
    }

    private String generatePlausibleWrongAnswer(String topic) {
        String[] patterns = {
                topic + " primarily focuses on server-side operations",
                topic + " is mainly used for database management",
                topic + " handles only client-side rendering",
                topic + " is designed for synchronous operations only",
                topic + " requires manual page reloads",
                topic + " works exclusively with XML data"
        };
        return patterns[random.nextInt(patterns.length)];
    }

    private String generateConfusingDefinition(String topic, String correctAnswer) {
        String[] prefixes = {"A technique that ", "A framework for ", "A pattern used in ", "An approach to ", "A method for "};
        String[] suffixes = {"managing application state", "handling user interactions", "organizing code structure", "improving performance", "enhancing security"};
        return prefixes[random.nextInt(prefixes.length)] + suffixes[random.nextInt(suffixes.length)];
    }
}