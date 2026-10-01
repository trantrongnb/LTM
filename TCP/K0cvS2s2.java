import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class K0cvS2s2 {

    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2211;
        String studentCode = "B23DCCN850";
        String qCode = "K0cvS2s2";

        try (SocketChannel channel = SocketChannel.open()) {
            channel.socket().setSoTimeout(5000); // Đặt timeout cho socket là 5s
            channel.connect(new InetSocketAddress(host, port));

            // a. Gửi mã sinh viên và mã câu hỏi
            String msg = studentCode + ";" + qCode;
            writeFrame(channel, msg);
            System.out.println("Đã gửi thông tin: " + msg);

            // b. Nhận dữ liệu gồm đúng 3 frame liên tiếp
            String frame1 = readFrame(channel);
            String frame2 = readFrame(channel);
            String frame3 = readFrame(channel);

            // Nối 3 payload lại để được HTTP request hoàn chỉnh
            String httpRequest = frame1 + frame2 + frame3;
            System.out.println("HTTP Request nhận được:\n" + httpRequest);

            // c. Trích xuất METHOD, PATH, HOST
            String[] lines = httpRequest.split("\r\n");
            
            // Dòng đầu tiên luôn có định dạng: METHOD PATH VERSION
            String requestLine = lines[0];
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];
            
            String hostHeader = "";
            // Duyệt qua các dòng để tìm header Host
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].toLowerCase().startsWith("host:")) {
                    hostHeader = lines[i].substring(5).trim();
                    break;
                }
            }

            // Gửi lại kết quả theo định dạng "METHOD;PATH;HOST"
            String result = method + ";" + path + ";" + hostHeader;
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
