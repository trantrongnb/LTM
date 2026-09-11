import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HoR9aKZe {

    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2211;
        String studentCode = "B23DCCN850";
        String qCode = "HoR9aKZe";

        try (SocketChannel channel = SocketChannel.open()) {
            channel.socket().setSoTimeout(5000); // Đặt timeout 5s
            channel.connect(new InetSocketAddress(host, port));

            // a. Gửi mã sinh viên và mã câu hỏi
            String msg = studentCode + ";" + qCode;
            writeFrame(channel, msg);
            System.out.println("Đã gửi thông tin: " + msg);

            // b. Nhận dữ liệu gồm đúng 2 frame liên tiếp
            String frame1 = readFrame(channel);
            String frame2 = readFrame(channel);

            // Nối 2 payload lại để được chuỗi JSON hoàn chỉnh
            String json = frame1 + frame2;
            System.out.println("JSON nhận được: " + json);

            // c. Trích xuất event, user, ok bằng Regex
            String event = "";
            // Tìm giá trị của trường "event"
            Matcher mEvent = Pattern.compile("\"event\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (mEvent.find()) {
                event = mEvent.group(1);
            }

            String user = "";
            // Tìm giá trị của trường "user"
            Matcher mUser = Pattern.compile("\"user\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (mUser.find()) {
                user = mUser.group(1);
            }

            String okStr = "";
            // Tìm giá trị boolean của trường "ok"
            Matcher mOk = Pattern.compile("\"ok\"\\s*:\\s*(true|false)").matcher(json);
            if (mOk.find()) {
                okStr = mOk.group(1);
            }
            String ok = "true".equals(okStr) ? "1" : "0";

            // Gửi lại kết quả theo định dạng "event=<event>;user=<user>;ok=<0|1>"
            String result = "event=" + event + ";user=" + user + ";ok=" + ok;
            System.out.println("Kết quả cần gửi: " + result);
            
            writeFrame(channel, result);
            System.out.println("Hoàn thành gửi kết quả.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi dữ liệu theo protocol: 4 byte độ dài (int) + payload
     */
    private static void writeFrame(SocketChannel channel, String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + payloadBytes.length);
        buffer.putInt(payloadBytes.length);
        buffer.put(payloadBytes);
        buffer.flip();
        
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    /**
     * Nhận dữ liệu theo protocol: 4 byte độ dài (int) + payload
     */
    private static String readFrame(SocketChannel channel) throws IOException {
        // Đọc 4 byte để lấy độ dài
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        readFully(channel, lengthBuffer);
        lengthBuffer.flip();
        int length = lengthBuffer.getInt();

        // Đọc phần payload dựa trên độ dài vừa lấy được
        ByteBuffer payloadBuffer = ByteBuffer.allocate(length);
        readFully(channel, payloadBuffer);
        payloadBuffer.flip();

        byte[] payloadBytes = new byte[length];
        payloadBuffer.get(payloadBytes);
        return new String(payloadBytes, StandardCharsets.UTF_8);
    }

    /**
     * Hàm dùng vòng lặp đảm bảo đọc đủ số byte cần thiết vào buffer
     */
    private static void readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                throw new EOFException("Server đã đóng kết nối trước khi đọc đủ dữ liệu");
            }
        }
    }
}
