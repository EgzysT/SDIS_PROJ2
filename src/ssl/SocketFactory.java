package ssl;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;

/**
 * SSL socket factory
 */
public class SocketFactory {

    /** SSL socket factory */
    private static SSLSocketFactory socketFactory;

    /** SSL server socket factory */
    private static SSLServerSocketFactory serverSocketFactory;

    static {
        socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
    }

    /**
     * Gets a SSL socket
     * @param host Host
     * @param port Port
     * @return SSL socket
     * @throws IOException
     */
    public static SSLSocket getSocket(String host, Integer port) throws IOException {
        SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);
        socket.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
        socket.setEnabledProtocols(new String[] { "TLSv1.2" });

        return socket;
    }

    /**
     * Gets a SSL server socket
     * @param port Port
     * @return SSL server socket
     * @throws IOException
     */
    public static SSLServerSocket getServerSocket(Integer port) throws IOException {
        SSLServerSocket socket = (javax.net.ssl.SSLServerSocket) serverSocketFactory.createServerSocket(port);
        socket.setNeedClientAuth(true);
        socket.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
        socket.setEnabledProtocols(new String[] { "TLSv1.2" });

        return socket;
    }
}
