package ch.patklaey.webdavsync.webdav;

import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.patklaey.webdavsync.Settings;
import de.aflx.sardine.Sardine;
import de.aflx.sardine.SardineFactory;
import de.aflx.sardine.impl.SardineImpl;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by uni on 5/25/15.
 */
public class WebDavConnectionFactory {

    public static Sardine getWebDavConnection() {
        return SardineFactory.begin();
    }

    public static Sardine fromSettings(Settings settings) {
        Sardine sardine;

        if (settings.checkCert()) {
            sardine = SardineFactory.begin();
        } else {
            sardine = new SardineImpl() {

                @Override
                protected SSLSocketFactory createDefaultSecureSocketFactory() {

                    KeyStore keyStore = null;
                    try {
                        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        keyStore.load(null, null);
                    } catch (KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    SSLSocketFactory factory = null;
                    try {
                        factory = new MySSLSocketFactory(keyStore);
                    } catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return factory;
                }

            };
        }

        if (settings.authRequired()) {
            sardine.setCredentials(settings.getUsername(), settings.getPassword());
        }

        return sardine;
    }

}
