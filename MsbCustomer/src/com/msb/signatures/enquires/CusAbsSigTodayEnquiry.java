package com.msb.signatures.enquires;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

public class CusAbsSigTodayEnquiry extends Enquiry {

	private static final Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	@Override
	public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
		List<String> result = new ArrayList<>();
		DataAccess da = new DataAccess(this);
		Session session = new Session(this);
		T24RecordUtils t24RecordUtils = new T24RecordUtils(this);
		String companyId = session.getCompanyId();
		boolean isHO = companyId.equalsIgnoreCase("VN0011000");
		String filter = "";
		String toDay = convertToDate6(session.getCurrentVariable("!TODAY"));
		for (FilterCriteria fc : filterCriteria) {
			if (fc.getFieldname().equals("R.DATA")) {
				continue;
			}
			if (fc.getFieldname().equals("F.CIF.ID")) {
				if (fc.getOperand().equals("1")) {
					filter = " WITH RECID EQ " + fc.getValue();
				} else if (fc.getOperand().equals("2")) {
					String[] value = fc.getValue().split(" ");
					if (value != null && value.length == 2) {
						if (Integer.parseInt(value[1]) - Integer.parseInt(value[0]) > 1000) {
							result.add("ERROR");
							return result;
						}
						filter = " WITH RECID GE " + value[0] + " AND RECID LE " + value[1];
					}
				}
			}
		}
		if (!filter.equals("")) {
			List<String> cusIds = da.selectRecords("", "CUSTOMER", "", filter, null);
			for (String id : cusIds) {
				boolean notExistsSig = false;
				String filterByCif = " WITH IMAGE.TYPE EQ SIGNATURES AND IMAGE.APPLICATION EQ CUSTOMER AND IMAGE.REFERENCE EQ "
						+ id;
				// check signature by customer no
				List<String> imageByCif = da.selectRecords("", "IM.DOCUMENT.IMAGE", "", filterByCif, null);
				if (imageByCif.isEmpty()) {
					List<String> accounts = da.getConcatValues("CUSTOMER.ACCOUNT", id);
					if (!accounts.isEmpty()) {
						String filterByAccount = " WITH IMAGE.TYPE EQ SIGNATURES AND IMAGE.APPLICATION EQ ACCOUNT AND IMAGE.REFERENCE EQ "
								+ String.join(" ", accounts);
						List<String> imageByAccount = da.selectRecords("", "IM.DOCUMENT.IMAGE", "", filterByAccount,
								null);
						if (imageByAccount.isEmpty()) {
							notExistsSig = true;
						}
					} else {
						notExistsSig = true;
					}
					if (notExistsSig) {
						CustomerRecord customer = t24RecordUtils.getRecord("", "CUSTOMER", "", id,
								CustomerRecord.class);
						if (customer != null && (customer.getCompanyBook().getValue().equals(companyId) || isHO)
								&& toDay.equals(customer.getDateTime(0).substring(0, 6))) {
							result.add(id + "#@" + customer.getDateTime(0) + "#@"
									+ customer.getInputter(0).split("\\_")[1]);
						}
					}
				}
			}
		}
		return result;
	}

	private String convertToDate6(String input) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyMMdd");
		try {
			Date date = inputFormat.parse(input);
			return outputFormat.format(date);
		} catch (ParseException e) {
			logger.error("AuthImDocumentImageEnquiry Error format Date: {}", e.getMessage());
		}
		return input;
	}
}
