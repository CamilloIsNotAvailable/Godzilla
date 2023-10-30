package file_database;

import gui.ButtonTopBar_panel;
import gui.ServerList_panel;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.regex.Pattern;

public class File_interface implements Database {
    private static final SecureFile FileCipherKey; //publico
    private static final SecureFile ServerList; //protetto
    private static final SecureFile TerminalLog; //publico
    private static final SecureFile MClientKey; //protetto
    private static final SecureFile CAPublicKey; //pubblico

    public static final int FILE_CIPHER_KEY = 0;
    public static final int SERVER_LIST = 1;
    public static final int TERMINAL_LOG = 2;
    public static final int MCLIENT_KEY = 3;
    public static final int CA_PUBLIC_KEY = 4;

    static { //inizializza tutti i file
        try {
            FileCipherKey = new SecureFile(project_path + "file_database/FileCipherKey.dat", false);
            ServerList = new SecureFile(project_path + "network/ServerList.dat", true);
            TerminalLog = new SecureFile(project_path + "gui/TerminalLog.dat", false);
            MClientKey = new SecureFile(project_path + "network/MClientKey.dat", true);
            CAPublicKey = new SecureFile(project_path + "network/CAPublicKey.dat", false);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static int init_index = 0;
    private static final int FILE_CIPHER_DATABASE = 0;
    private static final int FILE_CIPHER = 1;
    private static final int CA_PUBLIC_KEY_DATABASE = 2;
    private static final int SERVER_LIST_DATABASE = 3;
    private static final int MASTER_CLIENT_KEY_IV_DATABASE = 4;
    private static final int UPDATE_GUI = 5;
    public static void init_next() throws IllegalBlockSizeException, IOException, BadPaddingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        init_index += 1;

        switch (init_index - 1) {
            case FILE_CIPHER_DATABASE:
                init_file_cipher_key();
                break;

            case FILE_CIPHER:
                File_cipher.init();
                break;

            case CA_PUBLIC_KEY_DATABASE:
                init_ca_public_key();
                break;

            case SERVER_LIST_DATABASE:
                init_server_list();
                break;

            case MASTER_CLIENT_KEY_IV_DATABASE:
                init_master_client_key_iv();
                break;

            case UPDATE_GUI:
                ServerList_panel.update_gui();
                ButtonTopBar_panel.init_buttons();
                break;

            default:
                throw new RuntimeException("impossibile inizializzare il prossimo passaggio, index = " + init_index);
        }
    }

    private static void init_file_cipher_key() throws IllegalBlockSizeException, IOException, BadPaddingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        byte[] key_test = Base64.getDecoder().decode(FileCipherKey.read()); //copia i 32 byte per testare la validità della key inserita nel database
        for (int i = 0; i < key_test.length; i++) {
            FileCypherKey_test[i] = key_test[i];
        }

        init_next();
    }

    private static void init_ca_public_key() throws IllegalBlockSizeException, IOException, BadPaddingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        byte[] ca_key = CAPublicKey.read(); //copia la pubblic key della CA nel database
        for (int i = 0; i < ca_key.length; i++) {
            Database.CAPublicKey[i] = ca_key[i];
        }

