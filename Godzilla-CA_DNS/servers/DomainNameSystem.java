package servers;

import ip_database.Converter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.net.ServerSocket;

public abstract class DomainNameSystem {
    public static void init(ServerSocket sck) {
        while (true) {
            try {
                Connection c = new Connection(sck.accept());
                new Thread(new Client_DNS(c)).start(); //riceve la richiesta del client e invia la risposta

            } catch (IOException e) { throw new RuntimeException(e); }
        }
    }

    private static class Client_DNS implements Runnable {
        private Connection client;
        public Client_DNS(Connection c) {
            this.client = c;
        }

        @Override
        public void run() {
            try {
                String server_link = client.wait_for_string(true);
                String request = "error";

                if (server_link.charAt(0) == 'd') { //se il client richiede l'ip conoscendo il link
                    System.out.println("request ip of " + server_link);
                    request = Converter.get_ip_of(server_link.substring(1));
                }
                else if (server_link.charAt(0) == 'r') { //se il client richiede il link conoscendo l'ip.
                    System.out.println("request link of " + server_link);
                    request = Converter.get_link_of(server_link.substring(1));
                }
                else {}

                System.out.println("reply: " + request);
                client.write(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
