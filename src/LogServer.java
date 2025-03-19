import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class LogServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            System.out.println("ServerLogger is running on port: " + port);

            while (true) {
                Socket clientConnection = serverSocket.accept();
                System.out.println("Client connection established.");
                processClientRequest(clientConnection);
            }
        } catch (IOException ex) {
            System.err.println("Server encountered an error: " + ex.getMessage());
        }
    }

    private static void processClientRequest(Socket clientConnection) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            PrintWriter writer = new PrintWriter(clientConnection.getOutputStream(), true);
        ) {
            String incomingMessage = reader.readLine();
            if (incomingMessage == null) {
                writer.println("error: no message received");
                return;
            }
            System.out.println("Received message: " + incomingMessage);

            if (!isProofOfWorkValid(incomingMessage)) {
                writer.println("error: invalid proof of work");
                return;
            }


            int delimiterIndex = incomingMessage.indexOf(": ");
            if (delimiterIndex == -1) {
                writer.println("error: malformed message");
                return;
            }
            String logContent = incomingMessage.substring(delimiterIndex + 2);


            String previousHash = "start";
            File logFile = new File("log.txt");
            File headerFile = new File("loghead.txt");
            if (logFile.exists()) {
                if (!headerFile.exists()) {
                    writer.println("error: header file missing");
                    return;
                }
                try (BufferedReader headerReader = new BufferedReader(new FileReader(headerFile))) {
                    previousHash = headerReader.readLine();
                    if (previousHash == null) previousHash = "start";
                }
            }


            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logEntry = timestamp + " - " + previousHash + " " + logContent;

            String newHash = generateHash(logEntry);


            try (BufferedWriter logWriter = new BufferedWriter(new FileWriter("log.txt", true))) {
                logWriter.write(logEntry);
                logWriter.newLine();
            }


            try (BufferedWriter headerWriter = new BufferedWriter(new FileWriter("loghead.txt"))) {
                headerWriter.write(newHash);
            }

            writer.println("ok");
            System.out.println("Response sent: ok");
        } catch (IOException ex) {
            System.err.println("Error processing client request: " + ex.getMessage());
        } finally {
            try {
                clientConnection.close();
            } catch (IOException ex) {
                System.err.println("Error closing client connection: " + ex.getMessage());
            }
        }
    }

    private static boolean isProofOfWorkValid(String message) {
        try {
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedMessage = sha256Digest.digest(message.getBytes(StandardCharsets.UTF_8));
            return checkLeadingZeros(hashedMessage, 22);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("SHA-256 algorithm is unavailable: " + ex.getMessage());
            return false;
        }
    }

    private static boolean checkLeadingZeros(byte[] hash, int requiredZeroBits) {
        int bitCount = 0;
        for (byte b : hash) {
            if (b == 0) {
                bitCount += 8;
            } else {
                bitCount += Integer.numberOfLeadingZeros(b & 0xFF) - 24;
                break;
            }
            if (bitCount >= requiredZeroBits) return true;
        }
        return bitCount >= requiredZeroBits;
    }

    private static String generateHash(String input) {
        try {
            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256Digest.digest(input.getBytes(StandardCharsets.UTF_8));
            String encodedHash = Base64.getEncoder().encodeToString(hashBytes);
            return encodedHash.substring(encodedHash.length() - 24);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("SHA-256 algorithm is unavailable: " + ex.getMessage());
            return "";
        }
    }
}
