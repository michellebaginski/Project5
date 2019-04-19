import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class NetworkConnection {

    private ConnThread connthread = new ConnThread();
    private Consumer<Serializable> callback;
    ArrayList<ClientThread> threads = new ArrayList<>();
    int numClients = 0;     // stores number of clients connected
    int threadID = -1;    // stores data sender client-thread's name (ID)

    public void setSenderUsername(String name) {
        this.threads.get(threadID).clientUsername = name;
    }

    public String getSenderUsername(){
        return this.threads.get(threadID).clientUsername;
    }

    // constructor
    public NetworkConnection(Consumer<Serializable> callback) {
        this.callback = callback;
        connthread.setDaemon(true);
    }

    // starts server connection
    public void startConn() throws Exception {
        connthread.start();
    }

    // sends data to specified client thread
    public void send(Serializable data, int index){
        try {
            threads.get(index).out.writeObject(data);
        }
        catch (Exception e){
            System.out.println("Could not send data to client(s)");
            e.printStackTrace();
        }
    }

    // closes server connection
    public void closeConn() throws Exception {
        try {
            connthread.closeConn();
        }
        catch (Exception e){
            System.out.println("Could not close server socket");
        }
    }

    abstract protected int getPort();

    // nested class that creates a server socket and makes it listen for client connections
    class ConnThread extends Thread{
        private ServerSocket serverSocket;
        public void run() {
            try(ServerSocket server = new ServerSocket(getPort())){
                System.out.println("Server created with port number: " + getPort());

                // server is on and is listening for client connections and it will stop listening once 4 clients are connected
                int i = 0;
                while(i < 4){
                    ClientThread t1 = new ClientThread(server.accept());
                    threads.add(t1);
                    numClients++;
                    this.serverSocket = server;
                    t1.setDaemon(true);
                    t1.start();
                    t1.setName(Integer.toString(numClients-1));
                    i++;
                }

            } catch (Exception e) {
                callback.accept("Socket connection Closed");
            }
        }

        public void closeConn() {
            try {
                serverSocket.close();
            }
            catch (Exception e){}
        }
    }

    // nested inner class used for creating instances of
    // client threads that can communicate with the server
    class ClientThread extends Thread{
        private Socket clientSocket;
        private ObjectOutputStream out;
        String clientUsername;
        String opponentUsername;


        // variable to check if the player is free to challenge or occupied in a game
        boolean isAvailable;


        public ClientThread(Socket s){

            this.clientSocket = s;
            this.opponentUsername = null;
        }
        public synchronized void run() {
            try(ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())){
                this.out = out;
                clientSocket.setTcpNoDelay(true);
                isAvailable = true;

                while (true) {
                    Serializable data = (Serializable) in.readObject();
                    callback.accept(data);
                    threadID = Integer.parseInt((this.getName()));
                }

            }
            catch (Exception e){
                callback.accept("Client connection closed");
            }

        }

        public String getClientUsername(){
            return this.clientUsername;
        }
    }
}