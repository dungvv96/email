package com.msb.customer.model;

public class RelCustResult {
    private String customer;
    private String isrelation;
    private String ofcustomer;
    private String repId;

    /**
     * @return the customer
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * @param customer
     *            the customer to set
     */
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    /**
     * @return the isrelation
     */
    public String getIsrelation() {
        return isrelation;
    }

    /**
     * @param isrelation
     *            the isrelation to set
     */
    public void setIsrelation(String isrelation) {
        this.isrelation = isrelation;
    }

    /**
     * @return the ofcustomer
     */
    public String getOfcustomer() {
        return ofcustomer;
    }

    /**
     * @param ofcustomer
     *            the ofcustomer to set
     */
    public void setOfcustomer(String ofcustomer) {
        this.ofcustomer = ofcustomer;
    }
    
    /**
     * @return the repId
     */
    public String getRepId() {
        return repId;
    }

    /**
     * @param repId the repId to set
     */
    public void setRepId(String repId) {
        this.repId = repId;
    }

    @Override
    public String toString() {
        return customer + "#@!" + isrelation + "#@!" + ofcustomer + "#@!" + repId;
    }

}
