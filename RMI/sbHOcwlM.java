package RMI;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// Interface từ xa - phải nằm trong package RMI
interface ObjectService extends Remote {
    Serializable requestObject(String studentCode, String qCode) throws RemoteException;
    void submitObject(String studentCode, String qCode, Serializable object) throws RemoteException;
}

// Lớp dữ liệu - phải khớp chính xác với server (package, serialVersionUID, tên & kiểu thuộc tính)
class TicketSla implements Serializable {
    private static final long serialVersionUID = 20260517L;

    private String ticketId;
    private String priority;       // LOW, MEDIUM, HIGH, CRITICAL
    private int openedHoursAgo;
    private boolean breached;
    private String action;

    public TicketSla() { }

    public TicketSla(String ticketId, String priority, int openedHoursAgo) {
        this.ticketId       = ticketId;
        this.priority       = priority;
        this.openedHoursAgo = openedHoursAgo;
    }

    public String  getTicketId()       { return ticketId; }
    public void    setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String  getPriority()       { return priority; }
    public void    setPriority(String priority) { this.priority = priority; }

    public int     getOpenedHoursAgo() { return openedHoursAgo; }
    public void    setOpenedHoursAgo(int openedHoursAgo) { this.openedHoursAgo = openedHoursAgo; }

    public boolean isBreached()        { return breached; }
    public void    setBreached(boolean breached) { this.breached = breached; }

    public String  getAction()         { return action; }
    public void    setAction(String action) { this.action = action; }

    @Override
    public String toString() {
        return "TicketSla{ticketId=" + ticketId
                + ", priority=" + priority
                + ", openedHoursAgo=" + openedHoursAgo
                + ", breached=" + breached
                + ", action=" + action + "}";
    }
}

public class sbHOcwlM {

    public static void main(String[] args) {
        String serverIp   = "36.50.135.242";
        int    serverPort = 1099;
        String studentCode = "B23DCCN850";
        String qCode       = "sbHOcwlM";

        try {
            // a. Kết nối RMI Registry và lấy stub của ObjectService
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            ObjectService objectService = (ObjectService) registry.lookup("RMIObjectService");

            // b. Nhận đối tượng TicketSla từ Server
            TicketSla ticket = (TicketSla) objectService.requestObject(studentCode, qCode);
            System.out.println("Received: " + ticket);

            String priority       = ticket.getPriority();
            int    openedHoursAgo = ticket.getOpenedHoursAgo();

            // c. Xác định breached theo ngưỡng priority
            boolean breached = switch (priority) {
                case "CRITICAL" -> openedHoursAgo > 2;
                case "HIGH"     -> openedHoursAgo > 8;
                case "MEDIUM"   -> openedHoursAgo > 24;
                case "LOW"      -> openedHoursAgo > 72;
                default         -> false;
            };
            ticket.setBreached(breached);

            // c. Xác định action
            String action;
            if (!breached) {
                action = "MONITOR";
            } else if (priority.equals("CRITICAL") || openedHoursAgo > 96) {
                action = "ESCALATE_L2";
            } else {
                action = "ESCALATE_L1";
            }
            ticket.setAction(action);

            System.out.println("Updated:  " + ticket);

            // d. Gửi đối tượng đã cập nhật lên Server
            objectService.submitObject(studentCode, qCode, ticket);
            System.out.println("Submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // e. Kết thúc chương trình (tự động khi main() trả về)
    }
}
