package com.msb.customer.defaultvalues;

import com.msbf.common.utilities.MsbUtils;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;

/**
 * TODO: Document me!
 *
 * @author dungvv7
 *
 */
public class CheckIdCustomer extends RecordLifecycle {

    DataAccess da = new DataAccess(this);
    
    TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);

    @Override
    public String checkId(String currentRecordId, TransactionContext transactionContext) {
        // try to check id if not exists this code throw error
        da.getRecord(transactionContext.getApplicationName(), currentRecordId);
        // if currentRecordId exists check nau file
        try {
            da.getRecord("", "CUSTOMER", "$NAU", currentRecordId);
            // exists in NAU file throw error
            MsbUtils.setError(tafjRuntime, "Record already exists in NAU file.");
        } catch (Exception ex) {
            // not exists in NAU
        }

        return currentRecordId;
    }
}
