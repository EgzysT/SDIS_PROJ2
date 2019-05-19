import peer.Peer;

public class Main {

    public static void main(String[] args) {

        String sslDir = "/home/miguelalexbt/IdeaProjects/SDIS_PROJ2/src/ssl/";

        System.setProperty("javax.net.ssl.keyStore", sslDir + "keystore.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        System.setProperty("javax.net.ssl.trustStore", sslDir + "truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

//        Peer.instance().init();
    }
}
