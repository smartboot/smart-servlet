/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.license;

import java.io.Serializable;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/26
 */
public final class LicenseEntity implements Serializable {

    /**
     * 魔数
     */
    public static final byte[] MAGIC_NUM = new byte[]{115, 109, 97, 114, 116, 45, 108, 105, 99, 101, 110, 115, 101};
    /**
     * 公钥
     */
    private final byte[] publicKeys;
    /**
     * 申请时间
     */
    private final long applyTime = System.currentTimeMillis();

    /**
     * 过期时间
     */
    private final long expireTime;

    /**
     * 申请方
     */
    private String applicant;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 试用时长
     */
    private int trialDuration;

    /**
     * 原文
     */
    private transient byte[] data;

    public LicenseEntity(long expireTime, byte[] publicKeys) {
        this.expireTime = expireTime;
        this.publicKeys = publicKeys;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public byte[] getPublicKeys() {
        return publicKeys;
    }

    public long getApplyTime() {
        return applyTime;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public int getTrialDuration() {
        return trialDuration;
    }

    public void setTrialDuration(int trialDuration) {
        this.trialDuration = trialDuration;
    }
}
