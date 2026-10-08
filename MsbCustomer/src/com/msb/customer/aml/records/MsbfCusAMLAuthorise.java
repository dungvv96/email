package com.msb.customer.aml.records;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.msbf.common.utilities.MsbUtils;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.override.OverrideRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;

public class MsbfCusAMLAuthorise extends RecordLifecycle {

    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    DataAccess da = new DataAccess(this);

    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) {
        // TODO Auto-generated method stub
        switch (transactionContext.getCurrentFunction().toUpperCase().charAt(0)) {
        case 'A':
            TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
            String today = session.getCurrentVariable("!TODAY");

            logger.info("MsbfCusAMLAuthorise: today = " + today);

            CustomerRecord customerRec = new CustomerRecord(currentRecord);
            String inputDate = "20" + customerRec.getDateTime(0).substring(0, 6);
            logger.info("MsbfCusAMLAuthorise:  inputDate = " + inputDate);
            if (inputDate.compareTo(today) != 0) {
                MsbUtils.setErrorLocalField(tafjRuntime, "Can thuc hien Edit/Delete ban ghi INAU qua ngay", "CUSTOMER",
                        "MSB.AML.DATETIME");
            }

            if (("HIT".equals(hasFieldValue(customerRec.getLocalRefField("MSB.AML.RESULT")))
                    || ("NO HIT".equals(hasFieldValue(customerRec.getLocalRefField("MSB.AML.RESULT")))
                            && "HIGH".equals(hasFieldValue(customerRec.getLocalRefField("MSB.RR.RESULT")))))
                    && !"Y".equals(hasFieldValue(customerRec.getLocalRefField("MSB.DECISION")))) {
                MsbUtils.setErrorLocalField(tafjRuntime, "Thong tin AML khong hop le", "CUSTOMER",
                        "MSB.AML.RESULT");
            }
            // if (condition) {
            //
            // }
            break;
        default:
            break;
        }

        return;
    }

    /*
     * get Override message by Override ID
     */
    public String getOverMess(String overId) {
        String overMess = "";
        logger.info("Start MsbfCusAMLAuthorise getOverMess overId:" + overId);
        try {
            OverrideRecord overRec = new OverrideRecord(da.getRecord("", "OVERRIDE", "", overId));
            overMess = overRec.getMessage().get(0).getMessage().get(0).getValue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
            logger.error("MsbfCusAMLAuthorise getOverMess {}:" + e);
        }
        logger.info("End MsbfCusAMLAuthorise getOverMess overId:" + overId);
        return overMess;

    }

    public String hasFieldValue(TField tField) {
        logger.info("Start MsbfCusAMLAuthorise hasFieldValue:");
        return tField == null ? "" : tField.getValue();
        // return java.util.Optional.ofNullable(tField.getValue()).orElse("");

    }
}
