package network;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Net_listener {
    public static byte[] certificate;
    public static byte[] server_info;
    public static Cipher decoder;

    public static Map<String, Client> connected_client = new LinkedHashMap<>();
    private static Map<String, byte[]> users_credentials = new LinkedHashMap<>();

    public static void start(int port) {
        try {
            init_ce_server_info();
            init_users_credentialis();

            ServerSocket ss = new ServerSocket(port);

            while(true) {
                Connection client_conn = new Connection(ss.accept()); //attende il collegamento di un client
                new Thread(new Client(client_conn)).start();
            }
        } catch (BindException e) {
            System.out.println("porta già in utilizzo, impossibile inizializzare il server");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static void register_account(String username, byte[] psw_hash) {
        users_credentials.put(username, psw_hash);
    }

    public static boolean exist(String username, byte[] psw_hash) {
        int diff = Arrays.compare(psw_hash, users_credentials.get(username));
        return diff == 0;
    }

    public static void save_credentials() throws IOException {
        String file_credentials = "";
        String[] usernames = users_credentials.keySet().toArray(new String[0]);
        for (String username : usernames) {
            file_credentials += username + ";" + Base64.getEncoder().encodeToString(users_credentials.get(username)) + "\n";
        }

        new FileOutputStream("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_server/src/clients_credentials.dat").write(file_credentials.getBytes());
    }

    private static void init_ce_server_info() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {
        certificate = new FileInputStream("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_server/src/certificate.dat").readAllBytes();
        server_info = new FileInputStream("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_server/src/server_info.dat").readAllBytes();

        KeyFactory key_f = KeyFactory.getInstance("RSA");
        decoder = Cipher.getInstance("RSA");
        decoder.init(Cipher.DECRYPT_MODE, key_f.generatePrivate(new PKCS8EncodedKeySpec(new FileInputStream("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_server/src/private.key").readAllBytes())));
    }

    private static void init_users_credentialis() throws IOException {
        String file_txt = new String(new FileInputStream("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_server/src/clients_credentials.dat").readAllBytes());
        Pattern pat = Pattern.compile("[;\n]");
        /*
        * in server_info.dat sono contenuti tutti gli account creati su questo server come:
        * <username1>;<password hash1>
        * <username2>;<password hash2>
        * ...
        * ...
        *
        * viene diviso il testo in corrispondenza di ";" o "\n" in modo da trovare un array:
        * {<username1>, <password hash1>, <username2>, <password hash2>, ...}
         */

        String[] credentials = pat.split(file_txt);
        if (credentials.length != 1) { //se il file non è vuoto
            for (int i = 0; i < credentials.length - 1; i += 2) {
                users_credentials.put(credentials[i], Base64.getDecoder().decode(credentials[i+1]));
            }
        }
    }
}
