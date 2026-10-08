package com.msb.customer.validations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.msbf.common.utilities.MsbUtils;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfCustomerCheckDuplicateValRtn extends RecordLifecycle {
    DataAccess da = new DataAccess(this);
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        // Customer Legal ID validations for Prospect and Individual customer
        CustomerRecord cusRec = new CustomerRecord(currentRecord);
        CustomerRecord cusRecLive = new CustomerRecord(liveRecord);
        boolean checkChannel = checkChangeChannel(cusRec, cusRecLive);
        boolean checkLegal = checkChangeLegal(cusRec, cusRecLive);
        boolean checkTax = checkChangeTax(cusRec, cusRecLive);
        boolean checkContact = checkChangeContact(cusRec, cusRecLive);
        boolean checkDOB = checkChangeDOB(cusRec, cusRecLive);
        boolean checkName = checkMsbFullName(cusRec, cusRecLive);
        logger.info("MsbfCustomerCheckDuplicateValRtn: checkChannel=" + checkChannel);
        logger.info("MsbfCustomerCheckDuplicateValRtn: checkLegal=" + checkLegal);
        logger.info("MsbfCustomerCheckDuplicateValRtn: checkTax=" + checkTax);
        logger.info("MsbfCustomerCheckDuplicateValRtn: checkContact=" + checkContact);
        logger.info("MsbfCustomerCheckDuplicateValRtn: checkDOB=" + checkDOB);
        logger.info("MsbfCustomerCheckDuplicateValRtn: checkName=" + checkName);
        
        // Nếu nâng cấp channel của KH lên quầy: Cần check trùng tất cả 4 thông tin
        if(checkChannel){
            checkLegal = true;
            checkTax = true;
            checkContact = true;
            checkDOB = true;
            checkName = true;
        }
        
        try {
            // Check duplicate LEGAL.ID if change LEGAL.ID:
            if(checkLegal){
                List<String> customerIds = new ArrayList<String>();
                String sql = "SELECT RECID FROM V_FMSB_CUSTOMER WHERE RECID !='" + currentRecordId + "' AND (XMLEXISTS('$t[c34/text()=$id]' PASSING \"LEGAL_ID_34\" as \"t\", ? as \"id\"))";
                for(int i = 0; i < cusRec.getLegalId().size(); i++){
                    customerIds = executeSelect(sql, cusRec.getLegalId(i).getLegalId().getValue());
                    if(customerIds.size() > 0){
                        break;
                    }
                }
                
                logger.info("MsbfCustomerCheckDuplicateValRtn: customerIds=" + customerIds.toString());
                if(customerIds.size() > 0){
                    List<String> overrideFields = new ArrayList<String>();
                    overrideFields.add("LEGAL.ID");
                    String overrideMsg = MsbUtils.getDynamicOverride("MSBF.CUS.CHECK.DUP.MSG", overrideFields);
                    cusRec.getLegalId(0).getLegalId().setOverride(overrideMsg);
                    return cusRec.getValidationResponse();
                }
            }
            
            // Check duplicate MSB.TAX.ID
            if(checkTax){
                List<String> customerIds = new ArrayList<String>();
                String sql = "SELECT RECID FROM V_FMSB_CUSTOMER WHERE RECID !='" + currentRecordId + "' AND MSB_TAX_ID = ?";
                customerIds = executeSelect(sql, cusRec.getLocalRefField("MSB.TAX.ID").getValue());
                logger.info("MsbfCustomerCheckDuplicateValRtn: customerIds=" + customerIds.toString());
                                
                if(customerIds.size() > 0){
                    List<String> overrideFields = new ArrayList<String>();
                    overrideFields.add("MSB.TAX.ID");
                    String overrideMsg = MsbUtils.getDynamicOverride("MSBF.CUS.CHECK.DUP.MSG", overrideFields);
                    cusRec.getLocalRefField("MSB.TAX.ID").setOverride(overrideMsg);
                    return cusRec.getValidationResponse();
                }
            }
            
            // Check duplicate CONTACT.DATA:
            if(checkContact){
                List<String> customerIds = new ArrayList<String>();
                
                String sql = "SELECT RECID FROM V_FMSB_CUSTOMER WHERE RECID !='" + currentRecordId + "' AND (XMLEXISTS('$t[c175/text()=$id]' PASSING \"CONTACT_DATA_175\" as \"t\", ? as \"id\"))";
                for(int i = 0; i < cusRec.getContactType().size(); i++){
                    customerIds = executeSelect(sql, cusRec.getContactType(i).getContactData().getValue());
                    if(customerIds.size() > 0){
                        break;
                    }
                }

                logger.info("MsbfCustomerCheckDuplicateValRtn: customerIds=" + customerIds.toString());
                if(customerIds.size() > 0){
                    List<String> overrideFields = new ArrayList<String>();
                    overrideFields.add("CONTACT.DATA");
                    String overrideMsg = MsbUtils.getDynamicOverride("MSBF.CUS.CHECK.DUP.MSG", overrideFields);
                    cusRec.getContactType(0).getContactData().setOverride(overrideMsg);
                    return cusRec.getValidationResponse();
                }

            }
            
            // Check duplicate MSB.FULL.NAME + DATE.OF.BIRTH:
            String fullname = getMsbFullname(cusRec);
            String dateOfBirth = cusRec.getDateOfBirth().getValue();
            if((checkDOB || checkName) && !fullname.equals("")){
                List<String> customerIds = new ArrayList<String>();
                String sql = "SELECT RECID FROM V_FMSB_CUSTOMER WHERE RECID !='" + currentRecordId + "' AND DATE_OF_BIRTH = ?";
                customerIds = executeSelect(sql, dateOfBirth);
                logger.info("MsbfCustomerCheckDuplicateValRtn: customerIds=" + customerIds.toString());
                
                boolean flag = false;
                for(int i = 0; i < customerIds.size(); i++){
                    CustomerRecord cus = new CustomerRecord(da.getRecord("CUSTOMER", customerIds.get(i)));
                    String name = getMsbFullname(cus);
                    if(fullname.equals(name)){
                        flag = true;
                        break;
                    }
                }
                
                if(flag){
                    List<String> overrideFields = new ArrayList<String>();
                    overrideFields.add("MSB.FULL.NAME & DATE.OF.BIRTH");
                    String overrideMsg = MsbUtils.getDynamicOverride("MSBF.CUS.CHECK.DUP.MSG", overrideFields);
                    cusRec.getLocalRefGroups("MSB.FULL.NAME").get(0).getLocalRefField("MSB.FULL.NAME").setOverride(overrideMsg);
                    return cusRec.getValidationResponse();
                }
            }
                
        } catch (Exception e) {
            throw new T24CoreException("Error in main method MsbfCustomerCheckDuplicateValRtn " + e.getLocalizedMessage());
        }
        return cusRec.getValidationResponse();
    }
    
    // Check if change LEGAL.ID
    private boolean checkChangeLegal(CustomerRecord currentRec, CustomerRecord liveRec){
        boolean ret = false;
        try{
            // Check size:
            if(currentRec.getLegalId().size() == 0){
                return ret;
            }            
            if(currentRec.getLegalId().size() != liveRec.getLegalId().size()){
                ret = true;
            }
            
            // Check one by one
            for(int i = 0; i < currentRec.getLegalId().size(); i++){
                if(!currentRec.getLegalId(i).getLegalId().getValue().equals(liveRec.getLegalId(i).getLegalId().getValue())){
                    ret = true;
                    break;
                }
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: Exception = " + ex.getMessage());
        }
        
        return ret;
    }
    
    // Check if change MSB.TAX.ID
    private boolean checkChangeTax(CustomerRecord currentRec, CustomerRecord liveRec){
        boolean ret = false;
        try{           
            if(currentRec.getLocalRefField("MSB.TAX.ID").getValue().equals("")){
                return ret;
            }
            
            if(!currentRec.getLocalRefField("MSB.TAX.ID").getValue().equals(liveRec.getLocalRefField("MSB.TAX.ID").getValue())){
                ret = true;
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: Exception = " + ex.getMessage());
        }
        
        return ret;
    }
    
    // Check if change CONTACT.DATA
    private boolean checkChangeContact(CustomerRecord currentRec, CustomerRecord liveRec){
        boolean ret = false;
        
        try{
            // Check size:
            if(currentRec.getContactType().size() == 0){
                return ret;
            }  
            if(currentRec.getContactType().size() != liveRec.getContactType().size()){
                ret = true;
            }
            
            // Check one by one
            for(int i = 0; i < currentRec.getContactType().size(); i++){
                if(!currentRec.getContactType(i).getContactData().getValue().equals(liveRec.getContactType(i).getContactData().getValue())){
                    ret = true;
                    break;
                }
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: Exception = " + ex.getMessage());
        }
        return ret;
    }

    // Check if change DATE.OF.BIRTH
    private boolean checkChangeDOB(CustomerRecord currentRec, CustomerRecord liveRec){
        boolean ret = false;
        try{         
            if(currentRec.getDateOfBirth().getValue().equals("")){
                return ret;
            }
            if(!currentRec.getDateOfBirth().getValue().equals(liveRec.getDateOfBirth().getValue())){
                ret = true;
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: Exception = " + ex.getMessage());
        }
        
        return ret;
    }
    
    // Check if change MSB.FULL.NAME
    private boolean checkMsbFullName(CustomerRecord currentRec, CustomerRecord liveRec){
        boolean ret = false;
        try{
            String currentName = getMsbFullname(currentRec);
            String liveName = getMsbFullname(liveRec);
            logger.info("MsbfCustomerCheckDuplicateValRtn: currentName = " +currentName);
            logger.info("MsbfCustomerCheckDuplicateValRtn: liveName = " +liveName);
            if(!currentName.equals(liveName)){
                ret = true;
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: Exception = " + ex.getMessage());
        }
        
        return ret;
    }
    
    // Check if change CHANNEL to BRANCH
    private boolean checkChangeChannel(CustomerRecord currentRec, CustomerRecord liveRec){
        boolean ret = false;
        try{
            String channel = currentRec.getLocalRefField("MSB.CUS.OB.CHANNEL").getValue();
            String liveChannel = liveRec.getLocalRefField("MSB.CUS.OB.CHANNEL").getValue();
            if(!channel.equals(liveChannel) && channel.toUpperCase().equals("E-KYC-DONE")){
                ret = true;
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: Exception = " + ex.getMessage());
        }
        
        return ret;
    }

    // Get MSB.FULL.NAME
    private String getMsbFullname(CustomerRecord customerRecord){
        String ret = "";
        try{
            for(int i = 0; i < customerRecord.getLocalRefGroups("MSB.FULL.NAME").size(); i++){
                ret = ret.concat(" ").concat(customerRecord.getLocalRefGroups("MSB.FULL.NAME").get(i).getLocalRefField("MSB.FULL.NAME").getValue());
            }
            ret = ret.trim();
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: getMsbFullname ex=" + ex.getMessage());
        }
        return ret;
    }
    
    
    /**
     * 
     * @param sql
     * @return
     */
    private List<String> executeSelect(String sql, String param) {
        logger.info("MsbfCustomerCheckDuplicateValRtn: SQL =" + sql);
        TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
        List<String> result = new ArrayList<String>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        try{
            Connection connection = tafjRuntime.getConnectionAPIDB();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, param);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(resultSet.getString("RECID"));
            }
        }catch(Exception ex){
            logger.error("MsbfCustomerCheckDuplicateValRtn: execute SQL exception=" + ex.getMessage());        
        }
        finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception ex) {
                logger.error("MsbfCustomerCheckDuplicateValRtn: close resultSet or preparedStatement Exception=" + ex.getMessage());
            }
        }
        
        return result;
    }
}
