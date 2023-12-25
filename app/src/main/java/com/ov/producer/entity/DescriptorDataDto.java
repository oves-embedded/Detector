package com.ov.producer.entity;

import java.io.Serializable;

public class DescriptorDataDto implements Serializable {

    private String descriptorUUID;

    private String parentCharacteristicUUID;

    private String parentServiceUUID;

    private byte[] data;

    public DescriptorDataDto(String descriptorUUID, String parentCharacteristicUUID, String parentServiceUUID) {
        this.descriptorUUID = descriptorUUID;
        this.parentCharacteristicUUID = parentCharacteristicUUID;
        this.parentServiceUUID = parentServiceUUID;
    }

    public String getDescriptorUUID() {
        return descriptorUUID;
    }

    public void setDescriptorUUID(String descriptorUUID) {
        this.descriptorUUID = descriptorUUID;
    }

    public String getParentCharacteristicUUID() {
        return parentCharacteristicUUID;
    }

    public void setParentCharacteristicUUID(String parentCharacteristicUUID) {
        this.parentCharacteristicUUID = parentCharacteristicUUID;
    }

    public String getParentServiceUUID() {
        return parentServiceUUID;
    }

    public void setParentServiceUUID(String parentServiceUUID) {
        this.parentServiceUUID = parentServiceUUID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
