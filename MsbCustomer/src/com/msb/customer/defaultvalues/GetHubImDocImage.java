package com.msb.customer.defaultvalues;

import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.imdocumentimage.ImDocumentImageRecord;
import com.temenos.t24.api.system.DataAccess;

/**
 * TODO: Document me!
 *
 * @author hunglm7
 *
 */
public class GetHubImDocImage extends Enquiry {

    @Override
    public String setValue(String value, String currentId, TStructure currentRecord,
            List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        // TODO Auto-generated method stub
        DataAccess da = new DataAccess(this);
        String msbHub = null;
        
        try {
            ImDocumentImageRecord imd = new ImDocumentImageRecord(da.getRecord("IM.DOCUMENT.IMAGE", value));
            msbHub = imd.getLocalRefField("MSB.INPUT.HUB").getValue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
            try {
                ImDocumentImageRecord imd = new ImDocumentImageRecord(da.getRecord("", "IM.DOCUMENT.IMAGE", "$HIS", value));
                msbHub = imd.getLocalRefField("MSB.INPUT.HUB").getValue();
            } catch (Exception e2) {
                // TODO Auto-generated catch block
                // Uncomment and replace with appropriate logger
                // LOGGER.error(exception_var, exception_var);
            }
        }
        return msbHub;
    }

}
