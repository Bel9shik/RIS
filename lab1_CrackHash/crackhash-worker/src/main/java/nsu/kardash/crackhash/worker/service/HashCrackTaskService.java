package nsu.kardash.crackhash.worker.service;

import org.paukov.combinatorics3.Generator;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Перебор слов по диапазону (PartNumber, PartCount) с помощью combinatoricslib3.
 * Не держит все комбинации в памяти — использует итератор.
 */
@Service
public class HashCrackTaskService {

    /**
     * Итерация по словам в глобальном порядке (длина 1, затем 2, ...) без загрузки всех в память.
     * Диапазон задаётся PartNumber и PartCount — пространство слов делится поровну между воркерами.
     */
    public List<String> crack(String hash, int maxLength, String alphabet, int partNumber, int partCount) {
        List<Character> chars = alphabet.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        int alphabetSize = chars.size();

        long total = 0;
        for (int len = 1; len <= maxLength; len++) {
            total += (long) Math.pow(alphabetSize, len);
        }
        if (total == 0) {
            return List.of();
        }

        long partSize = total / partCount;
        long startIndex = (long) partNumber * partSize;
        long endIndex = (partNumber == partCount - 1) ? total : startIndex + partSize;

        List<String> results = new ArrayList<>();
        long globalIndex = 0;

        for (int length = 1; length <= maxLength; length++) {
            var iterator = Generator.permutation(chars)
                    .withRepetitions(length)
                    .iterator();
            while (iterator.hasNext()) {
                if (globalIndex >= endIndex) {
                    return results;
                }
                String word = iterator.next().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining());
                if (globalIndex >= startIndex) {
                    String md5 = md5(word);
                    if (hash.equalsIgnoreCase(md5)) {
                        results.add(word);
                    }
                }
                globalIndex++;
            }
        }
        return results;
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }
}
