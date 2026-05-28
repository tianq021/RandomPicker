package com.randompicker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class NamePickerService {

    private final Random random = new Random();

    public List<String> parseNames(String text, String splitMode, String customSplit) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String normalized = switch (splitMode) {
            case "newline" -> text.replace("\r\n", "\n").replace('\r', '\n');
            case "comma" -> text.replace('，', ',');
            case "space" -> text.replace('\t', ' ');
            case "semicolon" -> text.replace('；', ';');
            case "custom" -> customSplit == null || customSplit.isEmpty()
                    ? text.replace("\r\n", "\n").replace('\r', '\n')
                    : text.replace(customSplit, "\n");
            default -> text.replace("\r\n", "\n")
                    .replace('\r', '\n')
                    .replace('，', '\n')
                    .replace(',', '\n')
                    .replace('、', '\n')
                    .replace('；', '\n')
                    .replace(';', '\n')
                    .replace('\t', '\n')
                    .replace(' ', '\n');
        };

        String delimiter = switch (splitMode) {
            case "comma" -> ",";
            case "space" -> " ";
            case "semicolon" -> ";";
            default -> "\n";
        };

        String[] parts = normalized.split(java.util.regex.Pattern.quote(delimiter));
        Set<String> uniqueNames = new LinkedHashSet<>();

        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) {
                uniqueNames.add(name);
            }
        }

        return new ArrayList<>(uniqueNames);
    }

    public int parseCount(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public List<String> pickRandomNames(List<String> names, int count) {
        List<String> shuffled = new ArrayList<>(names);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, count);
    }
}
