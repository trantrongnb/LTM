import java.util.*;
import java.io.*;
import java.net.*;
public class rac{
    public static void main(String [] args){
        String host="36.50.135.242";
        int port=2206;
        String studentcode="B23DCCN850";
        String questioncode="81HwZgHK";
        try(Socket socket=new Socket(host,port)){
            socket.setSoTimeout(5000);
            System.out.println("Connect thanh cong");
            InputStream in=socket.getInputStream();
            OutputStream os=socket.getOutputStream();
            String msg=studentcode+";"+questioncode;
            os.write(msg.getBytes("UTF-8"));
            os.flush();

            byte [] buffer=new byte[4096];
            int bytesRead=is.read(buffer);
            String data=new String(buffer,0,bytesRead,"UTF-8").trim();
            System.out.println("Nhan duoc"+ data);
            String [] parts=data.split(",");
            List<Integer> numbers=new ArrayList<>();
            for(String p: parsts){
                numbers.add(Integer.parseInt(p.trim()));
            }
            

        } catch (SocketTimeoutException e){
            System.out.println("timeout");
        } catch (IOException e){
            System.out.println("error connection")
        }
    }
}