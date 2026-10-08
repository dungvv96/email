package com.msb.customer.validations;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

/**
 * TODO: Document me!
 *
 * @author dungvv7
 *
 */
public class ValidCustomerType extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        CustomerRecord customerRec = new CustomerRecord(currentRecord);
        try {
            String customerType = customerRec.getCustomerType().getValue();
            if (!"ACTIVE".equalsIgnoreCase(customerType)) {
                customerRec.getCustomerType().setError("EB-MSB.CUS.TYPE.ERR");
            }
        } catch (Exception e) {
            throw new T24CoreException("Exception in MsbVCifCustomerTypeVal " + e.getLocalizedMessage());
        }
        return customerRec.getValidationResponse();
    }
}
