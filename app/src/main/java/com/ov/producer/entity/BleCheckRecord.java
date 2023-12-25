package com.ov.producer.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

import java.util.Date;

@Entity
public class BleCheckRecord {

    @Id(autoincrement = true)
    private long id;

    @Index(unique = true)
    private String mac;

    private String bleName;

    private String scanStr;

    private String productName;

    private String suffOpid;

    private String opid;

    private Date testTime;



    @Generated(hash = 1044611810)
    public BleCheckRecord(long id, String mac, String bleName, String scanStr,
            String productName, String suffOpid, String opid, Date testTime) {
        this.id = id;
        this.mac = mac;
        this.bleName = bleName;
        this.scanStr = scanStr;
        this.productName = productName;
        this.suffOpid = suffOpid;
        this.opid = opid;
        this.testTime = testTime;
    }

    @Generated(hash = 1042166380)
    public BleCheckRecord() {
    }



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public String getScanStr() {
        return scanStr;
    }

    public void setScanStr(String scanStr) {
        this.scanStr = scanStr;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSuffOpid() {
        return suffOpid;
    }

    public void setSuffOpid(String suffOpid) {
        this.suffOpid = suffOpid;
    }

    public String getOpid() {
        return opid;
    }

    public void setOpid(String opid) {
        this.opid = opid;
    }

    public Date getTestTime() {
        return testTime;
    }

    public void setTestTime(Date testTime) {
        this.testTime = testTime;
    }

}
