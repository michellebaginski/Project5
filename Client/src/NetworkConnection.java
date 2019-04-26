import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class NetworkConnection {

    private ClientThread clientThread = new ClientThread();
    private Consumer<Serializable> callback;
    public String clientUsername;


    // constructor
    public NetworkConnection(Consumer<Serializable> callback) {
        this.callback = callback;
        clientThread.setDaemon(true);
    }

    // starts client thread
    public  void startConn() throws Exception{
        try {
            clientThread.start();
        }
        catch(Exception e){
            System.out.println("Could not start client socket with port number "
                    + getPort() + " and IP address " + getIP());
        }
    }

    // sends data to server
    public  void send(Serializable data){
        try{
            this.clientThread.out.writeObject(data);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // closes the client thread
    public  void closeConn() throws Exception{
        try{
            if(clientThread.socket != null && !clientThread.socket.isClosed())
                clientThread.socket.close();
        }
        catch(Exception e){
            System.out.println("Could not start client socket with port number "
                    + getPort() + " and IP address " + getIP());
        }
    }

    abstract protected String getIP();
    abstract protected int getPort();

    // nested class that creates a client socket
    class ClientThread extends Thread{
        private Socket socket;
        private ObjectOutputStream out;

        public void run() {
            try(Socket socket = new Socket(getIP(), getPort());
                ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())){

                this.socket = socket;
                this.out = out;
                socket.setTcpNoDelay(true);
                clientThread.out.writeObject("I am connected");

                // client thread is ON and is waiting for data inputs from server
                while(true) {
                    Serializable data = (Serializable) in.readObject();
                    callback.accept(data);
                }

            }
            catch(Exception e) {
                callback.accept(" client Connection Closed");
            }
        }
    }

}
