package com.ssl;

import javax.net.ssl.*;
import java.io.*;

public class SSLServer {

    private SSLServerSocket s;
    private SSLServerSocketFactory ssf;

    public SSLServer(int port) {

        ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {
            s = (SSLServerSocket) ssf.createServerSocket(port);

        }
        catch(IOException e) {
            System.out.println("Server - Failed to create SSLServerSocket");
            e.getMessage();
            return;
        }

        // Require client authentication
        s.setNeedClientAuth(true);
        s.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
        s.setEnabledProtocols(new String[] { "TLSv1.2" });
    }

    public void listen() {

        BufferedReader input = null;

        while (true) {
            try (SSLSocket sslSocket = (SSLSocket) s.accept()) {

                printMessage("Incoming connection");

                //Prints what has received
                input = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    printMessage(inputLine);

                    //Don't know why I have to do this break but otherwise it won't work
                    break;
                }

                printMessage("Sending Message");

                PrintWriter pw = new PrintWriter(sslSocket.getOutputStream(), true);
                pw.println("Hello Client!");

                printMessage("Message Sent");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void printMessage(String message) {
        System.out.println("SSLServer: " + message);
    }



    public static void main(String[] args) {

        if (args.length < 1 ) {
            System.out.println("Incorrect number of arguments");
            return;
        }

        //Sets server certificates

        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir") + File.separator + "src" + File.separator + "com" + File.separator + "ssl" + File.separator + "server.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + File.separator + "src" + File.separator + "com" + File.separator + "ssl" + File.separator + "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        SSLServer sslServer = new SSLServer(Integer.parseInt(args[0]));
        sslServer.listen();

        return;
    }
}
