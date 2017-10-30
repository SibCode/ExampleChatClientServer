/**
 * Created by Simon Bertschinger on 17.03.2017.
 */
import java.io.*;
import java.net.Socket;

public class ChatServerThread extends Thread
{
    private Server              server    = null;
    private Socket              socket    = null;
    private int                 ID        = -1;
    private DataInputStream     streamIn  = null;
    private DataOutputStream    streamOut = null;
    private String              username  = "anon";
    private String              status    = "none";
    private String              room      = "GLOBAL";

    public ChatServerThread(Server server, Socket socket) {
        super();
        this.server = server;
        this.socket = socket;
        ID = socket.getPort();
        room = "GLOBAL";
    }

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch(IOException ioe) {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            stop();
        }
    }

    public void sendInRoom(String msg, String room) {
        if (this.room.equals(room) || room.equals("GLOBAL")) send(msg);
    }

    public void sendInPrivate(String msg, String user) {
        try  {
            if (this.username.equals(user) || this.ID == Integer.parseInt(user)) send(msg);
        } catch(NumberFormatException nfe) {
            nfe.printStackTrace();
            System.out.println("Going on...");
        } catch(Exception e) {

        }
    }

    public void joinRoom(String room) {
        this.room = room;
    }

    public void leaveRoom(String room) {
        this.room = "GLOBAL";
    }

    public int getID() {
        return ID;
    }

    public void run() {
        System.out.println("Server Thread " + ID + " running.");
        while (true) {
            try {
                server.handle(ID, streamIn.readUTF());
            } catch(IOException ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }

    public void open() throws IOException {
        streamIn = new DataInputStream(new
            BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new
                BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() throws IOException {
        if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null) streamOut.close();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoom() { return room; }
}
