import java.io.*;
import java.net.*;
import java.util.*;

public class Bai4 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2208;
        String studentCode = "B23DCCN850";
        String questionCode = "GaZ5T4B4";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            // Set timeout 5s chung cho tất cả các bài
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công!");

            // KHỞI TẠO LUỒNG VĂN BẢN (Text Stream)
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            // a. Gửi mã sinh viên và mã câu hỏi (kèm \n)
            String msg = studentCode + ";" + questionCode;
            writer.write(msg + "\n");
            writer.flush();
            System.out.println("Đã gửi: " + msg);

            // b. Nhận dữ liệu từ server
            String data = reader.readLine();
            if (data != null) {
                data = data.trim();
                System.out.println("Nhận được: " + data);

                // c. Xử lý tính toán: Tìm ký tự chữ/số xuất hiện > 1 lần theo thứ tự xuất hiện
                // Dùng LinkedHashMap để giữ nguyên thứ tự chèn
                Map<Character, Integer> counts = new LinkedHashMap<>();
                for (char ch : data.toCharArray()) {
                    if (Character.isLetterOrDigit(ch)) {
                        counts.put(ch, counts.getOrDefault(ch, 0) + 1);
                    }
                }

                StringBuilder resultBuilder = new StringBuilder();
                for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
                    if (entry.getValue() > 1) {
                        resultBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                    }
                }

                String result = resultBuilder.toString();
                System.out.println("Kết quả: " + result);

                // Gửi kết quả lên server
                writer.write(result + "\n");
                writer.flush();
                System.out.println("Đã gửi kết quả thành công");
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Lỗi: Quá thời gian giao tiếp 5s");
        } catch (IOException e) {
            System.out.println("Lỗi kết nối mạng: " + e.getMessage());
        }
    }
}
