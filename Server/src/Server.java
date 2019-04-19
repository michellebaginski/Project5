import java.io.Serializable;
import java.util.function.Consumer;

public class Server extends NetworkConnection {

    private int port;

    // constructor
    public Server(int port, Consumer<Serializable> callback) {
        super(callback);
        this.port = port;
    }

    // returns server port number
    @Override
    protected int getPort() {
        return port;
    }

}
