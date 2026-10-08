package com.msb.dacbint4625.validations;

import java.util.Arrays;
import java.util.List;
import com.msbf.common.utilities.MsbUtils;
import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangementactivity.AaArrangementActivityRecord;
import com.temenos.t24.api.records.aaarrsettlement.AaArrSettlementRecord;
import com.temenos.t24.api.records.aaproduct.AaProductRecord;
import com.temenos.t24.api.records.aaproductgroup.AaProductGroupRecord;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfAaaLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        AaArrangementActivityRecord aaa = new AaArrangementActivityRecord(currentRecord);
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfAaaLegalExpDateValRtn function = " + function + " AA = " + aaa.getArrangement().getValue());
        if (!function.equals("INPUT")) {
            return aaa.getValidationResponse();
        }
        try {
            // CHECK ACTIVITY:
            String activity = aaa.getActivity().getValue();
            String configActivity = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "AA.ARRANGEMENT.ACTIVITY", "ACTIVITY", da);
            List<String> configActivities = Arrays.asList(configActivity.split("\\*"));
            if(!configActivities.contains(activity)){
                return aaa.getValidationResponse();
            }
            
            // Lấy PRODUCT.LINE, CUSTOMER.ID: 
            // Nếu activity new arrangement thì đọc theo product. Nếu activity settle payout/payin thì đọc trong AA record
            String aaId = aaa.getArrangement().getValue();
            String cusId = aaa.getCustomer(0).getCustomer().getValue();
            String product = aaa.getProduct().getValue();
            String productline = "";
            try{
                if(!product.equals("")){
                    AaProductRecord productRec = new AaProductRecord(da.getRecord("AA.PRODUCT", product));
                    String productGroup = productRec.getProductGroup().getValue();
                    AaProductGroupRecord groupRec = new AaProductGroupRecord(da.getRecord("AA.PRODUCT.GROUP", productGroup));
                    productline = groupRec.getProductLine().getValue();
                }
            }catch(Exception ex){
                logger.error("MsbfAaaLegalExpDateValRtn AaProductRecord ex=" + ex.getLocalizedMessage()); 
            }
            
            if(productline.equals("")){
                try{
                    AaArrangementRecord aaRec = new AaArrangementRecord(da.getRecord("AA.ARRANGEMENT", aaId));
                    productline = aaRec.getProductLine().getValue();
                    String accountId = aaRec.getLinkedAppl(0).getLinkedApplId().getValue();
                    AccountRecord acRec = new AccountRecord(da.getRecord("ACCOUNT", accountId));
                    cusId = acRec.getCustomer().getValue();
                }catch(Exception ex){
                    logger.error("MsbfAaaLegalExpDateValRtn AaArrangementRecord ex=" + ex.getLocalizedMessage()); 
                }
            }
            
            if(!productline.equals("")){
                String productParam = MsbUtils.msbGetParameter("MSB.TT17.GTTT", "AA.ARRANGEMENT.ACTIVITY", "PRODUCT.LINE", da);
                List<String> productParams = Arrays.asList(productParam.split("\\*"));
                if(!productParams.contains(productline)){
                    return aaa.getValidationResponse();
                }
            }
            
            // Check GTTT:
            MsbfLegalUtils legalUtil = new MsbfLegalUtils(this);
            String overrideIdGTTT = legalUtil.getDocumentExpStatusByCif(cusId);
            logger.info("MsbfAaaLegalExpDateValRtn cusId = " + cusId);
            logger.info("MsbfAaaLegalExpDateValRtn overrideIdGTTT = " + overrideIdGTTT);            
            if(!overrideIdGTTT.equals("")){
                aaa.getArrangement().setOverride(overrideIdGTTT);
            }
        } catch (Exception e) {
            logger.error("MsbfAaaLegalExpDateValRtn e=" + e.getLocalizedMessage());
        }
        
        return aaa.getValidationResponse();
    }
    
}
