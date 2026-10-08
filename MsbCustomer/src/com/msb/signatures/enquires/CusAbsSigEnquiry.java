package com.msb.signatures.enquires;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.msb.customer.model.CusAbsSig;
import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.api.TField;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.temenos.t24.api.tables.stmsblcusabssig.StMsblCusAbsSigRecord;

public class CusAbsSigEnquiry extends Enquiry {

	private static final String ST_MSBL_CUS_ABS_SIG = "ST.MSBL.CUS.ABS.SIG";
	private static final String DELIMITER = "#@";

	@Override
	public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
		DataAccess da = new DataAccess(this);
		List<String> result = new ArrayList<>();
		Session session = new Session(this);
		T24RecordUtils t24RecordUtils = new T24RecordUtils(this);
		String companyId = session.getCompanyId();
		List<TField> compositeValues = new ArrayList<>();
		// check if is HO query all record
		if (companyId.equalsIgnoreCase("VN0011000")) {
			List<String> cusAbsSigIds = da.selectRecords("", ST_MSBL_CUS_ABS_SIG, "", "", null);
			if (!cusAbsSigIds.isEmpty()) {
				for (String id : cusAbsSigIds) {
					StMsblCusAbsSigRecord cusAbsSigRec = t24RecordUtils.getRecord("", ST_MSBL_CUS_ABS_SIG, "", id,
							StMsblCusAbsSigRecord.class);
					if (cusAbsSigRec != null && !cusAbsSigRec.getCompositeValue().isEmpty()) {
						compositeValues.addAll(cusAbsSigRec.getCompositeValue());
					}
				}
			}
		} else {
			StMsblCusAbsSigRecord cusAbsSigRec = t24RecordUtils.getRecord("", ST_MSBL_CUS_ABS_SIG, "", companyId,
					StMsblCusAbsSigRecord.class);
			if (cusAbsSigRec != null && !cusAbsSigRec.getCompositeValue().isEmpty()) {
				compositeValues.addAll(cusAbsSigRec.getCompositeValue());
			}
		}
		if (!compositeValues.isEmpty()) {
			List<CusAbsSig> listCusAbsSig = compositeValues.stream().map(item -> {
				String[] value = item.getValue().split(DELIMITER);
				return new CusAbsSig(value[0], Long.parseLong(value[1]), value[2]);
			}).sorted(Comparator.comparingLong(CusAbsSig::getDateTime))
					.filter(cas -> filterCusAbsSig(cas, filterCriteria, da)).collect(Collectors.toList());
			for (CusAbsSig cas : listCusAbsSig) {
				result.add(cas.toString());
			}
		}
		return result;
	}

	private boolean filterCusAbsSig(CusAbsSig cas, List<FilterCriteria> filterCriteria, DataAccess da) {
		for (FilterCriteria fc : filterCriteria) {
			switch (fc.getFieldname()) {
			case "R.DATA":
				break;
			case "F.USER.ID":
				if ((fc.getOperand().equals("1") && !cas.getInputter().equals(fc.getValue()))
						|| (fc.getOperand().equals("6")
								&& !cas.getInputter().contains(fc.getValue().replace("...", "")))) {
					return false;
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

//	private boolean notExistsSig(String customerNo, DataAccess da) {
//		boolean notExistsSig = true;
//		String filterByCif = " WITH IMAGE.TYPE EQ SIGNATURES AND IMAGE.APPLICATION EQ CUSTOMER AND IMAGE.REFERENCE EQ "
//				+ customerNo;
//		// check signature by customer no
//		List<String> imageByCif = da.selectRecords("", "IM.DOCUMENT.IMAGE", "", filterByCif, null);
//		if (imageByCif.isEmpty()) {
//			List<String> accounts = da.getConcatValues("CUSTOMER.ACCOUNT", customerNo);
//			if (!accounts.isEmpty()) {
//				String filterByAccount = " WITH IMAGE.TYPE EQ SIGNATURES AND IMAGE.APPLICATION EQ ACCOUNT AND IMAGE.REFERENCE EQ "
//						+ String.join(" ", accounts);
//				List<String> imageByAccount = da.selectRecords("", "IM.DOCUMENT.IMAGE", "", filterByAccount, null);
//				if (!imageByAccount.isEmpty()) {
//					notExistsSig = false;
//				}
//			}
//		} else {
//			notExistsSig = false;
//		}
//		return notExistsSig;
//	}
}
