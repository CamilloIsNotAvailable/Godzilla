import file_database.Database;
import file_database.File_interface;
import gui.Godzilla_frame;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Godzilla {
    public static void main(String args[]) throws IllegalBlockSizeException, IOException, BadPaddingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Godzilla_frame.init().setVisible(true);
        File_interface.init_next();

        Runtime.getRuntime().addShutdownHook(shut_down);
    }

    private static Thread shut_down = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                save_MClient_key();
                save_server_list();

                File_interface.close();

            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
        }

        private void save_server_list() throws IllegalBlockSizeException, IOException, BadPaddingException {
            String new_file_txt = "";
            for (int i = 0; i < Database.serverList.size(); i++) {
                String key = (String) Database.serverList.keySet().toArray()[i];
                new_file_txt += key + ";" + Database.serverList.get(key) + "\n";
            }

            if (!new_file_txt.equals("")) {
                File_interface.overwrite_file(File_interface.SERVER_LIST, new_file_txt);
            }
        }

        private void save_MClient_key() throws IllegalBlockSizeException, IOException, BadPaddingException {
            String new_file_txt = "";
            for (int i = 0; i < Database.MClientKey.size(); i++) {
                String key = (String) Database.serverList.keySet().toArray()[i];
                new_file_txt += key + "\00{" + Database.MClientKey.get(key).el1 + "\00;" + Database.MClientKey.get(key).el2 + "\00}\n";
            }

            if (!new_file_txt.equals("")) {
                File_interface.overwrite_file(File_interface.MCLIENT_KEY, new_file_txt);
            }
        }
    });

}