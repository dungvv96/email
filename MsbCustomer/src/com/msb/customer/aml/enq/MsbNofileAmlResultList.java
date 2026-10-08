package com.msb.customer.aml.enq;

import java.util.ArrayList;
import java.util.List;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;

/**
 * TODO: Document me!
 * 
 * @author duyvv3
 */
public class MsbNofileAmlResultList extends Enquiry {
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        List<String> retIds = new ArrayList<String>();
        retIds.add("HIT");
        retIds.add("NO HIT");
        retIds.add("KNOCKED-OUT");
        return retIds;
    }
}
