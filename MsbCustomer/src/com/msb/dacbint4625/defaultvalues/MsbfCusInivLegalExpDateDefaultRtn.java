package com.msb.dacbint4625.defaultvalues;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.InputValue;
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
public class MsbfCusInivLegalExpDateDefaultRtn extends RecordLifecycle {

    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    Session session = new Session(this);
    
	@Override
    public void defaultFieldValuesOnHotField(String application, String currentRecordId, TStructure currentRecord,
            InputValue currentInputValue, TStructure unauthorisedRecord, TStructure liveRecord,
            TransactionContext transactionContext) {
        // TODO Auto-generated method stub
	    logger.info("MsbfCusInivLegalExpDateDefaultRtn.defaultFieldValuesOnHotField start process cif: {}, v1.0.0", currentRecordId);
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        String national = cusRec.getNationality().getValue();
        
        // Chỉ thực hiện khi hotfield Legal issue date  
        if(!currentInputValue.getFieldName().startsWith("LEGAL.ISS.DATE")) {
            return;
        }
        
        // Chỉ thực hiện default khi có date of birth
        String dateOfBirth = cusRec.getDateOfBirth().getValue().trim();
        if(dateOfBirth.equals("")){
            return;
        }
        
        try {
            for(int i = 0; i < cusRec.getLegalId().size(); i++){
                String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                String issueDate = cusRec.getLegalId(i).getLegalIssDate().getValue();
                if(national.equals("VN") && docName.equals("IDC")){
                    String expDate = getIdcExpDate(dateOfBirth, issueDate);
                    cusRec.getLegalId(i).getLegalExpDate().setValue(expDate);
                }
                if(national.equals("VN") && docName.equals("BC")){
                    String expDate = getGksExpDate(dateOfBirth, issueDate);
                    cusRec.getLegalId(i).getLegalExpDate().setValue(expDate);
                }
            }
        } catch (Exception e) {
            logger.error("MsbfCusInivLegalExpDateDefaultRtn ex=" + e.getLocalizedMessage());
        }
        
        currentRecord.set(cusRec.toStructure());
        logger.info("MsbfCusInivLegalExpDateDefaultRtn.defaultFieldValuesOnHotField end process cif: {}, v1.0.0", currentRecordId);
    }

	// Tính ngày hết hạn khi KH được cấp IDC
	private String getIdcExpDate(String dateOfBirth, String issueDate){
	    String expDate = "";
	    int years = 0;
	    int mmdd = 0;
	    try{
	        int year1 = Integer.parseInt(dateOfBirth.substring(0, 4));
	        int year2 = Integer.parseInt(issueDate.substring(0, 4));
	        int mmdd1 = Integer.parseInt(dateOfBirth.substring(4, 8));
	        int mmdd2 = Integer.parseInt(issueDate.substring(4, 8));
	        years = year2 - year1;
	        mmdd = mmdd2 - mmdd1;
	        
	        if(years < 25 || (years == 25 && mmdd < 0)){
	            int year = year1 + 25;
	            expDate = year + dateOfBirth.substring(4);
	        }
	        else if(years < 40 || (years == 40 && mmdd < 0)){
	            int year = year1 + 40;
	            expDate = year + dateOfBirth.substring(4);
	        }
	        else if(years < 58 || (years == 58 && mmdd < 0)){
                int year = year1 + 60;
                expDate = year + dateOfBirth.substring(4);
            }
	        else{
                expDate = "22000101";
            }
	    }catch(Exception ex){
	        logger.error("MsbfCusInivLegalExpDateDefaultRtn.getIdcExpDate ex = " + ex.getMessage());
	    }
	    return expDate;
	}
	
	// Tính ngày hết hạn khi KH được cấp giấy khai sinh
    private String getGksExpDate(String dateOfBirth, String issueDate){
        String expDate = "";
        try{
            int year1 = Integer.parseInt(dateOfBirth.substring(0, 4));
            int year = year1 + 18;
            expDate = year + dateOfBirth.substring(4);
        }catch(Exception ex){
            logger.error("MsbfCusInivLegalExpDateDefaultRtn.getGksExpDate ex = " + ex.getMessage());
        }
        return expDate;
    }
}
