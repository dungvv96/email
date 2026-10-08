package com.msb.customer.validations;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.customer.LegalIdClass;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfCustomerLegalIdValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        try {
            List<LegalIdClass> documentNames = cusRec.getLegalId();
            int idDocsize = documentNames.size();
            logger.info("MsbfCustomerLegalIdValRtn: idDocsize=" + idDocsize);
            CheckDuplication(cusRec, idDocsize);  
        } catch (Exception e) {
            throw new T24CoreException("Error in main method MsbVCusLegalIdVal " + e.getLocalizedMessage());
        }
        return cusRec.getValidationResponse();
    }
    
    /**
     * @param cusRec
     * @param idDocsize
     */
    private void CheckDuplication(CustomerRecord cusRec, int idDocsize) {
        // Checks for Duplicate values in legal id field
        try {
            int j = 0;
            for (int i = 0; i < idDocsize - 1; i++) {
                String legalId1 = cusRec.getLegalId(i).getLegalId().getValue();
                logger.info("MsbfCustomerLegalIdValRtn: legalId1=" + legalId1);                
                for (j = i + 1; j < idDocsize; j++) {
                    String legalId2 = cusRec.getLegalId(j).getLegalId().getValue();                    
                    if (legalId1.equals(legalId2) && (i != j)) {
                        cusRec.getLegalId(j).getLegalId().setError("EB-MSB.CUS.DUP");
                    }
                }
            }
        } catch (Exception e) {
            throw new T24CoreException("Exception in CheckDuplication " + e);
        }
    }
}
