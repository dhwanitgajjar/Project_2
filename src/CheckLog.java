import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CheckLog {
    public static void main(String[] args) {
        if (args.length != 0) {
            System.out.println("Usage: java LogValidator");
            System.exit(1);
        }

        File logFile = new File("log.txt");
        File headerFile = new File("loghead.txt");

        if (!logFile.exists()) {
            System.out.println("error: log file missing");
            System.exit(1);
        }
        if (!headerFile.exists()) {
            System.out.println("error: header pointer file missing");
            System.exit(1);
        }

        try (
            BufferedReader logReader = new BufferedReader(new FileReader(logFile));
            BufferedReader headerReader = new BufferedReader(new FileReader(headerFile))
        ) {
            String logLine;
            String previousHash = "start";
            int lineCount = 1;
            String storedHash = headerReader.readLine();

            while ((logLine = logReader.readLine()) != null) {
                String[] components = logLine.split(" - ", 2);
                if (components.length != 2) {
                    System.out.println("error: malformed entry at line " + lineCount);
                    System.exit(1);
                }
                String remainingContent = components[1];
                int separatorIndex = remainingContent.indexOf(' ');
                if (separatorIndex == -1) {
                    System.out.println("error: malformed entry at line " + lineCount);
                    System.exit(1);
                }
                String currentHash = remainingContent.substring(0, separatorIndex);

                if (!currentHash.equals(previousHash)) {
                    System.out.println("error: hash mismatch at line " + (lineCount - 1));
                    System.exit(1);
                }

                previousHash = generateHash(logLine);
                lineCount++;
            }

            if (!previousHash.equals(storedHash)) {
                System.out.println("error: header hash mismatch");
                System.exit(1);
            }

            System.out.println("Log is valid");
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("error: problem reading files - " + ex.getMessage());
            System.exit(1);
        }
    }

    private static String generateHash(String input) {
        try {
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedData = sha256Digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String encodedHash = Base64.getEncoder().encodeToString(hashedData);
            return encodedHash.substring(encodedHash.length() - 24);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("SHA-256 algorithm unavailable: " + ex.getMessage());
            return "";
        }
    }
}
