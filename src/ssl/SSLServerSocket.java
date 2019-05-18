package ssl;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;

public class SSLServerSocket {

    private javax.net.ssl.SSLServerSocket socket;

    public SSLServerSocket(Integer port) throws IOException {

        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        socket = (javax.net.ssl.SSLServerSocket) ssf.createServerSocket(port);
        socket.setNeedClientAuth(true);
        socket.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
        socket.setEnabledProtocols(new String[] { "TLSv1.2" });
    }

    public SSLSocket accept() throws IOException {
        return new SSLSocket(socket.accept());
    }
}
