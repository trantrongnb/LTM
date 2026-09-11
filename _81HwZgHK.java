import java.io.*;
import java.net.*;
import java.util.*;

public class _81HwZgHK {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2206;
        String studentCode = "B23DCCN850";
        String questionCode = "81HwZgHK";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            // Set timeout 5s chung cho tất cả các bài
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công!");

            // KHỞI TẠO LUỒNG BYTE THÔ (Raw Byte Stream) do code Python trước đó dùng recv/sendall raw
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
                String data = new String(buffer, 0, bytesRead, "UTF-8").trim();
                System.out.println("Nhận được: " + data);

                // c. Xử lý tính toán: Tìm khoảng cách nhỏ nhất giữa 2 số
                String[] parts = data.split(",");
                List<Integer> numbers = new ArrayList<>();
                for (String p : parts) {
                    numbers.add(Integer.parseInt(p.trim()));
                }

                Collections.sort(numbers);
                int minDistance = Integer.MAX_VALUE;
                int firstNum = -1;
                int secondNum = -1;

                // Xét 2 số liền kề vì đã sort
                for (int i = 0; i < numbers.size() - 1; i++) {
                    int first = numbers.get(i);
                    int second = numbers.get(i + 1);
                    int distance = second - first;

                    // Cập nhật khi khoảng cách bé hơn, hoặc bằng nhưng cặp số lớn hơn
                    if (distance < minDistance || 
                       (distance == minDistance && (second > secondNum || (second == secondNum && first > firstNum)))) {
                        minDistance = distance;
                        firstNum = first;
                        secondNum = second;
                    }
                }

                String result = minDistance + "," + firstNum + "," + secondNum;
                System.out.println("Kết quả: " + result);

                // Gửi kết quả lên server
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