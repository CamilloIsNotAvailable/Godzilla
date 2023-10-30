package file_database;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Database {
    Map<String, String> serverList = new LinkedHashMap<>(); //lega il nome del server al suo indirizzo
    Map<String, Pair<byte[], byte[]>> MClientKey = new LinkedHashMap<>(); //lega l'indirizzo del server con key ed iv scelti per la connessione (MClient)
    byte[] FileCypherKey_test = new byte[32]; //ultimi 32 byte dell hash SHA3-512 della chiave utilizzata per cifrare i file
    byte[] CAPublicKey = new byte[10]; //contiene la chiave pubblica della CA
    String project_path = Database.class.getProtectionDomain().getCodeSource().getLocation().getPath();
}
