package network;

import file_database.Database;
import gui.Godzilla_frame;
import gui.StringVectorOperator;
import gui.TempPanel;

import javax.crypto.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Vector;
import java.util.regex.Pattern;

public abstract class Server {
    public static final int E_INVCE = -4; //error codes
    public static final int E_GEN = -3;
    public static final int E_INVIP = -2;
    public static final int E_CONR = -1;

    public static String mail = "";
    public static String registered_name = "";

    private static final String CA_DNS_IP = "127.0.0.1";
    private static final int CA_DNS_PORT = 9696;

    private static final int SERVER_PORT = 31415;

    private static Connection server;
    private static MessageDigest sha3_hash;

    static { //inizializza sha3_hash
        try {
            sha3_hash = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static int start_connection_with(String link) {
        boolean dns_alive = false; //distingue fra connessione rifiutata dal DNS e dal server

        try {
            String ip;
            if (is_an_ip(link)) { //se viene dato l'indirizzo ip del server a cui collegarsi
                Connection dns = new Connection(CA_DNS_IP, CA_DNS_PORT);
                dns.write("r" + link); //aggiungendo r all'inizio specifichi al dns che vuoi conoscere l'indirizzo del server partendo dal suo ip
                ip = link;

                dns.close();
                dns_alive = true;

                server = new Connection(ip, SERVER_PORT);
            }
            else { //se viene dato il link, si collega al DNS per ricevere l'indirizzo ip
                Connection dns = new Connection(CA_DNS_IP, CA_DNS_PORT);
                dns.write("d" + link); //aggiungendo d all'inizio specifichi al dns che vuoi conoscere l'indirizzo ip partendo dal link
                ip = dns.wait_for_string();

                dns.close();
                dns_alive = true;

                server = new Connection(ip, SERVER_PORT);
            }

            //riceve il certificato del server
            String server_info = server.wait_for_string();
            byte[] server_certificate = server.wait_for_bytes();

            Cipher server_pubKey_cipher = get_cipher(server_certificate, server_info, ip);
            if (server_pubKey_cipher != null) { //se il certificato è valido, genera una chiave sint size = (bis.read() & 0xff) | ((bis.read() & 0xff) int size = (bis.read() & 0xff) | ((bis.read() & 0xff) << 8);<< 8);immetrica e la invia al server
                KeyGenerator kg = KeyGenerator.getInstance("AES");
                SecretKey key = kg.generateKey();

                byte[] aes_key_crypted = server_pubKey_cipher.doFinal(key.getEncoded());
                server.write(aes_key_crypted);

                Cipher encoder = Cipher.getInstance("AES");
                Cipher decoder = Cipher.getInstance("AES");
                encoder.init(Cipher.ENCRYPT_MODE, key);
                decoder.init(Cipher.DECRYPT_MODE, key);

                server.set_cipher(decoder, encoder); //da ora la connessione verrà cifrata con la chiave aes appena generata
                login_register(); //si registra o fa il login

                Godzilla_frame.set_title(registered_name + " - " + mail);
            } else { //se è stato trovato un errore nel verificare il certificato
                server.close();

                TempPanel.show_msg("il certificato ricevuto dal server non è valido, chiudo la connessione");
                return E_INVCE;
            }

            return 0;
        } catch (UnknownHostException e) {
            TempPanel.show_msg("l'indirizzo ip è stato inserito male, non ha senso");
            return E_INVIP;
        } catch (ConnectException e) {
            if (dns_alive) { //se la connessione è stata rifiutata dal server
                TempPanel.show_msg("connessione rifiutata, il server non è raggiungibile");
            }
            else { //se la connessione è stata rifiutata dal DNS
                TempPanel.show_msg("errore nella configurazione del DNS, server non raggiungibile");
            }
            return E_CONR;
        } catch (IOException e) {
            TempPanel.show_msg("impossibile connettersi al server");
            return E_GEN;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static void disconnect() {
        try {
            server.write("EOC"); //avvisa che sta chiudendo la connessione
            server.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void login_register() {
        TempPanel.show_msg("se vuoi fare il login premi \"ok\", altrimenti cancella", login_or_register, true);
    }

    private static StringVectorOperator login_or_register = new StringVectorOperator() {
        @Override
        public void success() { //se vuole fare il login
            Vector<String> requests = new Vector<>();
            requests.add("inserisci nome utente: ");
            requests.add("inserisci password: ");

            TempPanel.request_string(requests, login); //richiede nome utente e password
        }

        @Override
        public void fail() { //se vuole registrarsi o se vuole uscire
            TempPanel.show_msg("se vuoi registrarti premi \"ok\", altrimenti \"cancella\" e verrai disconnesso", register_exit, true);
        }
    };

    private static StringVectorOperator login = new StringVectorOperator() {
        @Override
        public void success() { //sono stati inseriti nome utente e password
            try {
                byte[] psw_hash = sha3_hash(input.elementAt(1)); //per evitare di condividere informazioni sulla lunghezza della password invia al server l'hash
                server.write("l" + input.elementAt(0)); //invia nome utente
                server.write(psw_hash); //invia la password

                if (server.wait_for_bytes()[0] == 0x1) { //se il login è andato a buon fine
                    System.out.println("login eseguito con successo");
                }
                else { //se nome utente o password sono sbagliati
                    TempPanel.show_msg("nome utente o password errati, premere \"ok\" per ritentare", login_or_register, true);
                }

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void fail() { //è stato premuto "cancella" viene chiesto nuovamente se vuole fare il login o se vuole registrarsi/scollegarsi
            TempPanel.show_msg("se vuoi fare il login premi \"ok\", altrimenti cancella", login_or_register, true);
        }
    };

    private static StringVectorOperator register_exit = new StringVectorOperator() {
        @Override
        public void success() { //vuole registrarsi
            Vector<String> requests = new Vector<>();
            requests.add("inserisci nome utente: ");
            requests.add("inserisci password: ");

            TempPanel.request_string(requests, register); //richiede nome utente e password
        }

        @Override
        public void fail() { //si disconnette dal server
            disconnect();
        }
    };

    private static StringVectorOperator register = new StringVectorOperator() {
        @Override
        public void success() {
            try {
                byte[] psw_hash = sha3_hash(input.elementAt(1)); //per evitare di condividere informazioni in rete sulla lunghezza della password invia al server l'hash
                server.write("r" + input.elementAt(0)); //invia nome utente
                server.write(psw_hash); //invia la password

                if (server.wait_for_bytes()[0] == 0x1) { //se la registrazione è andata a buon fine
                    System.out.println("login eseguito con successo");
                }
                else { //se nome utente o password sono sbagliati
                    TempPanel.show_msg("nome utente o password errati, premere \"ok\" per ritentare", login_or_register, true);
                }

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void fail() { //se viene premuto cancella, chiede nuovamente se vuole fare il login o registrarsi/scollegarsi
            TempPanel.show_msg("se vuoi fare il login premi \"ok\", altrimenti cancella", login_or_register, true);
        }
    };

    private static boolean is_an_ip(String txt) { //dovendo distinguere solamente fra ip e nomi di server controlla solamente se txt contiene dei "." (che non sono ammessi nei nomi dei server)
        return txt.contains(".");
    }

    /*
    * controlla che il certificato ricevuto sia valido,
    * se è valido crea un oggetto Cipher che cifri utilizzando la chiave pubblica del server, e inserisce mail, nome server nelle informazioni relative ad esso
    * se non è valido ritorna null
     */
    private static Cipher get_cipher(byte[] serverCertificate, String received_info, String ip) {
        try {
            byte[] dec_ce = Database.CAPublicKey.doFinal(serverCertificate); //decifra il certificato trovando l'hash delle informazioni del server
            byte[] server_info_hash = sha3_hash(received_info);

            Pattern patt = Pattern.compile("[;]");
            String[] server_info = patt.split(received_info);

            //se sono uguali, significa che le informazioni nel certificato e quelle in "received_info" sono uguali, e se l'indirizzo ip nel certificato coincide con quello voluto allora è possibile instaurare una connessione sicura
            if (Arrays.compare(dec_ce, server_info_hash) == 0 && server_info[2].equals(ip)) {
                registered_name = server_info[0]; //salva il nome con cui si è registrato questo server
                mail = server_info[4]; //salva la mail con cui è possibile contattare admin del server

                byte[] server_pub_key = Base64.getDecoder().decode(server_info[3].getBytes());
                KeyFactory key_f = KeyFactory.getInstance("RSA");
                Cipher server_cipher = Cipher.getInstance("RSA");
                PublicKey server_key = key_f.generatePublic(new X509EncodedKeySpec(server_pub_key));
                server_cipher.init(Cipher.ENCRYPT_MODE, server_key);

                return server_cipher;
            } else { //le informazioni non coincidono, il certificato o le informazioni del servono sono stati modificati
                return null;
            }
        } catch (Exception e) { //se riscontra un qualsiasi errore significa che il certificato ricevuto non è valido
            return null;
        }
    }

    private static byte[] sha3_hash(String txt) throws NoSuchAlgorithmException { //calcola l'hash di txt secondo l'algoritmo sha3
        return sha3_hash.digest(txt.getBytes());
    }
}
