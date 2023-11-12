package network;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection {
    private Socket sck;
    private BufferedOutputStream output;
    private BufferedInputStream input;

    private static final int TIMEOUT = 10; //dopo 10s che attende una risposta dal server senza ricevere risposta chiude la connessione

    private Cipher encoder = null;
    private Cipher decoder = null;
    private boolean secure = false;
    public Connection(Socket sck) throws IOException {
        this.sck = sck;
        this.output = new BufferedOutputStream(sck.getOutputStream());
        this.input = new BufferedInputStream(sck.getInputStream());
    }

    public void write(String msg) throws IOException, IllegalBlockSizeException, BadPaddingException {
        write(msg.getBytes());
    }

    public void write(byte[] msg) throws IOException, IllegalBlockSizeException, BadPaddingException {
        if (secure) { //se deve cifrare il messaggio
            msg = encoder.doFinal(msg);
        }

        output.write(new byte[] {(byte) (msg.length & 0xff), (byte) ((msg.length >> 8) & 0xff)});
         output.write(msg);

        output.flush();
    }

    public String wait_for_string(boolean timed) throws IOException, IllegalBlockSizeException, BadPaddingException {
        return new String(wait_for_bytes(timed));
    }

    public byte[] wait_for_bytes(boolean timed) throws IOException, IllegalBlockSizeException, BadPaddingException {
        long timeout_system = System.currentTimeMillis() + TIMEOUT*1000;

        byte[] msg = null;
        int msg_len = -1; //deve ancora ricevere le dimensioni del messaggio dal server
        while ((!timed || System.currentTimeMillis() < timeout_system) && msg == null) {
            if (msg_len == -1 && input.available() >= 2) { //se non ha ancora ricevuto le dimensioni del messaggio, e sono disponibili 2 byte in input
                byte[] msg_size_byte = input.readNBytes(2);
                msg_len = (msg_size_byte[0] & 0xff) | (msg_size_byte[1] << 8);
            }
            else if (msg_len != -1 && input.available() >= msg_len) { //se è già stata ricevuta la lunghezza del messaggio e ci sono disponibili msg_len bytes in input
                msg = input.readNBytes(msg_len);
            }
            else {}
        }

        if (secure && msg != null) {
            msg = decoder.doFinal(msg);
        }

        return msg;
    }

    public boolean set_cipher(Cipher decoder, Cipher encoder) {
        if (!secure) {
            this.decoder = decoder;
            this.encoder = encoder;
            secure = true;

            return true;
        }
        else {
            return false;
        }
    }
}
