import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class odiwIxIQ {
    static final String ENDPOINT = "http://36.50.135.242:2221/DataService";
    static final String NS = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT = "B23DCCN850";
    // "svg" trong ban sao de bai la ky hieu giao dien, khong thuoc ma cau hoi.
    static final String QCODE = "odiwIxIQ";

    static String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    static Document soapCall(String body) throws Exception {
        String envelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:tns=\"" + NS + "\">"
                + "<soapenv:Body>" + body + "</soapenv:Body></soapenv:Envelope>";

        HttpURLConnection connection = (HttpURLConnection) URI.create(ENDPOINT)
                .toURL().openConnection();
        try {
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            connection.setRequestProperty("SOAPAction", "\"\"");

            try (OutputStream output = connection.getOutputStream()) {
                output.write(envelope.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            InputStream stream = status < 400
                    ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) {
                throw new IOException("Server tra ve HTTP " + status + ", khong co noi dung.");
            }

            Document response;
            try (InputStream input = stream) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                response = factory.newDocumentBuilder().parse(input);
            }

            NodeList faults = response.getElementsByTagNameNS(
                    "http://schemas.xmlsoap.org/soap/envelope/", "Fault");
            if (faults.getLength() > 0) {
                throw new IOException("SOAP Fault: " + faults.item(0).getTextContent());
            }
            if (status >= 400) {
                throw new IOException("Server tra ve HTTP " + status);
            }
            return response;
        } finally {
            // Dong ket noi sau moi loi goi, ke ca khi gap loi.
            connection.disconnect();
        }
    }

    static boolean isPrime(int number) {
        if (number < 2) return false;
        if (number == 2) return true;
        if (number % 2 == 0) return false;
        // Dung phep chia de tranh tran so khi kiem tra can bac hai.
        for (int divisor = 3; divisor <= number / divisor; divisor += 2) {
            if (number % divisor == 0) return false;
        }
        return true;
    }

    static String calculate(List<Integer> numbers) {
        int primeCount = 0;
        long checksum = 0;
        for (int i = 0; i < numbers.size(); i++) {
            int value = numbers.get(i);
            if (isPrime(value)) primeCount++;
            // Nhan bang long va lay modulo tung buoc de tranh tran so.
            checksum = Math.floorMod(checksum + (i + 1L) * value, 100000L);
        }
        return "primeCount=" + primeCount + ";checksum=" + checksum;
    }

    public static void main(String[] args) throws Exception {
        // Co the truyen ma sinh vien va ma cau hoi tu dong lenh.
        String studentCode = args.length > 0 ? args[0] : STUDENT;
        String qCode = args.length > 1 ? args[1] : QCODE;
        String parameters = "<studentCode>" + escapeXml(studentCode) + "</studentCode>"
                + "<qCode>" + escapeXml(qCode) + "</qCode>";

        // b. Nhan List<Integer> tu getData.
        Document response = soapCall("<tns:getData>" + parameters + "</tns:getData>");
        if (response.getElementsByTagNameNS(NS, "getDataResponse").getLength() == 0) {
            throw new IOException("Phan hoi khong chua getDataResponse.");
        }
        NodeList values = response.getElementsByTagNameNS("*", "return");
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < values.getLength(); i++) {
            numbers.add(Integer.parseInt(values.item(i).getTextContent().trim()));
        }
        System.out.println("Du lieu nhan: " + numbers);

        // c. Dem so nguyen to va tinh checksum theo vi tri ban dau.
        String result = calculate(numbers);
        System.out.println("Ket qua nop: " + result);

        // d. Gui mot chuoi duy nhat, dung ten khoa va dau phan cach cua de bai.
        Document submitted = soapCall("<tns:submitDataString>" + parameters
                + "<data>" + escapeXml(result) + "</data>"
                + "</tns:submitDataString>");
        if (submitted.getElementsByTagNameNS(NS, "submitDataStringResponse")
                .getLength() != 1) {
            throw new IOException("Phan hoi khong chua submitDataStringResponse.");
        }
        System.out.println("Da gui ket qua, server phan hoi thanh cong.");
        // e. Cac stream va ket noi da dong trong soapCall; ket thuc client.
    }
}
