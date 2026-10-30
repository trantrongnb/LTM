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

public class xzs6fU7q {
    static final String ENDPOINT = "http://36.50.135.242:2221/ObjectService";
    static final String NS = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT = "B23DCCN850";
    // "svg" trong ban sao de bai la ky hieu giao dien, khong thuoc ma cau hoi.
    static final String QCODE = "xzs6fU7q";

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

    static org.w3c.dom.Element requiredElement(org.w3c.dom.Element parent, String name)
            throws IOException {
        NodeList elements = parent.getElementsByTagNameNS("*", name);
        if (elements.getLength() != 1) {
            throw new IOException("Phan hoi thieu hoac trung truong: " + name);
        }
        return (org.w3c.dom.Element) elements.item(0);
    }

    static class Customer {
        String customerId;
        String location;
        int purchaseCount;
        float totalSpent;

        static Customer fromXml(org.w3c.dom.Element element) throws IOException {
            Customer customer = new Customer();
            customer.customerId = requiredElement(element, "customerId").getTextContent();
            NodeList locations = element.getElementsByTagNameNS("*", "location");
            if (locations.getLength() > 0) {
                org.w3c.dom.Element location = (org.w3c.dom.Element) locations.item(0);
                String nil = location.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "nil");
                if (!"true".equals(nil) && !"1".equals(nil)) {
                    customer.location = location.getTextContent();
                }
            }
            customer.purchaseCount = Integer.parseInt(
                    requiredElement(element, "purchaseCount").getTextContent().trim());
            customer.totalSpent = Float.parseFloat(
                    requiredElement(element, "totalSpent").getTextContent().trim());
            return customer;
        }

        String toXml() {
            // Giu du location va dung thu tu xs:sequence cua Customer trong WSDL.
            return "<data><customerId>" + escapeXml(customerId) + "</customerId>"
                    + (location == null ? "" : "<location>" + escapeXml(location) + "</location>")
                    + "<purchaseCount>" + purchaseCount + "</purchaseCount>"
                    + "<totalSpent>" + totalSpent + "</totalSpent></data>";
        }

        @Override
        public String toString() {
            return "Customer{customerId=" + customerId + ", location=" + location
                    + ", purchaseCount=" + purchaseCount + ", totalSpent=" + totalSpent + "}";
        }
    }

    static List<Customer> process(List<Customer> customers) {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.purchaseCount >= 6 && customer.totalSpent >= 4000) {
                result.add(customer);
            }
        }
        result.sort(Comparator.comparingDouble((Customer customer) -> customer.totalSpent)
                .reversed().thenComparing(customer -> customer.customerId));
        return result;
    }

    public static void main(String[] args) throws Exception {
        String studentCode = args.length > 0 ? args[0] : STUDENT;
        String qCode = args.length > 1 ? args[1] : QCODE;
        String parameters = "<studentCode>" + escapeXml(studentCode) + "</studentCode>"
                + "<qCode>" + escapeXml(qCode) + "</qCode>";

        // b. Nhan danh sach doi tuong Customer.
        Document response = soapCall("<tns:requestListCustomer>" + parameters
                + "</tns:requestListCustomer>");
        NodeList messages = response.getElementsByTagNameNS(NS, "requestListCustomerResponse");
        if (messages.getLength() != 1) {
            throw new IOException("Phan hoi khong chua requestListCustomerResponse.");
        }
        NodeList values = ((org.w3c.dom.Element) messages.item(0))
                .getElementsByTagNameNS("*", "return");
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < values.getLength(); i++) {
            customers.add(Customer.fromXml((org.w3c.dom.Element) values.item(i)));
        }
        System.out.println("So khach hang nhan: " + customers.size());

        // c. Loc va sap xep, giu nguyen thuoc tinh cua cac khach hang duoc chon.
        List<Customer> result = process(customers);
        System.out.println("So khach hang nop: " + result.size());
        StringBuilder data = new StringBuilder();
        for (Customer customer : result) {
            System.out.println(customer);
            data.append(customer.toXml());
        }

        // d. Moi Customer la mot the data rieng; danh sach rong khong co the data.
        Document submitted = soapCall("<tns:submitListCustomer>" + parameters
                + data + "</tns:submitListCustomer>");
        if (submitted.getElementsByTagNameNS(NS, "submitListCustomerResponse")
                .getLength() != 1) {
            throw new IOException("Phan hoi khong chua submitListCustomerResponse.");
        }
        System.out.println("Da gui ket qua, server phan hoi thanh cong.");
        // e. Cac stream va ket noi da dong trong soapCall; ket thuc client.
    }
}
