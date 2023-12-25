package com.ov.producer.entity;

public class OvAttrDto {

    private String name;

    private String desc;

    private Object value;

    private Integer valType;

    private String serviceUUID;

    private String characteristicUUID;

    public Integer getValType() {
        return valType;
    }

    public void setValType(Integer valType) {
        this.valType = valType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public String getCharacteristicUUID() {
        return characteristicUUID;
    }

    public void setCharacteristicUUID(String characteristicUUID) {
        this.characteristicUUID = characteristicUUID;
    }
}
