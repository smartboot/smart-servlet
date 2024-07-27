package tech.smartboot.servlet.plugins.tls;

import org.smartboot.http.common.utils.Param;

public class SSLConfig {
    /**
     * SSL是否可用
     */
    @Param(name = "ssl.enable")
    private boolean enable = false;

    /**
     * 证书类型: pem,jks
     */
    @Param(name = "ssl.certType")
    private String type = "pem";

    @Param(name = "ssl.port")
    private int port = 443;

    @Param(name = "ssl.readBufferSize")
    private int readBufferSize = 1024;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}