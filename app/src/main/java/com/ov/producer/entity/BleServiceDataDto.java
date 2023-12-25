package com.ov.producer.entity;

import java.io.Serializable;
import java.util.Map;

public class BleServiceDataDto implements Serializable {

    private String serviceUUID;

    private String serviceType;

    private Map<String,CharacteristicDataDto> characteristicDataMap;

    public BleServiceDataDto(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Map<String, CharacteristicDataDto> getCharacteristicDataMap() {
        return characteristicDataMap;
    }

    public void setCharacteristicDataMap(Map<String, CharacteristicDataDto> characteristicDataMap) {
        this.characteristicDataMap = characteristicDataMap;
    }
}
