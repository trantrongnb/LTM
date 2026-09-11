
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class tP37XdSs {

    private final static String STUDENT_ID = "B23DCCN850";
    private final static String QUESTION_ID = "tP37XdSs";

    public static void initClient() {
        try (Socket mySocket = new Socket()) {

            mySocket.connect(new InetSocketAddress("36.50.135.242", 2210), 5000);

            GZIPOutputStream gos = new GZIPOutputStream(mySocket.getOutputStream(), true);

            // first message
            String firstMessage = STUDENT_ID + ";" + QUESTION_ID;
            // gos phảI bật true cho syncFlush và thêm endline ở cuối data
            gos.write((firstMessage + "\n").getBytes(StandardCharsets.UTF_8));
            gos.flush();

            // task 2
            GZIPInputStream gis = new GZIPInputStream(mySocket.getInputStream());
            byte[] buffer = new byte[8192];
            int n = gis.read(buffer);
            String input = new String(buffer, 0, n, StandardCharsets.UTF_8).trim();
            System.out.println(input);

            // task 3
            String result = reverseStr(input);
            String resultBase64 = Base64.getEncoder().encodeToString(result.getBytes(StandardCharsets.UTF_8));
            gos.write((result + "|" + resultBase64 + "\n").getBytes(StandardCharsets.UTF_8));
            gos.flush();

            mySocket.close();

        } catch (UnknownHostException e) {
            System.out.println(e.toString() + ":" + e.getMessage());
        } catch (IOException e) {
            System.out.println(e.toString() + ":" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initClient();
    }

    private static String reverseStr(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = input.length() - 1; i >= 0; i--) {
            result.append(input.charAt(i));
        }
        return result.toString();
    }
}
