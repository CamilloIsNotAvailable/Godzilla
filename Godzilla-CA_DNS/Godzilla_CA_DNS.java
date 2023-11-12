import ip_database.Converter;
import servers.CertificateAuthority;
import servers.DomainNameSystem;

import java.io.IOException;
import java.net.ServerSocket;

public class Godzilla_CA_DNS {
    private static final int CA_PORT = 6969;
    private static final int DNS_PORT = 9696;

    private static Thread dns_t;
    private static Thread ca_t;

    public static void main(String[] args) throws IOException {
        Converter.load_from_file();

        ServerSocket ca_ss = new ServerSocket(CA_PORT);
        ServerSocket dns_ss = new ServerSocket(DNS_PORT);

        dns_t = new Thread(() -> {
            DomainNameSystem.init(dns_ss);
        });

        ca_t = new Thread(() -> {
            CertificateAuthority.init(ca_ss);
        });

        dns_t.start();
        ca_t.start();
    }
}
