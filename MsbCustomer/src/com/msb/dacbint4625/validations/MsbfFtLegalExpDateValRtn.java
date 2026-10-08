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
import com.temenos.t24.api.records.fundstransfer.FundsTransferRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfFtLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        FundsTransferRecord ftRec = new FundsTransferRecord(currentRecord);
        try {            
            // Check funtion: Chỉ validate khi input.
            String function = transactionContext.getCurrentFunction();
            logger.info("MsbfFtLegalExpDateValRtn function = " + function);
            if (!function.equals("INPUT")) {
                return ftRec.getValidationResponse();
            }
            
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String ftVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "FUNDS.TRANSFER", "VERSION", da);
            List<String> ftVersions = Arrays.asList(ftVersion.split("\\*"));
            if(!ftVersions.contains(version)){
                return ftRec.getValidationResponse();
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String accountId = ftRec.getDebitAcctNo().getValue();
            String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
            logger.info("MsbfFtLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);            
            if(!overrideIdGTTT.equals("")){
                ftRec.getDebitAcctNo().setOverride(overrideIdGTTT);
            }
        } catch (Exception e) {
            logger.error("MsbfFtLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return ftRec.getValidationResponse();
    }
    
}
