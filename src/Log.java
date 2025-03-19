import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Log {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Log <port> <message>");
            return;
        }

        String hostname = "localhost";
        int port = Integer.parseInt(args[0]);
        String message = args[1];

        try (
            Socket socket = new Socket(hostname, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
        
            String sanitizedMessage = message.replaceAll("\\s+", " ").trim();

        
            String pow = generateProofOfWork(sanitizedMessage);
            String outmessage = pow + ": " + sanitizedMessage;

            System.out.println("sending: " + outmessage);
            out.println(outmessage);

        
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
        }
    }

    private static String generateProofOfWork(String message) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder pow = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            int attempts = 0;
            while (true) {
                attempts++;
                String candidate = pow.toString() + ": " + message;
                byte[] hash = digest.digest(candidate.getBytes(StandardCharsets.UTF_8));
                if (hasLeadingZeros(hash, 22)) {
                    return pow.toString();
                }
                
                if (attempts % 100000 == 0) {
                    pow.setLength(0);
                    for (int i = 0; i < attempts / 100000; i++) {
                        pow.append(characters.charAt(0));
                    }
                }
                pow.append(characters.charAt((int) (Math.random() * characters.length())));
                if (pow.length() > 10) {
                    pow.setLength(0);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 not available: " + e.getMessage());
            return "";
        }
    }

    private static boolean hasLeadingZeros(byte[] hash, int requiredZeros) {
        int zeroBits = 0;
        for (byte b : hash) {
            if (b == 0) {
                zeroBits += 8;
            } else {
                zeroBits += Integer.numberOfLeadingZeros(b & 0xFF) - 24;
                break;
            }
            if (zeroBits >= requiredZeros) return true;
        }
        return zeroBits >= requiredZeros;
    }
}