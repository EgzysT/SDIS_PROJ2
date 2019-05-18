package ssl;

import core.Message;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

public class SSLSocket {

    private javax.net.ssl.SSLSocket socket;

    public SSLSocket(Socket socket) {
        this.socket = (javax.net.ssl.SSLSocket) socket;
    }

    public SSLSocket(String host, Integer port) throws IOException {
        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();

        socket = (javax.net.ssl.SSLSocket) ssf.createSocket(host, port);
        socket.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
        socket.setEnabledProtocols(new String[] { "TLSv1.2" });
    }

    public void send(Message message) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        os.writeObject(message);
        os.flush();
    }

    public Message receive() throws IOException {
        Message message = null;

        try {
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            message = (Message) is.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return message;
    }

    public void close() throws IOException {
        socket.close();
    }
}