        init_next();
    }

    private static void init_server_list() throws IllegalBlockSizeException, IOException, BadPaddingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        /*
         * il testo è formattato in questo modo:
         * <nome1>;<indirizzo1>
         * <nome2>;<indirizzo2>
         * ...
         * ...
         *
         * viene diviso ad ogni ';' o '\n' ricavando un array:
         * {<nome1>, <indirizzo1>, <nome2>, <indirizzo2>, ...}
         * che poi verrà inserito nel database
         */
        String server_list = new String(ServerList.read());

        Pattern p = Pattern.compile("[;\n]");
        String[] info = p.split(server_list);

        for (int i = 0; i < info.length; i += 2) {
            Database.serverList.put(info[i], info[i+1]);
        }

        init_next();
    }

    private static void init_master_client_key_iv() throws IllegalBlockSizeException, IOException, BadPaddingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        /*
        * il testo nel file è formattato in questo modo:
        * <indirizzo1>\00{<key1>\00;<iv1>\00}
        * <indirizzo2>\00{<key2>\00;<iv2>\00}
        *
        * viene diviso ad ogni "\00{", "\00;", "\00}" o "\00}\n" trovando quindi un array:
        * {<indirizzo1>, <key1>, <iv1>, <indirizzo2>, <key2>, <iv2>, ... }
        * che poi viene aggiunto al database
        *
        * key ed iv sono scritti nel file come Base64
         */
        String file_txt = new String(MClientKey.read());

        Pattern p = Pattern.compile("\00[\\{;\\}]\n*");
        String[] info = p.split(file_txt); //conterrà uno spazio vuoto nell'ultima posizione perché l'ultima sequenza di caratteri in file_txt sarà \00}\n

        for (int i = 0; i < info.length-1; i+= 3) {
            Database.MClientKey.put(
                    info[i], //indirizzo del server
                    new Pair<>(
                            Base64.getDecoder().decode(info[i + 1].getBytes()), //key
                            Base64.getDecoder().decode(info[i + 2].getBytes())  //iv
                    )
            );
        }

        init_next();
    }

    public static String read_file(int file_id) throws IOException, IllegalBlockSizeException, BadPaddingException {
        switch (file_id) {
            case FILE_CIPHER_KEY:
                return new String(FileCipherKey.read());

            case SERVER_LIST:
                return new String(ServerList.read());

            case TERMINAL_LOG:
                return new String(TerminalLog.read());

            case MCLIENT_KEY:
                return new String(MClientKey.read());

            case CA_PUBLIC_KEY:
                return new String(CAPublicKey.read());

            default:
                throw new RuntimeException("impossibile trovare il file con id: " + file_id);
        }
    }

    public static void append_to_file(int file_id, String txt) throws IOException, IllegalBlockSizeException, BadPaddingException {
        switch (file_id) {
            case FILE_CIPHER_KEY:
                FileCipherKey.append(txt);
                break;

            case SERVER_LIST:
                ServerList.append(txt);
                break;

            case TERMINAL_LOG:
                TerminalLog.append(txt);
                break;

            case MCLIENT_KEY:
                MClientKey.append(txt);

            case CA_PUBLIC_KEY:
                CAPublicKey.append(txt);
                break;

            default:
                throw new RuntimeException("impossibile trovare il file con id: " + file_id);
        }
    }

    public static void overwrite_file(int file_id, String txt) throws IOException, IllegalBlockSizeException, BadPaddingException {
        switch (file_id) {
            case FILE_CIPHER_KEY:
                FileCipherKey.replace(txt);
                break;

            case SERVER_LIST:
                ServerList.replace(txt);
                break;

            case TERMINAL_LOG:
                TerminalLog.replace(txt);
                break;

            case MCLIENT_KEY:
                MClientKey.replace(txt);
                break;

            case CA_PUBLIC_KEY:
                CAPublicKey.replace(txt);
                break;

            default:
                throw new RuntimeException("impossibile trovare il file con id: " + file_id);
        }
    }

    public static void close() throws IOException {
        FileCipherKey.close();
        ServerList.close();
        TerminalLog.close();
        MClientKey.close();
        CAPublicKey.close();
    }

}

class SecureFile {
    private final boolean IS_PROTECTED;
    private FileOutputStream fos;

    private final File F;

    public SecureFile(String pathname, boolean is_prot) throws FileNotFoundException {
        F = new File(pathname);

        this.IS_PROTECTED = is_prot;

        fos = new FileOutputStream(F, true);
    }

    protected byte[] read() throws IOException, IllegalBlockSizeException, BadPaddingException {
        FileInputStream fis = new FileInputStream(F);
        byte[] txt = fis.readAllBytes();
        fis.close();

        if (IS_PROTECTED) {
            txt = File_cipher.decrypt(txt);
        }

        return txt;
    }

    protected void append(String txt) throws IOException, IllegalBlockSizeException, BadPaddingException {
        if (IS_PROTECTED) {
            String file_txt = read() + txt;
            replace(file_txt);
        } else {
            fos.write(txt.getBytes());
        }
    }

    protected void replace(String txt) throws IOException, IllegalBlockSizeException, BadPaddingException {
        clear_file();
        byte[] txt_b = txt.getBytes();

        if (IS_PROTECTED) {
            txt_b = File_cipher.crypt(txt_b);
        }

        fos.write(txt_b);
    }

    protected void replace(byte[] txt) throws IOException, IllegalBlockSizeException, BadPaddingException {
        clear_file();

        if (IS_PROTECTED) {
            txt = File_cipher.crypt(txt);
        }

        fos.write(txt);
    }

    private void clear_file() throws IOException {
        new FileOutputStream(F, false).close();
    }

    protected void close() throws IOException {
        fos.close();
    }

}