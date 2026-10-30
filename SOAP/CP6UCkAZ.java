import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class CP6UCkAZ {
    static final String ENDPOINT = "http://36.50.135.242:2221/CharacterService";
    static final String NS = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT = "B23DCCN850";
    // "svg" trong ban sao de bai la ky hieu giao dien, khong thuoc ma cau hoi.
    static final String QCODE = "CP6UCkAZ";

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

    static String maskLog(String line) {
        // Chi thay gia tri cua dung khoa, giu nguyen user, action va khoang trang.
        return line.replaceAll("(?<!\\S)(email=)\\S+", "$1[EMAIL]")
                .replaceAll("(?<!\\S)(phone=)\\S+", "$1[PHONE]")
                .replaceAll("(?<!\\S)(token=)\\S+", "$1[TOKEN]");
    }

    static int levelOrder(String line) {
        String level = line.trim().split("\\s+", 2)[0];
        switch (level) {
            case "ERROR": return 0;
            case "WARN": return 1;
            case "INFO": return 2;
            default: return 3;
        }
    }

    static List<String> process(List<String> logs) {
        List<String> result = new ArrayList<>();
        for (String line : logs) result.add(maskLog(line));
        // List.sort la stable: cac dong cung muc giu nguyen thu tu nhan.
        result.sort(Comparator.comparingInt(CP6UCkAZ::levelOrder));
        return result;
    }

    public static void main(String[] args) throws Exception {
        String studentCode = args.length > 0 ? args[0] : STUDENT;
        String qCode = args.length > 1 ? args[1] : QCODE;
        String parameters = "<studentCode>" + escapeXml(studentCode) + "</studentCode>"
                + "<qCode>" + escapeXml(qCode) + "</qCode>";

        // b. Nhan danh sach log tu requestStringArray.
        Document response = soapCall("<tns:requestStringArray>" + parameters
                + "</tns:requestStringArray>");
        NodeList messages = response.getElementsByTagNameNS(NS, "requestStringArrayResponse");
        if (messages.getLength() != 1) {
            throw new IOException("Phan hoi khong chua requestStringArrayResponse.");
        }
        NodeList values = ((org.w3c.dom.Element) messages.item(0))
                .getElementsByTagNameNS("*", "return");
        List<String> logs = new ArrayList<>();
        for (int i = 0; i < values.getLength(); i++) {
            logs.add(values.item(i).getTextContent());
        }

        // c. Che thong tin va sap xep ERROR -> WARN -> INFO.
        List<String> result = process(logs);
        System.out.println("So dong nhan: " + logs.size());
        StringBuilder data = new StringBuilder();
        for (String line : result) {
            System.out.println(line);
            data.append("<data>").append(escapeXml(line)).append("</data>");
        }

        // d. Moi chuoi la mot the data rieng, khong ghep thanh mot chuoi.
        Document submitted = soapCall("<tns:submitStringArray>" + parameters
                + data + "</tns:submitStringArray>");
        if (submitted.getElementsByTagNameNS(NS, "submitStringArrayResponse")
                .getLength() != 1) {
            throw new IOException("Phan hoi khong chua submitStringArrayResponse.");
        }
        System.out.println("Da gui ket qua, server phan hoi thanh cong.");
        // e. Cac stream va ket noi da dong trong soapCall; ket thuc client.
    }
}
