package utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPSocket {

    protected ServerSocket server;
//    private byte[] buffer;

    public TCPSocket(Integer port) {
        try {
            server = new ServerSocket(port);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

//        buffer = new byte[64512];
    }

//    public void send(InetSocketAddress addr, String data) {
//
//        try {
//            Socket client = new Socket(addr.getHostName(), addr.getPort());
//
//            ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
//
//            os.writeObject(data);
//            os.flush();
//
//            client.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
//    }

//    public byte[] listen() {
//
//        try {
//            Socket client = server.accept();
//
//            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
//            String idk = (String) is.readObject();
//
//            client.close();
//
//            System.out.println(idk.trim());
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(-1);
//        }
//
////        return Arrays.copyOfRange(buffer, 0, bytes_read);
//        return null;
//    }
}

//            server = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
//
//            server.setNeedClientAuth(true);
//
//            server.setEnabledCipherSuites(
//                    new String[] { "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256" });
//            server.setEnabledProtocols(
//                    new String[] { "TLSv1.2" });

//            Socket client = SSLSocketFactory.getDefault().createSocket("localhost", port);
//            ((SSLSocket) client).setEnabledCipherSuites(
//                    new String[] { "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256" });
//            ((SSLSocket) client).setEnabledProtocols(
//                    new String[] { "TLSv1.2" });
//            SSLParameters parameters = new SSLParameters();
//            parameters.setEndpointIdentificationAlgorithm("HTTPS");
//            ((SSLSocket) client).setSSLParameters(parameters);

//            KeyStore keyStore = KeyStore.getInstance("JKS");
//            keyStore.load(new FileInputStream("/home/miguelalexbt/IdeaProjects/SDIS2/serverkeystore.jks"), "password".toCharArray());
//
//            // Create key manager
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
//            keyManagerFactory.init(keyStore, "password".toCharArray());
//            KeyManager[] km = keyManagerFactory.getKeyManagers();
//
//            // Create trust manager
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
//            trustManagerFactory.init(keyStore);
//            TrustManager[] tm = trustManagerFactory.getTrustManagers();
//
//            // Initialize SSLContext
//            sslContext = SSLContext.getInstance("TLSv1.2");
//            sslContext.init(km,  tm, null);