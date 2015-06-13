package ch.patklaey.webdavsync;

/**
 * Created by uni on 5/25/15.
 */
public class Settings {

    private String webdavUrl = "";
    private boolean checkCert = true;
    private boolean authRequired = false;
    private boolean wifiOnly = true;
    private String username = "";
    private String password = "";
    private String localDirectory = "";
    private String remoteDirectory = "";

    public Settings() {
    }

    public String getWebdavUrl() {
        return webdavUrl;
    }

    public void setWebdavUrl(String webdavUrl) {
        this.webdavUrl = webdavUrl;
    }

    public boolean checkCert() {
        return checkCert;
    }

    public void setCheckCert(boolean checkCert) {
        this.checkCert = checkCert;
    }

    public boolean authRequired() {
        return authRequired;
    }

    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }

    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = remoteDirectory;
    }

    public boolean wifiOnly() {
        return wifiOnly;
    }

    public void setWifiOnly(boolean wifiOnly) {
        this.wifiOnly = wifiOnly;
    }

    @Override
    public String toString() {
        return "Settings{" +
                "webdavUrl='" + webdavUrl + '\'' +
                ", checkCert=" + checkCert +
                ", authRequired=" + authRequired +
                ", wifiOnly=" + wifiOnly +
                ", username='" + username + '\'' +
                ", password=\'*\'" +
                ", localDirectory='" + localDirectory + '\'' +
                ", remoteDirectory='" + remoteDirectory + '\'' +
                '}';
    }
}
