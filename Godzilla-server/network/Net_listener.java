package network;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Net_listener {
    public static void start(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);

            while(true) {
                Socket client_s = ss.accept(); //attende il collegamento di un client

                //deve inviare certificato CA + chiave pubblica
                System.out.println("connesso!");

            }
        } catch (BindException e) {
            System.out.println("porta già in utilizzo, impossibile inizializzare il server");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
