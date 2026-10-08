package com.msb.customer.aml.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.msb.customer.aml.api.OnboardingResponse.AmlStatus;
import com.msb.customer.aml.api.OnboardingResponse.ErrorDetail;

/**
 * POJO representing the response of the TableToJson "search_status" service.
 * Reuses {@link AmlStatus} and {@link ErrorDetail} from {@link OnboardingResponse}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmlQueryResponse {

	@JsonProperty("RequestId")
	private String requestId;

	@JsonProperty("ApplicationId")
	private String applicationId;

	@JsonProperty("CaseId")
	private String caseId;

	@JsonProperty("PrimaryApplicantId")
	private String primaryApplicantId;

	@JsonProperty("PrimaryApplicantName")
	private String primaryApplicantName;

	@JsonProperty("AMLCaseUrl")
	private String amlCaseUrl;

	@JsonProperty("DecisionToOnboard")
	private String decisionToOnboard;

	@JsonProperty("CaseStatus")
	private String caseStatus;

	@JsonProperty("AMLStatus")
	private AmlStatus amlStatus;

	@JsonProperty("CaseInfo")
	private CaseInfo caseInfo;

	@JsonProperty("ErrorDetails")
	private List<ErrorDetail> errorDetails;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getPrimaryApplicantId() {
		return primaryApplicantId;
	}

	public void setPrimaryApplicantId(String primaryApplicantId) {
		this.primaryApplicantId = primaryApplicantId;
	}

	public String getPrimaryApplicantName() {
		return primaryApplicantName;
	}

	public void setPrimaryApplicantName(String primaryApplicantName) {
		this.primaryApplicantName = primaryApplicantName;
	}

	public String getAmlCaseUrl() {
		return amlCaseUrl;
	}

	public void setAmlCaseUrl(String amlCaseUrl) {
		this.amlCaseUrl = amlCaseUrl;
	}

	public String getDecisionToOnboard() {
		return decisionToOnboard;
	}

	public void setDecisionToOnboard(String decisionToOnboard) {
		this.decisionToOnboard = decisionToOnboard;
	}

	public String getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus;
	}

	public AmlStatus getAmlStatus() {
		return amlStatus;
	}

	public void setAmlStatus(AmlStatus amlStatus) {
		this.amlStatus = amlStatus;
	}

	public CaseInfo getCaseInfo() {
		return caseInfo;
	}

	public void setCaseInfo(CaseInfo caseInfo) {
		this.caseInfo = caseInfo;
	}

	public List<ErrorDetail> getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(List<ErrorDetail> errorDetails) {
		this.errorDetails = errorDetails;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CaseInfo {

		@JsonProperty("ProcessDate")
		private String processDate;

		@JsonProperty("UserMaker")
		private String userMaker;

		@JsonProperty("UserChecker")
		private String userChecker;

		@JsonProperty("UserCheckerN1")
		private String userCheckerN1;

		public String getProcessDate() {
			return processDate;
		}

		public void setProcessDate(String processDate) {
			this.processDate = processDate;
		}

		public String getUserMaker() {
			return userMaker;
		}

		public void setUserMaker(String userMaker) {
			this.userMaker = userMaker;
		}

		public String getUserChecker() {
			return userChecker;
		}

		public void setUserChecker(String userChecker) {
			this.userChecker = userChecker;
		}

		public String getUserCheckerN1() {
			return userCheckerN1;
		}

		public void setUserCheckerN1(String userCheckerN1) {
			this.userCheckerN1 = userCheckerN1;
		}
	}
}
