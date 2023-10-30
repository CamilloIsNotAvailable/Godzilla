import network.Net_listener;

public class Godzilla_server {
    private static final int PORT = 31415;

    public static void main(String[] args) {
        start_msg();
        Net_listener.start(PORT);
    }

    private static void start_msg() {
        System.out.println("Godzilla_server: start");
    }
}
