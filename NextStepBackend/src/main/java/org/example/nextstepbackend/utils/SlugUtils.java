package org.example.nextstepbackend.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {

  private SlugUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
  private static final Pattern EDGES_DASHES = Pattern.compile("(?:^-+|-+$)");
  private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

  public static String toSlug(String input) {
    if (input == null || input.isEmpty()) {
      return "";
    }

    // 1. Replace all whitespaces (including tabs/newlines) with a single dash
    String slug = WHITESPACE.matcher(input.trim()).replaceAll("-");

    // 2. Normalize Unicode to separate diacritics from base characters (NFD form)
    // This turns "ố" into "o" + "ˆ" + "´"
    String normalized = Normalizer.normalize(slug, Normalizer.Form.NFD);

    // 3. Remove all diacritical marks (accents) using Regex
    slug = DIACRITICS.matcher(normalized).replaceAll("");

    // 4. Special handling for Vietnamese character 'đ' which Normalizer misses
    slug = slug.replace("đ", "d").replace("Đ", "D");

    // 5. Remove any character that is not a letter, digit, or dash
    slug = NONLATIN.matcher(slug).replaceAll("");

    // 6. Convert to lowercase for SEO-friendly URLs
    slug = slug.toLowerCase(Locale.ENGLISH);

    // 7. Strip leading or trailing dashes caused by special characters at the edges
    slug = EDGES_DASHES.matcher(slug).replaceAll("");

    return slug;
  }
}
