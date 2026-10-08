package com.msb.customer.validations;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

/**
 * TODO: Document me!
 *
 * @author dungvv7
 *
 */
public class ValidIndivCus extends RecordLifecycle {

    private static final String MSG_INPT = "INPUT MISSING";

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        CustomerRecord currentCus = new CustomerRecord(currentRecord);
        int sector = Integer.parseInt(currentCus.getSector().getValue());
        if (1000 <= sector && sector < 2000) {
            if (currentCus.getGender().getValue().isEmpty()) {
                currentCus.getGender().setError(MSG_INPT);
            }
        }
        return currentCus.getValidationResponse();
    }
}
