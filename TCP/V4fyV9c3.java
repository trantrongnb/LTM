import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class V4fyV9c3 {

    private final static String STUDENT_ID = "B23DCCN850";
    private final static String QUESTION_ID = "V4fyV9c3";

    public static void initClient() {
        try (Socket mySocket = new Socket()) {

            mySocket.connect(new InetSocketAddress("36.50.135.242", 2210), 5000);

            // Bật true cho syncFlush
            GZIPOutputStream gos = new GZIPOutputStream(mySocket.getOutputStream(), true);

            // Gửi message 1
            String firstMessage = STUDENT_ID + ";" + QUESTION_ID;
            gos.write((firstMessage + "\n").getBytes(StandardCharsets.UTF_8));
            gos.flush();

            // Nhận dữ liệu (đọc trực tiếp bằng mảng byte để tránh lỗi block của BufferedReader)
            GZIPInputStream gis = new GZIPInputStream(mySocket.getInputStream());
            byte[] buffer = new byte[8192];
            int n = gis.read(buffer);
            String input = new String(buffer, 0, n, StandardCharsets.UTF_8).trim();
            System.out.println("Nhận được: " + input);

            // Xử lý: Sắp xếp các ký tự theo thứ tự từ điển (tăng dần mã ASCII)
            char[] chars = input.toCharArray();
            Arrays.sort(chars);
            String result = new String(chars);
            System.out.println("Kết quả sau sắp xếp: " + result);

            // Gửi kết quả lên server
            gos.write((result + "\n").getBytes(StandardCharsets.UTF_8));
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
}
