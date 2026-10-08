package com.msb.customer.aml.records;

import org.apache.commons.lang3.StringUtils;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfCusFatcaValRtn extends RecordLifecycle {

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        CustomerRecord customerRec = new CustomerRecord(currentRecord);
        String citizen = customerRec.getLocalRefField("MSB.US.CITIZEN").getValue();
        String usBirth = customerRec.getLocalRefField("MSB.US.BIRTH").getValue();
        String usCustAddr = customerRec.getLocalRefField("MSB.US.CUST.ADDR").getValue();
        String usPhNo = customerRec.getLocalRefField("MSB.US.PH.NO").getValue();
        String usRegFix = customerRec.getLocalRefField("MSB.US.REG.FIX").getValue();
        String usAttorneyAddr = customerRec.getLocalRefField("MSB.US.ATTORNEY.ADDR").getValue();
        String usPermAddr = customerRec.getLocalRefField("MSB.US.MAIL.PERM.ADDR").getValue();
        String haveNtFatca = customerRec.getLocalRefField("MSB.HAVE.NOT.FATCA").getValue();
        String commitForm = customerRec.getLocalRefField("MSB.COMMT.FORM").getValue();
  
        // Validate FATCA infor
        if (haveNtFatca.isEmpty() && citizen.isEmpty() && usBirth.isEmpty() && usCustAddr.isEmpty() && usPhNo.isEmpty()
                && usRegFix.isEmpty() && usAttorneyAddr.isEmpty() && usPermAddr.isEmpty() && commitForm.isEmpty()) {
            customerRec.getLocalRefField("MSB.HAVE.NOT.FATCA")
                    .setError("FATCA information is required, please tick the appropriate value");
        }
        
        if (haveNtFatca.contentEquals("Y")) {
            if (citizen != null && !citizen.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.CITIZEN").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (usBirth != null && !usBirth.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.BIRTH").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (usCustAddr != null && !usCustAddr.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.CUST.ADDR").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (usPhNo != null && !usPhNo.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.PH.NO").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (usRegFix != null && !usRegFix.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.REG.FIX").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (usAttorneyAddr != null && !usAttorneyAddr.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.ATTORNEY.ADDR").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (usPermAddr != null && !usPermAddr.isEmpty()) {
                customerRec.getLocalRefField("MSB.US.MAIL.PERM.ADDR").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
            if (commitForm != null && !commitForm.isEmpty()) {
                customerRec.getLocalRefField("MSB.COMMT.FORM").setError("EB-MSB.CR.HAVE.NOT.FATCA");
            }
        }
        
        return customerRec.getValidationResponse();
    }

}
