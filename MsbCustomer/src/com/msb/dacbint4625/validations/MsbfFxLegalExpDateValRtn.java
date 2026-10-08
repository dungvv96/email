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
import com.temenos.t24.api.records.forex.ForexRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfFxLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        ForexRecord fx = new ForexRecord(currentRecord);
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfFxLegalExpDateValRtn function = " + function);
        if (!function.equals("INPUT")) {
            return fx.getValidationResponse();
        }
        try {
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String fxVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "FOREX", "VERSION", da);
            List<String> fxVersions = Arrays.asList(fxVersion.split("\\*"));
            if(!fxVersions.contains(version)){
                return fx.getValidationResponse();
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String accountId = fx.getOurAccountPay(0).getOurAccountRec().getValue();
            String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
            logger.info("MsbfFxLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);            
            if(!overrideIdGTTT.equals("")){
                fx.getOurAccountPay(0).getOurAccountRec().setOverride(overrideIdGTTT);
            }
        } catch (Exception e) {
            logger.error("MsbfFxLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return fx.getValidationResponse();
    }
    
}
