package com.msb.signatures.enquires;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.msb.customer.model.CusAbsSigResult;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.system.Session;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;

public class CusNotSigEnquiry extends Enquiry {

	private static final Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	@Override
	public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
		TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<CusAbsSigResult> cusResult = new ArrayList<>();
		Session session = new Session(this);
		String company = session.getCompanyId();
		String backDate = getBackDate(session.getCurrentVariable("!TODAY"));
		try {
			connection = tafjRuntime.getConnectionAPIDB();
			String sql = "SELECT DISTINCT c.RECID, c.COMPANY_BOOK , c.DATE_TIME , c.INPUTTER  FROM V_FMSB_CUSTOMER c "
					+ " LEFT JOIN V_FMSB_ACCOUNT a ON c.RECID = a.CUSTOMER_NO WHERE "
					+ " NOT EXISTS (SELECT 1 FROM V_F_IM_DOCUMENT_IMAGE v WHERE V.IMAGE_TYPE = 'SIGNATURES' AND "
					+ " ((V.IMAGE_APPLICATION = 'CUSTOMER' AND V.IMAGE_REFERENCE = c.RECID) "
					+ " OR (v.IMAGE_APPLICATION = 'ACCOUNT' AND v.IMAGE_REFERENCE = a.ACCOUNT_NUMBER))) "
					+ " AND c.COMPANY_BOOK = ? and c.customer_since > ?";
			logger.info("CifNoImageService start execute query: " + System.currentTimeMillis() + "ms");
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, company);
			preparedStatement.setString(2, backDate);
			resultSet = preparedStatement.executeQuery();
			logger.info("CifNoImageService end execute query: " + System.currentTimeMillis() + "ms");
			while (resultSet.next()) {
				CusAbsSigResult cus = new CusAbsSigResult();
				cus.setCompanyBook(resultSet.getString("COMPANY_BOOK"));
				cus.setCustomerNo(resultSet.getString("RECID"));
				cus.setDatetime(resultSet.getString("DATE_TIME"));
				cus.setInputter(resultSet.getString("INPUTTER").split("\\_")[1]);
				if (filterCusNotSig(cus, filterCriteria)) {
					cusResult.add(cus);
				}
			}
			logger.info("CifNoImageService total query return: " + cusResult.size());
		} catch (Exception e) {
			logger.error("CifNoImageService Error company: {}, message: {}", company,
					Arrays.toString(e.getStackTrace()));
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception ex) {
				logger.error("CifNoImageService Error close connection: {}", Arrays.toString(ex.getStackTrace()));
			}
		}
		return cusResult.stream().map(CusAbsSigResult::toString).collect(Collectors.toList());
	}

	private String getBackDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate backDate = LocalDate.parse(date, formatter);
		backDate = backDate.minusMonths(3);
		return backDate.format(formatter);
	}

	private boolean filterCusNotSig(CusAbsSigResult cas, List<FilterCriteria> filterCriteria) {
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
			case "F.CIF.ID":
				if (fc.getOperand().equals("1") && !cas.getCustomerNo().equals(fc.getValue().trim())) {
					return false;
				}
				if (fc.getOperand().equals("2")) {
					String[] value = fc.getValue().split(" ");
					if (value != null && value.length == 2) {
						return Integer.parseInt(value[0].trim()) >= Integer.parseInt(cas.getCustomerNo())
								&& Integer.parseInt(cas.getCustomerNo()) <= Integer.parseInt(value[1].trim());
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
	}
}
