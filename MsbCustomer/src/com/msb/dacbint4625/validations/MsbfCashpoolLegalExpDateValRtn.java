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
import com.temenos.t24.api.records.accashpool.AcCashPoolRecord;
import com.temenos.t24.api.records.override.OverrideRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfCashpoolLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        AcCashPoolRecord cpRec = new AcCashPoolRecord(currentRecord);
        try {            
            // Check funtion: Chỉ validate khi input.
            String function = transactionContext.getCurrentFunction();
            logger.info("MsbfCashpoolLegalExpDateValRtn function = " + function);
            if (!function.equals("INPUT")) {
                return cpRec.getValidationResponse();
            }
            
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String acVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "AC.CASH.POOL", "VERSION", da);
            List<String> acVersions = Arrays.asList(acVersion.split("\\*"));
            if(!acVersions.contains(version)){
                return cpRec.getValidationResponse();
            }
            
            
            
            // Check GTTT link account:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String accountId = cpRec.getLinkAcct(0).getLinkAcct().getValue();
            String overrideIdGTTT = legalUtil.getDocumentExpStatus(accountId);
            logger.info("MsbfCashpoolLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);            
            if(!overrideIdGTTT.equals("")){
                cpRec.getLinkAcct(0).getLinkAcct().setOverride(overrideIdGTTT);
            }
            
            // Check GTTT Id account:
            overrideIdGTTT = legalUtil.getDocumentExpStatus(currentRecordId);
            logger.info("MsbfCashpoolLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
            if(!overrideIdGTTT.equals("")){
                cpRec.getLinkAcct(0).getLinkAcct().setOverride(overrideIdGTTT);
            }
        } catch (Exception e) {
            logger.error("MsbfCashpoolLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return cpRec.getValidationResponse();
    }

    
    
    @Override
    public String checkId(String currentRecordId, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        String overrideIdGTTT = "";
        String detail = "";
        try {            
            // Check funtion: Chỉ validate khi input.
            String function = transactionContext.getCurrentFunction();
            logger.info("MsbfCashpoolLegalExpDateValRtn function = " + function);
            if (!function.equals("INPUT")) {
                return super.checkId(currentRecordId, transactionContext);
            }
            
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String acVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "AC.CASH.POOL", "VERSION", da);
            List<String> acVersions = Arrays.asList(acVersion.split("\\*"));
            if(!acVersions.contains(version)){
                return super.checkId(currentRecordId, transactionContext);
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            overrideIdGTTT = legalUtil.getDocumentExpStatus(currentRecordId);
            logger.info("MsbfCashpoolLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
            if(!overrideIdGTTT.equals("")){
                OverrideRecord overrideRec = new OverrideRecord(da.getRecord("OVERRIDE", overrideIdGTTT));
                detail = overrideRec.getMessage(0).getMessage(0).getValue();
            }
        } catch (Exception e) {
            logger.error("MsbfCashpoolLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        if(!overrideIdGTTT.equals("MSB.KHCN.NN.PP.VS") && !overrideIdGTTT.equals("MSB.KHDN.NN.PP.VS")){
            throw new RuntimeException(detail);
        }
        return super.checkId(currentRecordId, transactionContext);
    }



    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        AcCashPoolRecord cpRec = new AcCashPoolRecord(currentRecord);
        String overrideIdGTTT = "";
        try {            
            // Check funtion: Chỉ validate khi input.
            String function = transactionContext.getCurrentFunction();
            logger.info("MsbfCashpoolLegalExpDateValRtn function = " + function);
            if (!function.equals("INPUT")) {
                return;
            }
            
            // Lấy thông tin cấu hình xem verion có thực hiện validate GTTT không, nếu không thì return:
            String version = transactionContext.getCurrentVersionId();
            String acVersion = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "AC.CASH.POOL", "VERSION", da);
            List<String> acVersions = Arrays.asList(acVersion.split("\\*"));
            if(!acVersions.contains(version)){
                return;
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            overrideIdGTTT = legalUtil.getDocumentExpStatus(currentRecordId);
            logger.info("MsbfCashpoolLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
        } catch (Exception e) {
            logger.error("MsbfCashpoolLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        String detail = "";
        if(!overrideIdGTTT.equals("")){
            OverrideRecord overrideRec = new OverrideRecord(da.getRecord("OVERRIDE", overrideIdGTTT));
            detail = overrideRec.getMessage(0).getMessage(0).getValue();
            if(overrideIdGTTT.equals("MSB.KHCN.NN.PP.VS") || overrideIdGTTT.equals("MSB.KHDN.NN.PP.VS")){
                cpRec.getOverride(0).setValue(detail);
            }
            else{
                throw new RuntimeException(detail);
            }
        }
    }
    
}
