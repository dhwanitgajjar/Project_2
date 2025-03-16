import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Log {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: log <server_port> <message>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String message = processMessage(args[1]);
        String pow = findProofOfWork(message);
        if (pow == null) {
            System.out.println("error: could not find proof of work");
            System.exit(1);
        }
        String fullMessage = pow + ":" + message;
        try (Socket socket = new Socket("localhost", port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(fullMessage);
            String response = in.readLine();
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("error: connection failed");
        }
    }

    private static String processMessage(String message) {
        return message.replaceAll("\\s+", " ");
    }

    private static String findProofOfWork(String message) {
        byte[] suffix = (":" + message).getBytes(StandardCharsets.UTF_8);
        for (int length = 1; length <= 4; length++) {
            String pow = findPowForLength(length, suffix);
            if (pow != null) return pow;
        }
        return null;
    }

    private static String findPowForLength(int length, byte[] suffix) {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        int[] indices = new int[length];
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] powBytes = new byte[length];
        while (true) {
            for (int i = 0; i < length; i++) {
                powBytes[i] = (byte) chars[indices[i]];
            }
            digest.reset();
            digest.update(powBytes);
            digest.update(suffix);
            byte[] hash = digest.digest();
            if (isValidHash(hash)) {
                return new String(powBytes, StandardCharsets.UTF_8);
            }
            if (!incrementIndices(indices, chars.length)) break;
        }
        return null;
    }

    private static boolean incrementIndices(int[] indices, int max) {
        int i = 0;
        while (i < indices.length) {
            indices[i]++;
            if (indices[i] < max) return true;
            indices[i] = 0;
            i++;
        }
        return false;
    }

    private static boolean isValidHash(byte[] hash) {
        return hash.length >= 3 && hash[0] == 0 && hash[1] == 0 && (hash[2] & 0xFF) <= 0x03;
    }
}