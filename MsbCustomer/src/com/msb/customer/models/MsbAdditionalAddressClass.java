package com.msb.customer.models;

/**
 * TODO: Document me!
 *
 * @author hieunt35
 *
 */
public class MsbAdditionalAddressClass {

    String addressType;
    String addressStreet1;
    String addressStreet2;

    /**
     * @param addressType
     * @param addressStreet1
     * @param addressStreet2
     */
    public MsbAdditionalAddressClass(String addressType, String addressStreet1, String addressStreet2) {
        super();
        this.addressType = addressType;
        this.addressStreet1 = addressStreet1;
        this.addressStreet2 = addressStreet2;
    }

    /**
     * @return the addressType
     */
    public String getAddressType() {
        return addressType;
    }

    /**
     * @param addressType
     *            the addressType to set
     */
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    /**
     * @return the addressStreet1
     */
    public String getAddressStreet1() {
        return addressStreet1;
    }

    /**
     * @param addressStreet1
     *            the addressStreet1 to set
     */
    public void setAddressStreet1(String addressStreet1) {
        this.addressStreet1 = addressStreet1;
    }

    /**
     * @return the addressStreet2
     */
    public String getAddressStreet2() {
        return addressStreet2;
    }

    /**
     * @param addressStreet2
     *            the addressStreet2 to set
     */
    public void setAddressStreet2(String addressStreet2) {
        this.addressStreet2 = addressStreet2;
    }

}
