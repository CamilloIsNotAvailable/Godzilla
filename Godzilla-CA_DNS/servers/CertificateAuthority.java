package servers;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public abstract class CertificateAuthority {
    private static Cipher prv_decoder;
    private static Cipher prv_encoder;

    public static void init(ServerSocket sck)  {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey key = kf.generatePrivate(new PKCS8EncodedKeySpec(new FileInputStream("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_CA_DNS/src/CAPrivateKey.dat").readAllBytes()));

            prv_decoder = Cipher.getInstance("RSA");
            prv_encoder = Cipher.getInstance("RSA");
            prv_decoder.init(Cipher.DECRYPT_MODE, key);
            prv_encoder.init(Cipher.ENCRYPT_MODE, key);

            while (true) {
                Connection c = new Connection(sck.accept());
                new Thread(new CA_client(c)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CA_client implements Runnable {
        private Connection client;
        public CA_client(Connection c) {
            this.client = c;
        }

        @Override
        public void run() {
            try {
                System.out.println("nuova connessione CA");
                secure_aes(); //riceve una chiave AES e la utilizzerà da ora in poi per cifrare la connessione
                System.out.println("instaurata connessione sicura");

                //riceve tutte le informazioni da mettere all'interno del certificato
                String name = client.wait_for_string(false);
                String link = client.wait_for_string(true);
                String ip = client.get_ip();
                String mail = client.wait_for_string(true);
                String server_pbKey = client.wait_for_string(true); //riceve la chiave pubblica codificata con Base64
                System.out.println("ricevute informazioni server: " + name + ";" + link + ";" + ip + ";" + mail);

                //costruisce il certificato e lo cifra
                byte[] ce = prv_encoder.doFinal((name + ";" + link + ";" + ip + ";" + server_pbKey + ";" + mail).getBytes());
                client.write(ce);
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

        private void secure_aes() throws IllegalBlockSizeException, IOException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
            byte[] aes_key_bytes = client.wait_for_bytes(true);
            aes_key_bytes = prv_decoder.doFinal(aes_key_bytes); //decifra la chiave aes utilizzando la propria chiave privata
            SecretKey aes_key = new SecretKeySpec(aes_key_bytes, "AES");

            Cipher encoder = Cipher.getInstance("AES");
            Cipher decoder = Cipher.getInstance("AES");
            encoder.init(Cipher.ENCRYPT_MODE, aes_key);
            decoder.init(Cipher.DECRYPT_MODE, aes_key);

            client.set_cipher(decoder, encoder);
        }
    }
}
