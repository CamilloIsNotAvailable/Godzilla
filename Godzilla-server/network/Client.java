package network;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Client implements Runnable {
    private Connection connection;
    private String username;
    public Client(Connection c) {
        this.connection = c;
    }

    @Override
    public void run() {
        try {
            System.out.println("nuova connessione");
            connection.write(Net_listener.server_info); //invia informazioni del server ed il suo certificato
            connection.write(Net_listener.certificate);
            System.out.println("inviato certificate");

            byte[] session_key_bytes = Net_listener.decoder.doFinal(connection.wait_for_bytes(true)); //riceve la chiave AES per la sessione
            SecretKey session_key = new SecretKeySpec(session_key_bytes, "AES");
            Cipher encoder = Cipher.getInstance("AES");
            Cipher decoder = Cipher.getInstance("AES");

            encoder.init(Cipher.ENCRYPT_MODE, session_key);
            decoder.init(Cipher.DECRYPT_MODE, session_key);
            connection.set_cipher(decoder, encoder); //inizia a cifrare la connessione utilizzando la session key appena ricevuta
            System.out.println("instaurata connessione sicura");

            username = login();
            Net_listener.connected_client.put(username, this);

            System.out.println("end");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private String login() throws IllegalBlockSizeException, IOException, BadPaddingException {
        String username = connection.wait_for_string(false); //il tempo necessario perché arrivino dipende molto dalla persona che si sta collegando, quindi non viene messo un timer
        byte[] psw_hash = connection.wait_for_bytes(true);

        if (username.charAt(0) == 'r') { //se deve registrare un nuovo utente
            System.out.println("register -> " + username.substring(1));
            Net_listener.register_account(username.substring(1), psw_hash);
            connection.write("\001");

            return username;
        }
        else if (username.charAt(0) == 'l') { //se deve eseguire il login in un account
            if (Net_listener.exist(username.substring(1), psw_hash)) {
                System.out.println("login -> " + username.substring(1));
                connection.write("\001");

                return username;
            }
            else {
                connection.write("\000");
                return null;
            }
        }
        else {
            connection.write("\000");
            return null;
        }
    }
}
