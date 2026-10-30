package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Locale;

// Interface từ xa - phải nằm trong package RMI để khớp với Server
interface DataService extends Remote {
    public Object requestData(String studentCode, String qCode) throws RemoteException;
    public void submitData(String studentCode, String qCode, Object data) throws RemoteException;
}

public class CAHfm00D {

    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String studentCode = "B23DCCN850";
        String qCode = "CAHfm00D";

        try {
            // a. Kết nối RMI Registry và lấy stub của DataService
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            DataService dataService = (DataService) registry.lookup("RMIDataService");

            // b. Nhận chuỗi CSV các số thực từ Server
            Object received = dataService.requestData(studentCode, qCode);
            String csv = (String) received;
            System.out.println("Received: " + csv);

            // Parse CSV thành mảng double
            String[] tokens = csv.split(",");
            int n = tokens.length;
            double[] values = new double[n];
            for (int i = 0; i < n; i++) {
                values[i] = Double.parseDouble(tokens[i].trim());
            }

            // c. Tính trung bình (average)
            double sum = 0;
            for (double v : values) sum += v;
            double avg = sum / n;

            // c. Tính độ lệch chuẩn (standard deviation) - population stddev
            double sumSq = 0;
            for (double v : values) sumSq += (v - avg) * (v - avg);
            double stddev = Math.sqrt(sumSq / n);

            // c. Tính phân vị 95 (p95): vị trí ceil(n * 0.95) - 1 sau khi sắp xếp tăng dần
            double[] sorted = Arrays.copyOf(values, n);
            Arrays.sort(sorted);
            int p95Index = (int) Math.ceil(n * 0.95) - 1;
            double p95 = sorted[p95Index];

            System.out.printf(Locale.US, "Average: %.2f%n", avg);
            System.out.printf(Locale.US, "StdDev:  %.2f%n", stddev);
            System.out.printf(Locale.US, "P95:     %.2f%n", p95);

            // d. Tạo chuỗi kết quả và gửi lên Server
            String result = String.format(Locale.US, "average=%.2f;stddev=%.2f;p95=%.2f", avg, stddev, p95);
            System.out.println("Submitting: " + result);

            dataService.submitData(studentCode, qCode, result);
            System.out.println("Submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // e. Kết thúc chương trình (tự động khi main() trả về)
    }
}
