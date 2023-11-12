import gui.Server_frame;
import network.Net_listener;
import java.io.IOException;

public class Godzilla_server {
    private static final int PORT = 31415;

    public static void main(String[] args) {
//        Server_frame.init();
        Runtime.getRuntime().addShutdownHook(new Thread(shutdown_hook));
        Net_listener.start(PORT);
    }

    private static Runnable shutdown_hook = new Runnable() {
        @Override
        public void run() {
            try {
                Net_listener.save_credentials();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
}
