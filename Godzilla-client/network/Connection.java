package network;

import gui.TempPanel;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class Connection {
    public static final int E_GEN = -3;
    public static final int E_INVIP = -2;
    public static final int E_CONR = -1;

    private static final int PORT = 31415;
    public static int start_connection_with(String server_ip) {
        try {
            Socket s = new Socket(server_ip, PORT);

            //riceve certificato CA + chiave pubblica del server

            return 0;
        } catch (UnknownHostException e) {
            TempPanel.show_msg("l'indirizzo ip è stato inserito male, non ha senso");
            return E_INVIP;
        } catch (ConnectException e) {
            TempPanel.show_msg("connessione rifiutata, il server non è stato acceso");
            return E_CONR;
        } catch (IOException e) {
            TempPanel.show_msg("impossibile connettersi al server");
            return E_GEN;
        }
    }

    public static void disconnect() {

    }
}
