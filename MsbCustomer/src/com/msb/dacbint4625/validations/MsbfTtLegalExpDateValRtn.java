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
import com.temenos.t24.api.records.teller.TellerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfTtLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        TellerRecord tt = new TellerRecord(currentRecord);
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfTtLegalExpDateValRtn function = " + function);
        if (!function.equals("INPUT")) {
            return tt.getValidationResponse();
        }
        
        try {
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String ac1Version = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "TELLER", "VERSION.CHECK.ACCOUNT.1", da);
            String ac2Version = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "TELLER", "VERSION.CHECK.ACCOUNT.2", da);
            List<String> ac1Versions = Arrays.asList(ac1Version.split("\\*"));
            List<String> ac2Versions = Arrays.asList(ac2Version.split("\\*"));
            if(!ac1Versions.contains(version) && !ac2Versions.contains(version)){
                return tt.getValidationResponse();
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String accountId = "";
            if(ac1Versions.contains(version)){
                accountId = tt.getAccount1(0).getAccount1().getValue();
                String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
                logger.info("MsbfTtLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
                if(!overrideIdGTTT.equals("")){
                    tt.getAccount1(0).getAccount1().setOverride(overrideIdGTTT);
                }
            }
            
            if(ac2Versions.contains(version)){
                accountId = tt.getAccount2().getValue();
                String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
                logger.info("MsbfTtLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
                if(!overrideIdGTTT.equals("")){
                    tt.getAccount2().setOverride(overrideIdGTTT);
                }
            }
        } catch (Exception e) {
            logger.error("MsbfTtLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return tt.getValidationResponse();
    }
    
}
