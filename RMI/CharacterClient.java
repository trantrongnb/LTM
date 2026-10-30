package RMI;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// Interface từ xa - phải nằm trong package RMI để khớp với Server
interface CharacterService extends Remote {
    public String requestCharacter(String studentCode, String qCode) throws RemoteException;
    public void submitCharacter(String studentCode, String qCode, String strSubmit) throws RemoteException;
}

public class CharacterClient {  // ← Đổi thành tên file / mã câu hỏi

    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String studentCode = "B23DCCN850";
        String qCode = "REPLACE_WITH_QCODE";  // ← Đổi thành mã câu hỏi thực tế

        try {
            // a. Kết nối RMI Registry và lấy stub của CharacterService
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            CharacterService characterService = (CharacterService) registry.lookup("RMICharacterService");

            // a. Triệu gọi requestCharacter để nhận chuỗi từ Server
            String received = characterService.requestCharacter(studentCode, qCode);
            System.out.println("Received: " + received);

            // b. Mã hóa URL theo quy tắc URI path encoding
            // Tương đương: new java.net.URI(null, null, s, null).toASCIIString()
            String encoded = new URI(null, null, received, null).toASCIIString();
            System.out.println("Encoded:  " + encoded);

            // c. Gửi chuỗi đã mã hóa lên Server
            characterService.submitCharacter(studentCode, qCode, encoded);
            System.out.println("Submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // d. Kết thúc chương trình (tự động khi main() trả về)
    }
}
