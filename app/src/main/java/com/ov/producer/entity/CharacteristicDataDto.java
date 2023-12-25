package com.ov.producer.entity;


import java.util.Map;

public class CharacteristicDataDto {

    private String characteristicUUID;

    private String parentServiceUUID;

    private int properties;
    private Map<String,DescriptorDataDto>descriptorDataMap;

    private byte[]data;

    private String name;

    private String desc;

    private Integer valType;

    private Object realVal;

    private boolean enableWrite=false;

    private boolean enableRead=false;

    private boolean enableIndicate=false;

    private boolean enableNotify=false;

    private boolean enableWriteNoResp=false;


    public CharacteristicDataDto(String characteristicUUID, String parentServiceUUID) {
        this.characteristicUUID = characteristicUUID;
        this.parentServiceUUID = parentServiceUUID;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValType() {
        return valType;
    }

    public void setValType(Integer valType) {
        this.valType = valType;
    }

    public Object getRealVal() {
        return realVal;
    }

    public void setRealVal(Object realVal) {
        this.realVal = realVal;
    }

    public boolean isEnableWriteNoResp() {
        return enableWriteNoResp;
    }

    public void setEnableWriteNoResp(boolean enableWriteNoResp) {
        this.enableWriteNoResp = enableWriteNoResp;
    }

    public int getProperties() {
        return properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    public boolean isEnableNotify() {
        return enableNotify;
    }

    public void setEnableNotify(boolean enableNotify) {
        this.enableNotify = enableNotify;
    }

    public boolean isEnableWrite() {
        return enableWrite;
    }

    public void setEnableWrite(boolean enableWrite) {
        this.enableWrite = enableWrite;
    }

    public boolean isEnableRead() {
        return enableRead;
    }

    public void setEnableRead(boolean enableRead) {
        this.enableRead = enableRead;
    }

    public boolean isEnableIndicate() {
        return enableIndicate;
    }

    public void setEnableIndicate(boolean enableIndicate) {
        this.enableIndicate = enableIndicate;
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

    public String getCharacteristicUUID() {
        return characteristicUUID;
    }

    public void setCharacteristicUUID(String characteristicUUID) {
        this.characteristicUUID = characteristicUUID;
    }

    public Map<String, DescriptorDataDto> getDescriptorDataMap() {
        return descriptorDataMap;
    }

    public void setDescriptorDataMap(Map<String, DescriptorDataDto> descriptorDataMap) {
        this.descriptorDataMap = descriptorDataMap;
    }
}
