import java.net.*;
import java.io.*;

public class Client implements Runnable {

    private Socket           socket    = null;
    private Thread           thread    = null;
    private DataInputStream  console   = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client    = null;

    public Client(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try
        {  socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            start();
        }
        catch(UnknownHostException uhe)
        {  System.out.println("Host unknown: " + uhe.getMessage()); }
        catch(IOException ioe)
        {  System.out.println("Unexpected exception: " + ioe.getMessage()); }
    }

    public void run() {
        while (thread != null) {
            try {
                streamOut.writeUTF(console.readLine());
                streamOut.flush();
            } catch(IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        }
    }

    public void handle(String msg) {
        if (msg.equals("..bye")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            stop();
        } else System.out.println(msg);
    }

    public void start() throws IOException {
        console   = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
        System.out.println("Please enter your username: ");
        try {
            streamOut.writeUTF("SET_USERNAME.."+console.readLine());
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println("Couldn't set username.");
        }
        System.out.println("Please enter your status: ");
        try {
            streamOut.writeUTF("SET_STATUS.."+console.readLine());
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println("Couldn't set status.");
        }
        streamOut.flush();
        System.out.println("Type ..bye to leave");
        System.out.println("JOIN..YOURGROUP to join YOURGROUP");
        System.out.println("PRIVATE..USERNAME..MESSAGE to send private message");
        System.out.println("PRIVATE..USERID..MESSAGE alternatively");
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console   != null)  console.close();
            if (streamOut != null)  streamOut.close();
            if (socket    != null)  socket.close();
        } catch(IOException ioe) {
            System.out.println("Error closing ...");
        }
        client.close();
        client.stop();
    }

    public static void main(String args[])
    {
        Client client = null;
        if (args.length != 2) {
            System.out.println("USAGE: java -jar ChatClient.jar host port");
            client = new Client("127.0.0.1",3141);
        } else {
            client = new Client(args[0], Integer.parseInt(args[1]));
        }
    }
}

