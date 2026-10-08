package com.msb.signatures.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.msb.customer.model.CusAbsSigResult;
import com.temenos.api.exceptions.T24IOException;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.servicehook.ServiceData;
import com.temenos.t24.api.hook.system.ServiceLifecycle;
import com.temenos.t24.api.system.Session;
import com.temenos.t24.api.tables.stmsblcusabssig.StMsblCusAbsSigRecord;
import com.temenos.t24.api.tables.stmsblcusabssig.StMsblCusAbsSigTable;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;

public class MsblCusAbsSigService extends ServiceLifecycle {

	private static final Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	@Override
	public void initialise(ServiceData serviceData) {
		// not use
	}

	@Override
	public String getTableName(ServiceData serviceData, List<String> controlList) {
		StMsblCusAbsSigTable cusAbsSigTable = new StMsblCusAbsSigTable(this);
		try {
			cusAbsSigTable.clear();
		} catch (T24IOException e) {
			logger.error("CifNoImageService Error clear file: {}", e.getMessage());
		}
		return "F.COMPANY";
	}

	@Override
	public void process(String id, ServiceData serviceData, String controlItem) {
		TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<CusAbsSigResult> cusResult = new ArrayList<>();
		Session session = new Session(this);
		String backDate = getBackDate(session.getCurrentVariable("!TODAY"));
		int currentRetry = 0;
		int maxRetries = 3;
		while (currentRetry < maxRetries) {
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
				preparedStatement.setString(1, id);
				preparedStatement.setString(2, backDate);
				resultSet = preparedStatement.executeQuery();
				logger.info("CifNoImageService end execute query: " + System.currentTimeMillis() + "ms");
				while (resultSet.next()) {
					CusAbsSigResult cus = new CusAbsSigResult();
					cus.setCompanyBook(resultSet.getString("COMPANY_BOOK"));
					cus.setCustomerNo(resultSet.getString("RECID"));
					cus.setDatetime(resultSet.getString("DATE_TIME"));
					cus.setInputter(resultSet.getString("INPUTTER").split("\\_")[1]);
					cusResult.add(cus);
				}
				logger.info("CifNoImageService total query return: " + cusResult.size());
				StMsblCusAbsSigTable cusAbsSigTable = new StMsblCusAbsSigTable(this);
				StMsblCusAbsSigRecord cusSigRec = new StMsblCusAbsSigRecord();
				for (CusAbsSigResult value : cusResult) {
					cusSigRec.addCompositeValue(value.toString());
				}
				try {
					cusAbsSigTable.write(id, cusSigRec);
					logger.info("CifNoImageService write success: " + id);
				} catch (T24IOException e) {
					logger.error("CifNoImageService Error write file: {}", e.getMessage());
				}
				break;
			} catch (Exception e) {
				logger.error("CifNoImageService Error company: {}, message: {}", id,
						Arrays.toString(e.getStackTrace()));
				currentRetry++;
				if (currentRetry < maxRetries) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						logger.error("CifNoImageService Error sleep: {}", e1.getMessage());
					}
				}
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
		}

	}

	private String getBackDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate backDate = LocalDate.parse(date, formatter);
		backDate = backDate.minusMonths(3);
		return backDate.format(formatter);
	}
}
