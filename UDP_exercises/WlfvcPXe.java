package UDP;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class WlfvcPXe {
    private DatagramSocket mySocket;
    private InetAddress serverAddress;
    private int serverPort=2209;
    private byte[] requestID= new byte[8];

    public void connection() throws IOException{
        serverAddress = InetAddress.getByName("36.50.135.242");
        mySocket= new DatagramSocket();
    }
    public void send(String str) throws IOException{
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(data,data.length,serverAddress,serverPort);
        mySocket.send(packet);
    }
    public Student receive() throws IOException,ClassNotFoundException{
        byte[] buffer = new byte[66535];
        DatagramPacket packet= new DatagramPacket(buffer,buffer.length);
        mySocket.receive(packet);
        if (packet.getLength() <= 8) {
            throw new IOException("Goi tin khong du du lieu Student");
        }
        System.arraycopy(
                packet.getData(),
                packet.getOffset(),
                requestID,
                0,8
        );
        ByteArrayInputStream byteIn = new ByteArrayInputStream(
                packet.getData(),
                packet.getOffset()+8,
                packet.getLength()-8
        );
        try(ObjectInputStream objectIn = new ObjectInputStream(byteIn)) {
            return (Student) objectIn.readObject();
        }
    }
    public void solve(Student student) {
        String name= student.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten sinh vien rong");
        }
        String[] words= name.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder email= new StringBuilder(words[words.length-1]);
        for (int i=0;i< words.length-1;i++) {
            email.append(words[i].charAt(0));
        }
        email.append("@ptit.edu.vn");
        student.setEmail(email.toString());
        for (int i=0; i< words.length;i++) {
            words[i]= words[i].substring(0,1).toUpperCase(Locale.ROOT)+words[i].substring(1);
        }
        student.setName(String.join(" ",words));
    }
    public void send(Student student) throws IOException{
        byte[] objectData;
        ByteArrayOutputStream byteOut= new ByteArrayOutputStream();
        try(ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)){
            objectOut.writeObject(student);
            objectOut.flush();
            objectData = byteOut.toByteArray();
        }
        byte[] data =new byte[8+objectData.length];
        System.arraycopy(requestID,0,data,0,8);
        System.arraycopy(objectData,0,data,8,objectData.length);
        DatagramPacket packet= new DatagramPacket(
data,data.length,serverAddress,serverPort
        );
        mySocket.send(packet);
    }
    public void close() {
        if (mySocket!=null) {
            mySocket.close();
        }
    }

    public static void main(String[] args) {
        WlfvcPXe client = new WlfvcPXe();
        try {
            client.connection();
            client.send(";B23DCCN850;WlfvcPXe");
            Student student=client.receive();
            System.out.println("Ten nhan "+student.getName());
            client.solve(student);
            client.send(student);
            System.out.println("Ten da sua: " + student.getName());
            System.out.println("Email: " + student.getEmail());
        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
        }finally {
            client.close();
        }

    }
}

class Student implements Serializable {
    private static final long serialVersionUID = 20171107;
    
    private String id;
    private String code;
    private String name;
    private String email;

    public Student(String id, String code, String name, String email) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.email = email;
    }

    public Student(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
