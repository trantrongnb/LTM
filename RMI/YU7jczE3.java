package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Interface từ xa - phải nằm trong package RMI để khớp với Server
interface DataService extends Remote {
    public Object requestData(String studentCode, String qCode) throws RemoteException;
    public void submitData(String studentCode, String qCode, Object data) throws RemoteException;
}

public class YU7jczE3 {

    // c. Tìm các vị trí đỉnh cục bộ (1-indexed)
    public static List<Integer> findLocalPeaks(int[] arr) {
        List<Integer> peaks = new ArrayList<>();
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            boolean leftOk  = (i == 0)     || arr[i] > arr[i - 1]; // đầu mảng: chỉ cần lớn hơn bên phải
            boolean rightOk = (i == n - 1) || arr[i] > arr[i + 1]; // cuối mảng: chỉ cần lớn hơn bên trái
            if (leftOk && rightOk) {
                peaks.add(i + 1); // chỉ số bắt đầu từ 1
            }
        }
        return peaks;
    }

    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String studentCode = "B23DCCN850";
        String qCode = "YU7jczE3";

        try {
            // a. Kết nối RMI Registry và lấy stub của DataService
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            DataService dataService = (DataService) registry.lookup("RMIDataService");

            // b. Triệu gọi requestData để nhận mảng số nguyên từ Server
            Object received = dataService.requestData(studentCode, qCode);
            int[] arr = (int[]) received;
            System.out.println("Received: " + Arrays.toString(arr));

            // c. Tìm các vị trí đỉnh cục bộ
            List<Integer> peaks = findLocalPeaks(arr);
            System.out.println("Local peaks (1-indexed): " + peaks);

            // d. Gửi List<Integer> các vị trí đỉnh lên Server
            dataService.submitData(studentCode, qCode, peaks);
            System.out.println("Submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // e. Kết thúc chương trình (tự động khi main() trả về)
    }
}
