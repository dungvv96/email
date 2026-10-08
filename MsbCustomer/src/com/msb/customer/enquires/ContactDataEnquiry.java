package com.msb.customer.enquires;

import java.util.List;
import java.util.stream.Collectors;

import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.customer.ContactTypeClass;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.tafj.api.client.jVarClient;

public class ContactDataEnquiry extends Enquiry {

	@Override
	public String setValue(String value, String currentId, TStructure currentRecord,
			List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
		String result = "";
		T24RecordUtils t24RecordUtils = new T24RecordUtils(this);
		CustomerRecord customerRec = t24RecordUtils.getRecord("", "CUSTOMER", "", currentId, CustomerRecord.class);
		if (customerRec != null && !customerRec.getContactType().isEmpty()) {
			String phone = customerRec.getContactType().stream()
					.filter(item -> !item.getContactType().getValue().equals("EMAIL"))
					.map(ContactTypeClass::getContactData).map(TField::getValue).collect(Collectors.joining(jVarClient.FM));
			String email = customerRec.getContactType().stream()
					.filter(item -> item.getContactType().getValue().equals("EMAIL"))
					.map(ContactTypeClass::getContactData).map(TField::getValue).collect(Collectors.joining(jVarClient.FM));
			result = phone + "!@" + email;
		}
		return result;
	}
}
