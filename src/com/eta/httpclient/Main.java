package com.eta.httpclient;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static void EnableSelfSingedCertificate() throws NoSuchAlgorithmException, KeyManagementException
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager()
            {
                public java.security.cert.X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType){}

                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        HostnameVerifier allHostsValid = (hostname, session) -> true;

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static void main(String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException, ParseException
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date startDateTime = dateFormat.parse("2015-01-01");
        Date endDateTime = dateFormat.parse("2015-01-31");

        // https://eta.asd116.ru
        String baseAddress = "https://192.168.1.2";

        String uri;
        uri = String.format(
                "%s//api/package/archive?" +
                "ar.withDictionaries=true&ar.startDateTime=%s&" +
                "ar.endDateTime=%s&responseToFile=true",
                baseAddress,
                dateFormat.format(startDateTime),
                dateFormat.format(endDateTime));

        URL url = new URL(uri);

        // для возможности использования самоподписанного сертификата сервера
        // после установки на сервер заверенного сертификата вызов данного
        // метода будет не нужен
        EnableSelfSingedCertificate();

        HttpsURLConnection connection = null;

        try
        {

            connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setRequestProperty("login", "Archive.Downloader");
            connection.setRequestProperty("password", "xxxx");

            try(InputStream xml = connection.getInputStream())
            {
               if(connection.getResponseCode() == 200)
               {
                   Path path = Paths.get("d:\\ArchivePackageJv.xml");

                   if (Files.exists(path))
                   {
                       Files.delete(path);
                   }
                   Files.copy(xml, path);
               }
            }
        }
        finally
        {
            if(connection != null)
            {
                connection.disconnect();
            }
        }
    }
}
