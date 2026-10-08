package com.msb.customer.defaultvalues;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.imdocumentimage.ImDocumentImageRecord;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author hunglm7
 *
 */
public class InpHubDocImage extends RecordLifecycle {

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        ImDocumentImageRecord Imd =new ImDocumentImageRecord(currentRecord);
        DataAccess da = new DataAccess(this);
        Session sess = new Session(this);
        String userId = sess.getUserId();
        UserRecord Usr = null;
        
        try {
            
            Usr = new UserRecord(da.getRecord("USER", userId));
            String InpHub = Usr.getLocalRefField("MSB.HUB").getValue();
            Imd.getLocalRefField("MSB.INPUT.HUB").setValue(InpHub);
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
        }
        currentRecord.set(Imd.toStructure());
    }

}
