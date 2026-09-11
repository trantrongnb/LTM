import java.util.*;
import java.io.*;
import java.net.*;

public class rac{
    public static void main(String [] args){
        String host="36.50.135.242";
        int port=2207;
        String stucode="B23DCCN850";
        String quecode="RxexCsEL";
        try (Socket socket=new Socket(host,port)){
            DataInputStream is=new DataInputStream(socket.getInputStream());
            DataOutputStream os=new DataOutputStream(socket.getOutputStream());

            String msg=stucode+";"+quecode;
            os.writeUTF(msg);
            os.flush();
            System.out.println("Da gui"+msg);
            int a=is.readInt();
            int b=is.readInt();
            int sum=a+b;
            int product=a*b;
            os.writeInt(sum);
            os.writeInt(product);
            os.flush();

            } catch (Exception e){
                e.printStackTrace();
            }
    }
}