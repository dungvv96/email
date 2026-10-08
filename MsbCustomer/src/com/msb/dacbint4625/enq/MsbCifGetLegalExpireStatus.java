package com.msb.dacbint4625.enq;

import java.util.List;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.override.OverrideRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.msbf.common.utilities.MsbCustomerUtils;
import com.temenos.api.TStructure;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;

/**
 * TODO: Document me!
 * 
 * @author duyvv3 RTN lấy thông tin trạng thái KH:
 */
public class MsbCifGetLegalExpireStatus extends Enquiry {
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    DataAccess da = new DataAccess(this);

    @Override
    public String setValue(String value, String currentId, TStructure currentRecord,
            List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        String customerId = value;       
        Session session = new Session(this);
        
        try{           
            // Check thời hạn giấy tờ tùy thân của người đại diện pháp luật và KTT:
            MsbCustomerUtils customerUtil = new MsbCustomerUtils(this);
            String today = session.getCurrentVariable("!TODAY");
            String overrideIdGTTT = customerUtil.getDocumentExpStatusByCif(customerId, today);
            String detail = "GTTT cua KH hop le";
            
            if(!overrideIdGTTT.equals("")){
                OverrideRecord overrideRec = new OverrideRecord(da.getRecord("OVERRIDE", overrideIdGTTT));
                detail = overrideRec.getMessage(0).getMessage(0).getValue();
            }
            
            return detail;
        }catch(Exception ex){
            logger.error("" + ex.getMessage());
        }
        return "";
    }
    
}
