package network;

import java.net.Socket;

public class C_Connection {
    private Client client;
    public C_Connection(Socket client_socket) {
        client = new Client(client_socket);
    }
}
