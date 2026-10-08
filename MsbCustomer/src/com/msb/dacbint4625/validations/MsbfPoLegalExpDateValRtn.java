package com.msb.dacbint4625.validations;

import java.util.Arrays;
import java.util.List;
import com.msbf.common.utilities.MsbUtils;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.paymentorder.PaymentOrderRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfPoLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        PaymentOrderRecord po = new PaymentOrderRecord(currentRecord);
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfPoLegalExpDateValRtn function = " + function);
        if (!function.equals("INPUT")) {
            return po.getValidationResponse();
        }
        try {
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String poVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "PAYMENT.ORDER", "VERSION", da);
            List<String> poVersions = Arrays.asList(poVersion.split("\\*"));
            if(!poVersions.contains(version)){
                return po.getValidationResponse();
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String accountId = po.getDebitAccount().getValue();
            String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
            logger.info("MsbfPoLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
            if(!overrideIdGTTT.equals("")){
                po.getDebitAccount().setOverride(overrideIdGTTT);
            }
        } catch (Exception e) {
            logger.error("MsbfPoLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return po.getValidationResponse();
    }
    
}
