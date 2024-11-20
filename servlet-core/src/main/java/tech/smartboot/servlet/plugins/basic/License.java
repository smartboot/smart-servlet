/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.basic;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 读取License并解析其内容
 *
 * @author 三刀
 * @version V1.0 , 2020/3/20
 */
public class License {
    /**
     * 忽略
     */
    public static final RuntimeExpireStrategy EXPIRE_STRATEGY_IGNORE = entity -> System.err.println("invalid license");

    /**
     * 异常
     */
    public static final RuntimeExpireStrategy EXPIRE_STRATEGY_THROWS = entity -> {
        throw new LicenseException("invalid license");
    };

    private static final String KEY_ALGORITHM = "RSA";
    private final byte[] readBuffer = new byte[8];
    /**
     * 过期策略
     */
    private final RuntimeExpireStrategy expireStrategy;

    /**
     * 试用版过期策略
     */
    private final RuntimeExpireStrategy trialExpireStrategy;
    private final long period;

    private LicenseEntity entity;
    private long timestamp;

    public License() {
        this(EXPIRE_STRATEGY_IGNORE, TimeUnit.HOURS.toMillis(1));
    }

    public License(RuntimeExpireStrategy expireStrategy, long period) {
        this(expireStrategy, expireStrategy, period);
    }

    public License(RuntimeExpireStrategy expireStrategy, RuntimeExpireStrategy trialExpireStrategy, long period) {
        if (period < TimeUnit.SECONDS.toMillis(1)) {
            throw new IllegalArgumentException("period is too fast");
        }
        this.expireStrategy = expireStrategy;
        this.trialExpireStrategy = trialExpireStrategy;
        this.period = period;
    }

    /**
     * 使用公钥进行解密
     *
     * @param data
     * @param publicKey
     * @return
     */
    private byte[] decryptByPublicKey(byte[] data, byte[] publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(encodedKeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new LicenseException("decrypt exception", e);
        }
    }


    /**
     * 启动License过期监控
     */
    private void monitorExpireThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                long sleep = Math.min(entity.getExpireTime() - System.currentTimeMillis(), period);

                if (entity.getTrialDuration() > 0) {
                    sleep = Math.min(sleep, entity.getTrialDuration() * 60000L);
                }

                if (sleep <= 0) {
                    sleep = 10000;
                }

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (entity.getExpireTime() < System.currentTimeMillis()) {
                    expireStrategy.expire(entity);
                } else if (entity.getTrialDuration() > 0 && System.currentTimeMillis() - timestamp > entity.getTrialDuration() * 60000L) {
                    trialExpireStrategy.expire(entity);
                }
            }
        }, "licenseMonitor");
        thread.setDaemon(true);
        thread.start();
    }

    public LicenseEntity loadLicense(byte[] bytes) throws IOException {
        LicenseEntity entity = replace(bytes);
        monitorExpireThread();
        return entity;
    }

    public LicenseEntity replace(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        byte[] magicBytes = new byte[LicenseEntity.MAGIC_NUM.length];
        inputStream.read(magicBytes);
        checkBytes(magicBytes, LicenseEntity.MAGIC_NUM);

        // 申请时间
        long applyTime = readLong(inputStream);
        if (applyTime > System.currentTimeMillis()) {
            throw new LicenseException("invalid license");
        }
        //过期时间
        long expireTime = readLong(inputStream);

        if (expireTime < System.currentTimeMillis()) {
            throw new LicenseException("license expire");
        }

        //md5
        byte[] md5 = new byte[readInt(inputStream)];
        inputStream.read(md5);

        //公钥
        byte[] publicKey = new byte[readInt(inputStream)];
        inputStream.read(publicKey);
//        System.out.println(Base64.getEncoder().encodeToString(publicKey));

        //申请者
        byte[] applicant = new byte[readInt(inputStream)];
        inputStream.read(applicant);

        //联系方式
        byte[] contact = new byte[readInt(inputStream)];
        inputStream.read(contact);
//        System.out.println("contact:" + new String(contact));

        //试用时长
        int trialDuration = readInt(inputStream);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int size = 0;
        while ((size = inputStream.read()) > 0) {
            byte[] part = new byte[size];
            inputStream.read(part);
            byte[] decodeData = decryptByPublicKey(part, publicKey);
            byteArrayOutputStream.write(decodeData);
            if (readLong(inputStream) != expireTime % decodeData.length) {
                throw new LicenseException("invalid license");
            }

        }

        byte[] data = byteArrayOutputStream.toByteArray();
        if (!Md5.md5(data).equals(new String(md5))) {
            throw new LicenseException("invalid license");
        }
        LicenseEntity entity = new LicenseEntity(expireTime, publicKey);
        entity.setApplicant(new String(applicant));
        entity.setContact(new String(contact));
        entity.setTrialDuration(trialDuration);
        entity.setData(data);
        this.entity = entity;
        timestamp = System.currentTimeMillis();
        return entity;
    }

    public LicenseEntity getEntity() {
        return entity;
    }

    private void checkBytes(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            throw new LicenseException("invalid license");
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                throw new LicenseException("invalid license");
            }
        }
    }

    private long readLong(InputStream inputStream) throws IOException {
        inputStream.read(readBuffer, 0, 8);
        return (((long) readBuffer[0] << 56) + ((long) (readBuffer[1] & 255) << 48) + ((long) (readBuffer[2] & 255) << 40) + ((long) (readBuffer[3] & 255) << 32) + ((long) (readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16) + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
    }

    private int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public String getExpireHtml(Map<String, String> data) {
        String tmp = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>产品过期提示</title>
                    <style>        body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f9;
                        margin: 0;
                        padding: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                    }
                    .alert-container {
                        background-color: #fff;
                        border-radius: 8px;
                        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                        padding: 20px;
                        text-align: center;
                        max-width: 500px;
                        width: 90%;
                    }
                    .alert-header {
                        color: #e74c3c;
                        font-size: 24px;
                        margin-bottom: 10px;
                    }
                    .alert-message {
                        color: #333;
                        font-size: 16px;
                        line-height: 1.6;
                    }
                    .alert-button {
                        display: inline-block;
                        margin-top: 20px;
                        padding: 10px 20px;
                        background-color: #e74c3c;
                        color: #fff;
                        border: none;
                        border-radius: 4px;
                        cursor: pointer;
                        font-size: 16px;
                        transition: background-color 0.3s ease;
                    }
                    .alert-button:hover {
                        background-color: #c0392b;
                    }
                    </style>
                </head>
                <body>
                <div class="alert-container">
                    <h2 class="alert-header">产品已过期</h2>
                    <p class="alert-message">您的产品有效期已结束，请尽快续费以继续享受服务。</p>
                    <p class="alert-message">授权Mac地址：{{mac}}</p>
                    <a class="alert-button" style=" text-decoration: none;" href="https://smartboot.tech/smart-servlet/license.html" target="_blank">联系我们</a>
                </div>
                </body>
                </html>
                """;
        for (String key : data.keySet()) {
            tmp = tmp.replace("{{" + key + "}}", data.get(key));
        }
        return tmp;
    }
}
