package ip_database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Converter {
    private static Map<String, String> ip_link = new LinkedHashMap<>(); //link -> ip
    public static void load_from_file() throws IOException {
        File f = new File("/home/camillo/java-projects/Godzilla_project/1.0/Godzilla_CA_DNS/src/ip_database/ip_link.dtb");
        FileInputStream fis = new FileInputStream(f);

        String txt = new String(fis.readAllBytes());
        if (!txt.equals("")) {
            Pattern p = Pattern.compile("[;\n]");
            String[] sep = p.split(txt);

            for (int i = 0; i < sep.length; i += 2) {
                ip_link.put(sep[i], sep[i+1]);
            }
        }
    }

    public static String get_ip_of(String link) {
        String ip;
        if ((ip = ip_link.get(link)) == null) { //non è registrato nessun server con quell'indirizzo ip
            return "error, the ip is not registered";
        }
        else {
            return ip;
        }
    }

    public static String get_link_of(String ip) {
        ArrayList<String> val_list = new ArrayList(ip_link.values());
        ArrayList<String> key_list = new ArrayList(ip_link.keySet());
        int i = val_list.indexOf(ip);
        if (i == -1) { //non esiste nessun ip legato a questo link
            return "error, the link do not exist";
        }
        else {
            return key_list.get(i);
        }
    }
}
