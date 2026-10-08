package com.msb.dacbint4625.validations;

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
public class MsbfCusIndivLegalExpDateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        String today = session.getCurrentVariable("!TODAY");
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        String national = cusRec.getNationality().getValue();
        
        // Check funtion: Chỉ validate khi input.
        String function = transactionContext.getCurrentFunction();
        logger.info("MsbfCusIndivLegalExpDateValRtn function = " + function);
        if (!function.equals("INPUT")) {
            return cusRec.getValidationResponse();
        }
        try {
            // Check mandatory với ngày hết hạn: Không check khi người nước ngoài với loại giấy tờ khác PP và VS
            boolean flagPp = false;
            for(int i = 0; i < cusRec.getLegalId().size(); i++){
                String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                boolean flagMandatory = false;
                if(national.equals("VN")){
                    flagMandatory = true;
                }
                else if(docName.equals("PP") || docName.equals("VS")){
                    flagMandatory = true;
                }
                
                if(docName.equals("PP")){
                    flagPp = true;
                }
                
                if(flagMandatory && cusRec.getLegalId(i).getLegalExpDate().getValue().trim().equals("")){
                    cusRec.getLegalId(i).getLegalExpDate().setError("INPUT MISSING");
                }
            }
            
            // Check bắt buộc nhập cả PP với KHCN là người NN:
            if(!national.equals("VN") && !flagPp){
                cusRec.getLegalId(0).getLegalDocName().setError("Nguoi nuoc ngoai bat buoc nhap thong tin Passport");
            }
            
            // Check validate với ngày TODAY: chỉ validate IDC/BC với KH VN, PP và VS với KH NN
            boolean flagIdcBc = false;
            boolean flagIdPpVs = false;
            for(int i = 0; i < cusRec.getLegalId().size(); i++){
                boolean flagCheckExpire = false;
                String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                if(national.equals("VN") && (docName.equals("IDC") || docName.equals("BC"))){
                    flagCheckExpire = true;
                    flagIdcBc = true;
                }
                if(!national.equals("VN") && (docName.equals("PP") || docName.equals("VS"))){
                    flagCheckExpire = true;
                    flagIdPpVs = true;
                }
                
                // Check validate với ngày TODAY
                String legalExpDate = cusRec.getLegalId(i).getLegalExpDate().getValue().trim();
                if(!legalExpDate.equals("")){
                    if(flagCheckExpire && today.compareTo(legalExpDate) > 0){
                        cusRec.getLegalId(i).getLegalExpDate().setError("GTTT da het han");
                    }
                    if(flagCheckExpire && today.compareTo(legalExpDate) == 0){
                        cusRec.getLegalId(i).getLegalExpDate().setOverride("GTTT chi con hieu luc het ngay hien tai");
                    }
                }
            }            
            
            // Chặn nếu loại giấy tờ VN mà không có CCCD hay GKS
            if(national.equals("VN") && !flagIdcBc && cusRec.getLegalId().size() > 0){
                cusRec.getLegalId(0).getLegalDocName().setError("GTTT khong con hieu luc den het 31/12/2024");
            }
            
            // Check bắt buộc nhập đủ PP và VS với người NN:
            if(!national.equals("VN") && !flagIdPpVs && cusRec.getLegalId().size() > 0){
                cusRec.getLegalId(0).getLegalExpDate().setOverride("MSB.KHCN.NN.PP.VS");
            }
        } catch (Exception e) {
            logger.error("MsbfCusIndivLegalExpDateValRtn ex=" + e.getLocalizedMessage());
        }
        
        return cusRec.getValidationResponse();
    }
    
}
