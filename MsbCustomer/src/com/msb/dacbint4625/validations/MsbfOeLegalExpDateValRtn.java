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
import com.temenos.t24.api.records.pporderentry.PpOrderEntryRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfOeLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        PpOrderEntryRecord oe = new PpOrderEntryRecord(currentRecord);
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfOeLegalExpDateValRtn function = " + function);
        if (!function.equals("INPUT")) {
            return oe.getValidationResponse();
        }
        try {
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String oeVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "PP.ORDER.ENTRY", "VERSION", da);
            List<String> oeVersions = Arrays.asList(oeVersion.split("\\*"));
            if(!oeVersions.contains(version)){
                return oe.getValidationResponse();
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String accountId = oe.getDebitaccountnumber().getValue();
            String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
            logger.info("MsbfOeLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);            
            if(!overrideIdGTTT.equals("")){
                oe.getDebitaccountnumber().setOverride(overrideIdGTTT);
            }
        } catch (Exception e) {
            logger.error("MsbfOeLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return oe.getValidationResponse();
    }
    
}
