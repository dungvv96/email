package com.msb.customer.aml.records;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.temenos.api.exceptions.T24CoreException;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.tables.ebmsbhinterfaceparameter.EbMsbhInterfaceParameterRecord;

public class MsbfCusAMLUtil {

	private static final Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	public static JSONObject buildAuthenInfo(EbMsbhInterfaceParameterRecord paramRec) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("req_id", generateEsbRequestId());
		jsonObject.put("req_time", generateEsbRequestTime());
		jsonObject.put("srv", paramRec.getSrv().getValue());
		jsonObject.put("req_app", paramRec.getReqApp().getValue());
		jsonObject.put("authorizer", paramRec.getUserId().getValue());
		jsonObject.put("password", paramRec.getPassword().getValue());
		logger.info("AMLUtil jsonObject: " + jsonObject);
		return jsonObject;
	}

	public static String post(String endpoint, String jsondata, String time, String keyAPI) {
		String responseOutput = "";
		int timeout = 90000;
		try {
			if (StringUtils.isNotEmpty(time)) {
				timeout = Integer.parseInt(time);
			}
		} catch (Exception ex) {
			logger.error("AMLUtil error set timeout");
		}
		try {
			URL url = new URL(endpoint);
			String readLine = null;
			logger.info("AMLUtil start call ESB url: {} , body: {}", url, jsondata);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Msb-Api-Key", keyAPI);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			OutputStream os = conn.getOutputStream();
			byte[] input = jsondata.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
			int statusCode = conn.getResponseCode();
			if (statusCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer response = new StringBuffer();
				while ((readLine = in.readLine()) != null)
					response.append(readLine);
				in.close();
				responseOutput = response.toString();
			} else {
				logger.info("AMLUtil error call ESB statusCode: {}", statusCode);
				throw new T24CoreException("AMLUtil call ESB error status: " + statusCode);
			}
			logger.info("AMLUtil POST Response Success: {}", responseOutput);
		} catch (Exception exception) {
			logger.error("AMLUtil error call ESB: {}", exception.getMessage());
			throw new T24CoreException("Error call ESB");
		}
		logger.info("AMLUtil responseOutput: " + responseOutput);
		return responseOutput;
	}

	public static String generateEsbRequestId() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
		return sdf.format(now) + getProcessIdSuffix();
	}

	public static String generateEsbRequestTime() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyyHH:mm:ss.SSS");
		return sdf.format(now);
	}

	public static String getProcessIdSuffix() {
		String processId = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		int startIndex = Math.max(0, processId.length() - 3);
		return processId.substring(startIndex);
	}
}
