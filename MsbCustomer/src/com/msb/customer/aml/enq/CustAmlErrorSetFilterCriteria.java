package com.msb.customer.aml.enq;

import java.util.List;

import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class CustAmlErrorSetFilterCriteria extends Enquiry {
    
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

    @Override
    public List<FilterCriteria> setFilterCriteria(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        FilterCriteria criteria = new FilterCriteria();
        DataAccess da = new DataAccess(this);
        try {        
            String condition = "WITH MSB.AML.RESULT EQ 'HIT' OR (MSB.AML.RESULT EQ 'NO HIT' AND MSB.RR.RESULT EQ 'HIGH') OR MSB.DECISION EQ 'N' 'P'";
            logger.info("CustAmlErrorSetFilterCriteria: condition = " + condition);

            // Lấy danh sách KH chờ duyệt bị lỗi AML
            List<String> ids = da.selectRecords("", "CUSTOMER", "$NAU", condition);
            logger.info("CustAmlErrorSetFilterCriteria: ids = " + ids.toString());
            
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < ids.size(); i++){
                sb.append(ids.get(i)).append(" ");
            }
            criteria.setFieldname("@ID");
            criteria.setOperand("EQ");
            criteria.setValue(sb.toString().trim());
            filterCriteria.add(criteria);            

            return filterCriteria;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
            logger.info("CustAmlErrorSetFilterCriteria: e = " + e.getMessage());
            return filterCriteria;
        }
    }
}