package org.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Loads word resources and returns random standard words, hard words, and boss paragraphs.
 */
public class WordGenerator {

    private final List<String> standardWords;
    private final List<String> bossParagraphs;
    private final Random random;

    public WordGenerator(String wordsFilePath, String paragraphsFilePath) {
        this.standardWords = new ArrayList<>();
        this.bossParagraphs = new ArrayList<>();
        this.random = new Random();

        loadLinesFromFile(wordsFilePath, standardWords);
        loadLinesFromFile(paragraphsFilePath, bossParagraphs);
    }

    private void loadLinesFromFile(String resourcePath, List<String> targetList) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        targetList.add(line.trim());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load resource: " + resourcePath);
            e.printStackTrace();
        }
    }

    public String getRandomWord() {
        return standardWords.get(random.nextInt(standardWords.size()));
    }

    public String getRandomHardWord() {
        List<String> hardWords = standardWords.stream()
                .filter(word -> word.length() >= 7)
                .toList();

        if (!hardWords.isEmpty()) {
            return hardWords.get(random.nextInt(hardWords.size()));
        }

        return getRandomWord();
    }

    public String getRandomBossParagraph() {
        return bossParagraphs.get(random.nextInt(bossParagraphs.size()));
    }
}