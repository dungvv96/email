package com.msb.customer.aml.records;

import com.temenos.api.TBoolean;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.version.VersionRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.temenos.t24.api.complex.eb.templatehook.AutomaticAuthorisationContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;

/**
 * TODO: Document me!
 *
 * @author anhpt29
 *
 */
public class MsbfCusAutoAuthEnableRtn extends RecordLifecycle {
    Logger logger = LoggerFactory.getLogger("IRIS_DEV");
    DataAccess da = new DataAccess(this);
    Session session = new Session(this);

    CustomerRecord cusRecord;
    String versionId = "";

    @Override
    public TBoolean enableAutomaticAuthorisation(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            AutomaticAuthorisationContext automaticAuthorisationContext) {
        // TODO Auto-generated method stub
        try {
            logger.info("MsbfCusAutoAuthEnableRtn.enableAutomaticAuthorisation  START");
            versionId = application + transactionContext.getCurrentVersionId();
            logger.info("MsbfCusAutoAuthEnableRtn.enableAutomaticAuthorisation versionId:" + versionId);
            cusRecord = new CustomerRecord(currentRecord);

            if ("NO HIT".equals(hasFieldValue(cusRecord.getLocalRefField("MSB.AML.RESULT")))
                    && ("".equals(hasFieldValue(cusRecord.getLocalRefField("MSB.RR.RESULT")))
                            || "LOW".equals(hasFieldValue(cusRecord.getLocalRefField("MSB.RR.RESULT")))
                            || "MEDIUM".equals(hasFieldValue(cusRecord.getLocalRefField("MSB.RR.RESULT"))))) {
                logger.info("MsbfCusAutoAuthEnableRtn.enableAutomaticAuthorisation NO HIT End");
                return (new TBoolean(true));
            } else {
                VersionRecord versionRecord = new VersionRecord(da.getRecord("", "VERSION", "", versionId));
                if (versionRecord.getNoOfAuth().getValue().equals("0")) {
                    logger.info("MsbfCusAutoAuthEnableRtn.enableAutomaticAuthorisation no of auth = 0 End");
                    return (new TBoolean(true));
                } else {
                    logger.info("MsbfCusAutoAuthEnableRtn.enableAutomaticAuthorisation no of auth <> 0 End");
                    return (new TBoolean(false));
                }
            }
        } catch (Exception ex) {
            logger.error("MsbfCusAutoAuthEnableRtn.enableAutomaticAuthorisation exception = " + ex.getMessage());
            return (new TBoolean(false));
        }
    }

    public String hasFieldValue(TField tField) {
        logger.info("Start MsbfCusAutoAuthEnableRtn hasFieldValue:");
        return tField == null ? "" : tField.getValue();
        // return java.util.Optional.ofNullable(tField.getValue()).orElse("");

    }
}
