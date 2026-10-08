package com.msb.customer.defaultvalues;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

/**
 * TODO: Document me!
 *
 * @author dungvv7
 *
 */
public class DefaultSRV202Val extends RecordLifecycle{

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        CustomerRecord currentCus = new CustomerRecord(currentRecord);
        int sector = Integer.parseInt(currentCus.getSector().getValue());
        if (1000 <= sector && sector < 2000) {
            currentCus.setBirthIncorpDate("");
        } else if (sector >= 2000) {
            if (!currentCus.getDateOfBirth().getValue().isEmpty()) {
                currentCus.setBirthIncorpDate(currentCus.getDateOfBirth());
                currentCus.setDateOfBirth("");
            }
            currentCus.setGender("");
            currentCus.setTitle("");
            currentCus.setCustBirthCity("");
            currentCus.setMaritalStatus("");
            if (!currentCus.getEmploymentStatus().isEmpty()) {
                currentCus.getEmploymentStatus().get(0).getJobTitle().set("");
            }
        }
        currentRecord.set(currentCus.toStructure());
    }
}
