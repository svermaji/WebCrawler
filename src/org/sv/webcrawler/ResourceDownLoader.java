package org.sv.webcrawler;

import org.sv.webcrawler.util.MyLogger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.util.Scanner;

/**
 * This class will help in downloading bunch of urls that
 * may point to resource like images, pdfs etc.
 */
public class ResourceDownLoader {

    private MyLogger logger;
    private TrustManager[] trustAllCerts;

    public ResourceDownLoader(MyLogger logger) {
        this.logger = logger;
        initComponents();
    }

    // for certain https resources
    private void createTrustManager() {
        trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
    }

    /**
     * This method initializes settings
     */
    private void initComponents() {
        createTrustManager();
        trustAllHttps();
    }

    private void trustAllHttps() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            logger.log(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Downloads http url and send data as string
     * @param httpUrl
     * @return
     */
    public String downloadAsString(String httpUrl) {

        logger.log("Trying url [" + httpUrl + "]");

        long startTime = System.currentTimeMillis();
        try {
            URL url = new URL(httpUrl);
            // expression \\A matches the beginning of input and to collect entire stream
            String contents = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();

            long diffTime = (System.currentTimeMillis() - startTime);
            long diffTimeInSec = diffTime / 1000;
            long size = contents.length();

            logger.log("Download size [" + size + "] complete in ["
                + diffTimeInSec + "] seconds with speed [" + (size / diffTime) + "] KB/s");

            return contents;
        } catch (Exception e) {
            logger.log(e.getMessage());
            e.printStackTrace();
        }
        logger.log("File downloaded");
        return "";
    }
}

