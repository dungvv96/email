package com.msb.signatures.defaultvalues;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.imdocumentimage.ImDocumentImageRecord;

/**
 * TODO: Document me!
 *
 * @author hunglm7
 *
 */
public class DefaultImExpDate extends RecordLifecycle {

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        ImDocumentImageRecord ImDocImage = new ImDocumentImageRecord(currentRecord);
        if (ImDocImage.getLocalRefField("MSB.IM.EXPIRY.DATE").getValue().isEmpty()) {
            ImDocImage.getLocalRefField("MSB.IM.EXPIRY.DATE").setValue("22000101");
        }
        currentRecord.set(ImDocImage.toStructure());
    }

}
