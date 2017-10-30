import java.net.*;
import java.io.*;

public class Server implements Runnable {

    private ChatServerThread clients[] = new ChatServerThread[50];
    private ServerSocket server = null;
    private Thread       thread = null;
    private int clientCount = 0;

    public Server(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
        } catch(IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null)
        {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch(IOException ioe) {
                System.out.println("Server accept error: " + ioe); stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++)
            if (clients[i].getID() == ID)
                return i;
            return -1;
    }

    public synchronized void handle(int ID, String input) {
        int userID = findClient(ID);
        if (input.equals("..bye")) {
            for (int i = 0; i < clientCount; i++)
                clients[i].send("@GLOBAL [SERVER]: CLIENT [" + clients[userID].getUsername() + "#" + ID + "] LEFT ");
            clients[userID].send("..bye");
            remove(ID);
        } else {
            String[] inputs = input.split("\\.\\.");
            if (inputs[0].toUpperCase().equals("SET_USERNAME")) {
                clients[userID].setUsername(inputs[1]);
                clients[userID].send("@YOU [SERVER] : YOUR USERNAME WAS SET TO: "+clients[userID].getUsername());
                for (int i = 0; i < clientCount; i++)
                    clients[i].send("@GLOBAL [SERVER]: CLIENT [" + clients[userID].getUsername() + "#" + ID + "] JOINED @GLOBAL");
            } else if (inputs[0].toUpperCase().equals("SET_STATUS")) {
                clients[userID].setStatus(inputs[1]);
                clients[userID].send("@YOU [SERVER] : YOUR STATUS WAS SET TO: "+clients[userID].getStatus());
                for (int i = 0; i < clientCount; i++)
                    clients[i].send("@GLOBAL [" + clients[userID].getUsername() + "#" + ID + "] STATUS > "+clients[userID].getStatus());
            } else if (inputs[0].toUpperCase().equals("JOIN")) {
                clients[userID].joinRoom(inputs[1]);
                for (int i = 0; i < clientCount; i++)
                    clients[i].send("@GLOBAL [SERVER]: CLIENT [" + clients[userID].getUsername() + "#" + ID + "] JOINED @" + clients[userID].getRoom());
            } else if (inputs[0].toUpperCase().equals(("PRIVATE"))) {
                for (int i = 0; i < clientCount; i++)
                    clients[i].sendInPrivate("@YOU [" + clients[userID].getUsername() + "#" + ID + "] : " + inputs[2],inputs[1]);
            } else if (inputs[0].toUpperCase().equals(("PUBLIC"))) {
                for (int i = 0; i < clientCount; i++)
                    clients[i].send("@GLOBAL [" + clients[userID].getUsername() + "#" + ID + "] : " + inputs[1]);
            } else
                for (int i = 0; i < clientCount; i++)
                    clients[i].sendInRoom("@"+clients[userID].getRoom()+" ["+clients[userID].getUsername()+"#"+ID+"]: "+input,clients[userID].getRoom());
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ChatServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount-1)
                for (int i = pos+1; i < clientCount; i++)
                    clients[i-1] = clients[i];
            clientCount--;
            try {
                toTerminate.close();
            } catch(IOException ioe) {
                System.out.println("Error closing thread: " + ioe);
            }
            toTerminate.stop();
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch(IOException ioe) {
                System.out.println("Error opening thread: " + ioe);
            }
        } else
            System.out.println("Client refused: maximum " + clients.length + " reached.");
    }

    public static void main(String args[]) {
        Server server = null;
        if (args.length != 1) {
            System.out.println("USAGE: java -jar ChatServer.jar port");
            server = new Server(3141);
        } else {
            server = new Server(Integer.parseInt(args[0]));
        }
    }
}

