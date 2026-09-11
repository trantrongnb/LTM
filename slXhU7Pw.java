import java.io.*;
import java.net.*;
import java.util.*;

public class Bai3 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2206;
        String studentCode = "B23DCCN850";
        String questionCode = "slXhU7Pw";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            // Set timeout 5s chung cho tất cả các bài
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công!");

            // KHỞI TẠO LUỒNG BYTE THÔ (Đúng yêu cầu đề bài: InputStream/OutputStream)
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            // a. Gửi mã sinh viên và mã câu hỏi (Dạng mảng byte)
            String msg = studentCode + ";" + questionCode;
            os.write(msg.getBytes("UTF-8"));
            os.flush();
            System.out.println("Đã gửi: " + msg);

            // b. Nhận dữ liệu từ server
            byte[] buffer = new byte[4096];
            int bytesRead = is.read(buffer);
            if (bytesRead > 0) {
                // Dịch byte array thành String
                String data = new String(buffer, 0, bytesRead, "UTF-8").trim();
                System.out.println("Nhận được: " + data);

                // c. Xử lý tính toán: Tìm số lớn thứ hai và index xuất hiện đầu tiên
                String[] parts = data.split(",");
                List<Integer> numbers = new ArrayList<>();
                for (String p : parts) {
                    numbers.add(Integer.parseInt(p.trim()));
                }

                // Dùng HashSet để loại bỏ trùng lặp và List để sort giảm dần
                List<Integer> uniqueNums = new ArrayList<>(new HashSet<>(numbers));
                uniqueNums.sort(Collections.reverseOrder());

                int secondMax = uniqueNums.size() >= 2 ? uniqueNums.get(1) : uniqueNums.get(0);
                int index = numbers.indexOf(secondMax);

                String result = secondMax + "," + index;
                System.out.println("Kết quả: " + result);

                // Gửi kết quả lên server (Dạng mảng byte)
                os.write(result.getBytes("UTF-8"));
                os.flush();
                System.out.println("Đã gửi kết quả thành công");
            }

        } catch (SocketTimeoutException e) {
            System.out.println("Lỗi: Quá thời gian giao tiếp 5s");
        } catch (IOException e) {
            System.out.println("Lỗi kết nối mạng: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Dữ liệu server gửi không phải số nguyên");
        }
    }
}
