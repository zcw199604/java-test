package com.example.tobacco.model;

import javax.validation.constraints.NotBlank;

public class SupplierRequest {
    @NotBlank private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
