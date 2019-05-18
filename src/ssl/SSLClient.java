//package ssl;
//
//import javax.net.ssl.*;
//import java.io.*;
//
//public class SSLClient {
//
//    private SSLSocket s;
//    private SSLSocketFactory ssf;
//
//    public SSLClient(String host, int port) {
//
//        ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
//
//        try {
//            s = (SSLSocket) ssf.createSocket(host, port);
//        }
//        catch(IOException e) {
//            System.out.println("Server - Failed to create SSLSocket");
//            e.getMessage();
//            return;
//        }
//
//        s.setEnabledCipherSuites(new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" });
//        s.setEnabledProtocols(new String[] { "TLSv1.2" });
//
////        SSLParameters sslParams = new SSLParameters();
////        sslParams.setEndpointIdentificationAlgorithm("HTTPS");
////        s.setSSLParameters(sslParams);
//    }
//
//    public void sendMessage() {
//
//        try {
//            s.startHandshake();
//
//            printMessage("Sending Message");
//
//            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
//            out.println("Hello Server!");
//            out.flush();
//
//            printMessage("Message Sent");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        BufferedReader input = null;
//        try {
//            printMessage("Receiving Message");
//
//            //Prints what has received
//            input = new BufferedReader(new InputStreamReader(s.getInputStream()));
//
//            String inputLine;
//            while ((inputLine = input.readLine()) != null)
//                printMessage(inputLine);
//
//            printMessage("Message Received");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//    }
//
//    public void printMessage(String message) {
//        System.out.println("SSLClient: " + message);
//    }
//
//
//
//    public static void main(String[] args) {
//
//        if (args.length < 2 ) {
//            System.out.println("Incorrect number of arguments");
//            return;
//        }
//
//        //Sets client certificates
//        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir") + File.separator + "src" + File.separator + "ssl" + File.separator + "common.keys");
//        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
//
////        System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir") + File.separator + "src" + File.separator + "ssl" + File.separator + "client.keys");
////        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
//
//        System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + File.separator + "src" + File.separator + "ssl" + File.separator + "truststore");
//        System.setProperty("javax.net.ssl.trustStorePassword", "123456");
//
//        SSLClient sslClient = new SSLClient(args[0], Integer.parseInt(args[1]));
//        sslClient.sendMessage();
//
//        return;
//    }
//}
