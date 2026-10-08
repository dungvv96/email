package com.msb.signatures.validations;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.imdocumentimage.ImDocumentImageRecord;

/**
 * TODO: Document me!
 *
 * @author hunglm7
 *
 */
public class MsbValidNewImImage extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        ImDocumentImageRecord imd = new ImDocumentImageRecord(currentRecord);
        try {
            if (!imd.getImage().getValue().isEmpty()) {
                imd.getImage().setError("Chinh sua khong hop le, de nghi su dung chuc nang amend signatures/documents!");
            }                
        } catch (Exception e) {
            throw new T24CoreException("Exception in MsbValidNewImImage " + e.getLocalizedMessage());
        }
        
        return imd.getValidationResponse();
    }

}
