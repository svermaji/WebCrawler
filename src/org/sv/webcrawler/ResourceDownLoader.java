package org.sv.webcrawler;

import org.sv.webcrawler.util.MyLogger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * This class will help in downloading bunch of urls that
 * may point to resource like images, pdfs etc.
 */
public class ResourceDownLoader {

    private MyLogger logger;
    private final String DEFAULT = "default (current folder)";
    private TrustManager[] trustAllCerts;

    public ResourceDownLoader (MyLogger logger) {
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
     * This method initializes the form.
     */
    private void initComponents() {
        createTrustManager();
        trustAllHttps ();
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

    public String downloadAsString(String httpUrl) {
        logger.log("Trying url [" + httpUrl + "]");
        long startTime = System.currentTimeMillis();
        try {
            int KB = 1024;
            URL u = new URL(httpUrl);
            URLConnection uc = u.openConnection();
            int fileSize = uc.getContentLength();
            logger.log("Url resource size is [" + fileSize + "] Bytes i.e. [" + (fileSize / KB) + "] KB");
            /*InputStream in = uc.getInputStream();
            int chunkSize = 1024;
            byte b[] = new byte[chunkSize];
            int i = in.read(b);
            FileOutputStream fos = new FileOutputStream(getDestPath(httpUrl));
            int chunks = 1;
            while (i != -1) {
                fos.write(b, 0, i);
                i = in.read(b);
                chunks++;
            }*/
            StringBuilder sb = new StringBuilder();
            ReadableByteChannel rbc = Channels.newChannel(u.openStream());
            ByteBuffer buffer = ByteBuffer.allocateDirect(fileSize);
            Charset charset = Charset.forName("ISO-8859-1");
            int numRead = rbc.read(buffer);
            buffer.rewind();
            ByteBuffer backedBuffer = readBuffer (buffer, numRead);

            long diffTime = (System.currentTimeMillis() - startTime);
            long diffTimeInSec = diffTime / 1000;

            logger.log("Download complete in ["
                + diffTimeInSec + "] seconds with speed [" + (fileSize / diffTime) + "] KB/s");

            return charset.decode(backedBuffer).toString();
        } catch (Exception e) {
            logger.log(e.getMessage());
            e.printStackTrace();
        }
        logger.log("File downloaded");
        return "";
    }

    private ByteBuffer readBuffer(ByteBuffer buffer, int numRead) {
        byte[] bytes = new byte[numRead];
        for (int i=0; i < numRead; i++) {
            bytes[i] = buffer.get();
        }
        return ByteBuffer.wrap(bytes, 0, numRead);
    }

}

