import java.io.Serializable;
import java.util.function.Consumer;

public class Client extends NetworkConnection {

    private String ip;
    private int port;

    // constructor
    public Client(String ip, int port, Consumer<Serializable> callback) {
        super(callback);
        this.ip = ip;
        this.port = port;
    }

    // returns client IP address
    @Override
    protected String getIP() {
        return this.ip;
    }

    // returns client port number
    @Override
    protected int getPort() {
        return this.port;
    }

}
