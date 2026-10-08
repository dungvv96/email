package com.msb.dacbint4625.validations;

import com.msbf.common.utilities.MsbCustomerUtils;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfCusBizLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub

        // Customer Legal ID validations for Prospect and Individual customer
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        String today = session.getCurrentVariable("!TODAY");
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfCusBizLegalExpDateValRtn function = " + function);
        if (!function.equals("INPUT")) {
            return cusRec.getValidationResponse();
        }
        
        try {
            // Check bắt buộc nhập người đại diện pháp luật
            boolean flag = false;
            for(int i = 0; i < cusRec.getRelationCode().size(); i++){
                String relationCode = cusRec.getRelationCode(i).getRelationCode().getValue();           
                if(relationCode.equals("95")){
                    flag = true;
                }
            }
            if(!flag){
                cusRec.getRelationCode(0).getRelationCode().setOverride("MSB.KHDN.GTTT.95.MANDATORY");
                return cusRec.getValidationResponse();
            }
            
            // Check thời hạn giấy tờ tùy thân của người đại diện pháp luật và KTT:
            MsbCustomerUtils customerUtil = new MsbCustomerUtils(this);
            String overrideIdGTTT = "";
            for(int i = 0; i < cusRec.getRelationCode().size(); i++){
                String relationCode = cusRec.getRelationCode(i).getRelationCode().getValue();
                String customerId = cusRec.getRelationCode(i).getRelCustomer().getValue();
                logger.info("MsbfCusBizLegalExpDateValRtn customerId = " + customerId);
                logger.info("MsbfCusBizLegalExpDateValRtn relationCode = " + relationCode);
                
                // Kiểm tra thông tin GTTT người đại diện PL
                if(relationCode.equals("95")){
                    overrideIdGTTT = customerUtil.getDocumentExpStatusByCif(customerId, today);
                    logger.info("MsbfCusBizLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
                    if(!overrideIdGTTT.equals("")){
                        cusRec.getRelationCode(i).getRelationCode().setOverride(overrideIdGTTT);
                    }
                }
                
                // Kiểm tra thông tin GTTT của ủy quyền người đại diện PL
                if(relationCode.equals("96")){
                    overrideIdGTTT = customerUtil.getDocumentExpStatusByCif(customerId, today);
                    logger.info("MsbfCusBizLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
                    if(overrideIdGTTT.equals("MSB.KHCN.NN.PP.VS") || overrideIdGTTT.equals("MSB.KHDN.NN.PP.VS")){
                        cusRec.getRelationCode(i).getRelationCode().setOverride("MSB.KHDN.NN.PP.VS.OVE");
                    }
                    else if(!overrideIdGTTT.equals("")){
                        cusRec.getRelationCode(i).getRelationCode().setOverride("MSB.CIF.GTTT.96.EXPIRE");
                    }
                }
                
                // Kiểm tra thông tin GTTT KTT
                if(relationCode.equals("97")){
                    overrideIdGTTT = customerUtil.getDocumentExpStatusByCif(customerId, today);
                    logger.info("MsbfCusBizLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
                    if(!overrideIdGTTT.equals("")){
                        cusRec.getRelationCode(i).getRelationCode().setOverride(overrideIdGTTT);
                    }
                }
                
                // Kiểm tra thông tin GTTT ủy quyền KTT
                if(overrideIdGTTT.equals("MSB.KHCN.NN.PP.VS") || overrideIdGTTT.equals("MSB.KHDN.NN.PP.VS")){
                    cusRec.getRelationCode(i).getRelationCode().setOverride("MSB.KHDN.NN.PP.VS.OVE");
                }
                else if(relationCode.equals("98")){
                    overrideIdGTTT = customerUtil.getDocumentExpStatusByCif(customerId, today);
                    logger.info("MsbfCusBizLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);
                    if(!overrideIdGTTT.equals("")){
                        cusRec.getRelationCode(i).getRelationCode().setOverride("MSB.CIF.GTTT.98.EXPIRE");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("MsbfCusBizLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return cusRec.getValidationResponse();
    }
    
}
