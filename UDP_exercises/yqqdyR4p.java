import java.io.*;
import java.net.*;
import java.util.*;

public class yqqdyR4p {
    public static void main(String [] args){
        // 1. Khai báo thông tin Server
        String serverIp="36.50.135.242";
        int serverPort=2207;
        String studentCode="B23DCCN850";
        String qCode="yqqdyR4p";
        
        DatagramSocket socket=null;
        try{
            // 2. Khởi tạo kết nối UDP
            // Tạo ra một cổng mạng (socket) ở phía Client
            socket=new DatagramSocket();
            // Cài đặt thời gian chờ tối đa khi nhận dữ liệu là 5 giây để tránh bị treo vĩnh viễn
            socket.setSoTimeout(5000);
            // Chuyển đổi IP (dạng text) sang đối tượng InetAddress mà mạng UDP hiểu được
            InetAddress serverAddress=InetAddress.getByName(serverIp);

            // 3. Gửi thông điệp lần 1 (Mã Sinh Viên)
            // Ghép chuỗi gửi đi: ;MãSinhViên;MãCâuHỏi
            String message=";"+studentCode+";"+qCode;
            // UDP truyền bằng byte[], nên phải chuyển chuỗi thành byte array
            byte[] sendData=message.getBytes();
            // Đóng gói mảng byte cùng địa chỉ IP và Port của Server để chuẩn bị gửi
            DatagramPacket sendPacket=new DatagramPacket(sendData,sendData.length,serverAddress,serverPort);
            // Gửi gói tin đi
            socket.send(sendPacket);
            System.out.println("sent: "+message);


            // 4. Nhận thông điệp từ Server
            // Khởi tạo một bộ nhớ đệm rỗng 1024 bytes
            byte[] receiveData=new byte[1024];
            DatagramPacket receivePacket=new DatagramPacket(receiveData,receiveData.length);
            // Đợi và nhận gói tin từ Server ném về
            socket.receive(receivePacket);
            // Trích xuất dữ liệu mảng byte đó và ép về lại dạng chuỗi String
            String receiveStr=new String(receivePacket.getData(),0,receivePacket.getLength());
            System.out.println("received:"+receiveStr);

            // 5. Cắt chuỗi và lưu các số đã xuất hiện
            // Tách thành 3 phần: [requestId, n, chuỗi_các_số]
            String [] parts=receiveStr.split(";");
            if(parts.length>=3){
                String requestId=parts[0]; // Lấy requestId
                int n=Integer.parseInt(parts[1]); // Lấy giới hạn n
                String [] numbersStr=parts[2].split(","); // Cắt các con số A1, A2... ra
                
                // Mảng đánh dấu. present[x] = true nghĩa là số x đã xuất hiện
                // Cấp phát n+1 để mảng chứa được số lớn nhất là n
                boolean [] present=new boolean[n+1];
                for(String s:numbersStr){
                    if(!s.trim().isEmpty()){
                        int num=Integer.parseInt(s.trim());
                        if(num>=1&&num<=n){
                            // Số nào có mặt thì đánh dấu là true
                            present[num]=true;
                        }
                    }
                }
                
                // 6. Tìm các số còn thiếu và ghép lại thành chuỗi
                // StringBuilder dùng để nối các số rất nhanh
                StringBuilder sb=new StringBuilder();
                sb.append(requestId).append(";");
                // Biến 'first' dùng để kiểm soát việc in dấu phẩy (không in phẩy ở số bị thiếu đầu tiên)
                boolean first=true;
                
                // Vòng lặp duyệt từ 1 đến n để tìm số bị thiếu
                for(int i = 1; i <= n; i++){
                    // Nếu present[i] vẫn là false (số i bị thiếu)
                    if(!present[i]){
                        if(!first){
                            sb.append(",");
                        }
                        sb.append(i);
                        first=false;
                    }
                }
                
                // 7. Gửi trả kết quả cuối cùng cho Server
                String ans=sb.toString();
                byte[] sendResponseData=ans.getBytes();
                // Đóng gói mảng byte chứa đáp án vào DatagramPacket
                DatagramPacket sendResponse=new DatagramPacket(sendResponseData,sendResponseData.length,serverAddress,serverPort);
                // Bắn ngược về Server
                socket.send(sendResponse);
                System.out.println("sent:"+ans);
            }

        } catch (Exception e){
            // Bắt lỗi nếu mất mạng, sai format chuỗi hoặc timeout
            e.printStackTrace();
        } finally {
            // 8. Đóng Socket (Dọn dẹp)
            if(socket != null) {
                // Luôn phải đóng port (socket) sau khi dùng xong
                socket.close();
            }
        }
    }
}