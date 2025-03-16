import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class LogServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        System.out.println(serverSocket.getLocalPort());
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String received = in.readLine();
            if (received == null) {
                out.println("error: empty message");
                return;
            }
            if (!isValidHash(received)) {
                out.println("error: invalid proof-of-work");
                return;
            }
            int colonIndex = received.indexOf(':');
            if (colonIndex == -1) {
                out.println("error: missing colon");
                return;
            }
            String message = received.substring(colonIndex + 1);
            String logLine = processLogEntry(message);
            if (logLine == null) {
                out.println("error: log files corrupted");
                return;
            }
            appendToLog(logLine);
            out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidHash(String received) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(received.getBytes(StandardCharsets.UTF_8));
            return hash[0] == 0 && hash[1] == 0 && (hash[2] & 0xFF) <= 0x03;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String processLogEntry(String message) {
        Path logPath = Paths.get("log.txt");
        Path headPath = Paths.get("loghead.txt");
        String prevHash;
        try {
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
                Files.write(headPath, "start".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                prevHash = "start";
            } else {
                if (!Files.exists(headPath)) return null;
                prevHash = new String(Files.readAllBytes(headPath), StandardCharsets.UTF_8).trim();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = LocalDateTime.now().format(formatter);
            String logLine = timestamp + " - " + prevHash + " " + message;
            String newHash = computeHash(logLine);
            Files.write(headPath, newHash.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return logLine;
        } catch (IOException e) {
            return null;
        }
    }

    private static String computeHash(String logLine) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(logLine.getBytes(StandardCharsets.UTF_8));
            String base64 = Base64.getEncoder().encodeToString(hash);
            return base64.length() >= 24 ? base64.substring(base64.length() - 24) : base64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void appendToLog(String logLine) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("log.txt"), StandardOpenOption.APPEND)) {
            writer.write(logLine);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}