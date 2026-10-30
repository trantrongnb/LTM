package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

// Interface từ xa - phải nằm trong package RMI để khớp với Server
interface ByteService extends Remote {
    public byte[] requestData(String studentCode, String qCode) throws RemoteException;
    public void submitData(String studentCode, String qCode, byte[] data) throws RemoteException;
}

public class xWUmaIas {

    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String studentCode = "B23DCCN850";
        String qCode = "xWUmaIas";

        try {
            // a. Kết nối đến RMI Registry và lấy stub của ByteService
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            ByteService byteService = (ByteService) registry.lookup("RMIByteService");

            // a. Triệu gọi requestData để nhận mảng byte từ Server
            byte[] data = byteService.requestData(studentCode, qCode);
            System.out.println("Received data (" + data.length + " bytes): " + Arrays.toString(data));

            // b. Tìm phần tử xuất hiện nhiều nhất
            // Dùng LinkedHashMap để bảo toàn thứ tự xuất hiện đầu tiên
            Map<Byte, Integer> countMap = new LinkedHashMap<>();
            for (byte b : data) {
                countMap.put(b, countMap.getOrDefault(b, 0) + 1);
            }

            // Tìm số lần xuất hiện nhiều nhất
            int maxCount = 0;
            for (int count : countMap.values()) {
                if (count > maxCount) {
                    maxCount = count;
                }
            }

            // Tìm phần tử đầu tiên có số lần xuất hiện nhiều nhất (theo thứ tự mảng gốc)
            byte mostFreqByte = 0;
            for (byte b : data) {
                if (countMap.get(b) == maxCount) {
                    mostFreqByte = b;
                    break;
                }
            }

            System.out.println("Most frequent element: " + mostFreqByte + " (appears " + maxCount + " times)");

            // c. Tạo mảng kết quả: [phần_tử_nhiều_nhất, số_lần_xuất_hiện]
            byte[] result = new byte[]{mostFreqByte, (byte) maxCount};
            System.out.println("Result to submit: " + Arrays.toString(result));

            // c. Triệu gọi submitData để gửi kết quả lên Server
            byteService.submitData(studentCode, qCode, result);
            System.out.println("Submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // d. Kết thúc chương trình (tự động khi main() trả về)
    }
}
