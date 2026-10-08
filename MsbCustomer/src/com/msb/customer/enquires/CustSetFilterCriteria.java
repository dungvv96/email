package com.msb.customer.enquires;

import java.util.List;

import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * TODO: Document me!
 *
 * @author hunglm7
 *
 */
public class CustSetFilterCriteria extends Enquiry {

    @Override
    public List<FilterCriteria> setFilterCriteria(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        FilterCriteria criteria = new FilterCriteria();
        DataAccess da = new DataAccess(this);
        Session sess = new Session(this);
        String userId = sess.getUserId();
        UserRecord Usr = null;
        try {        
        Usr = new UserRecord(da.getRecord("USER", userId)); 
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
        }
        String UserHub = Usr.getLocalRefField("MSB.HUB").getValue();         
        
        boolean isSearchInpHubEQ = true;
      
        for (FilterCriteria fc : filterCriteria) {
            if (fc.getFieldname().equals("MSB.INPUT.HUB") && !fc.getValue().isEmpty()) {
                isSearchInpHubEQ = false;
            }
            };
        if (isSearchInpHubEQ) {
            criteria.setFieldname("MSB.INPUT.HUB");
            criteria.setOperand("EQ");
            criteria.setValue(UserHub);
            filterCriteria.add(criteria);            
        }    

        return filterCriteria; 
    }

}