import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class CheckLog {
    public static void main(String[] args) {
        Path logPath = Paths.get("log.txt");
        Path headPath = Paths.get("loghead.txt");
        if (!Files.exists(logPath) || !Files.exists(headPath)) {
            System.out.println("failed: log files missing");
            System.exit(1);
        }
        try {
            List<String> lines = Files.readAllLines(logPath);
            if (lines.isEmpty()) {
                System.out.println("failed: empty log file");
                System.exit(1);
            }
            String expectedHash = "start";
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(" - ", 2);
                if (parts.length < 2) {
                    System.out.println("failed: invalid log format at line " + (i + 1));
                    System.exit(1);
                }
                String currentHash = parts[1].split(" ")[0];
                if (i == 0 && !currentHash.equals("start")) {
                    System.out.println("failed: initial hash is not 'start'");
                    System.exit(1);
                } else if (i > 0 && !currentHash.equals(expectedHash)) {
                    System.out.println("failed: hash mismatch at line " + i);
                    System.exit(1);
                }
                expectedHash = computeHash(line);
                if (i == lines.size() - 1) {
                    String headHash = new String(Files.readAllBytes(headPath), StandardCharsets.UTF_8).trim();
                    if (!expectedHash.equals(headHash)) {
                        System.out.println("failed: head hash mismatch");
                        System.exit(1);
                    }
                }
            }
            System.out.println("Valid!");
            System.exit(0);
        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String computeHash(String line) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(line.getBytes(StandardCharsets.UTF_8));
        String base64 = Base64.getEncoder().encodeToString(hash);
        return base64.length() >= 24 ? base64.substring(base64.length() - 24) : base64;
    }
}