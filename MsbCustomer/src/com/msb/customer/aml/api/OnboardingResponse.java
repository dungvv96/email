package com.msb.customer.aml.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing the response body of the InitiateOnboardingService API.
 * Unknown fields are ignored so the client stays tolerant to API changes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnboardingResponse {

	@JsonProperty("RequestId")
	private String requestId;

	@JsonProperty("CaseId")
	private String caseId;

	@JsonProperty("ErrorDetails")
	private List<ErrorDetail> errorDetails;

	@JsonProperty("WatchListDetails")
	private List<WatchListDetail> watchListDetails;

	@JsonProperty("RecommentToOnboard")
	private String recommentToOnboard;

	@JsonProperty("PrimaryApplicantId")
	private String primaryApplicantId;

	@JsonProperty("PrimaryApplicantName")
	private String primaryApplicantName;

	@JsonProperty("CIF")
	private String cif;

	@JsonProperty("ApplicationId")
	private String applicationId;
	
	@JsonProperty("Datetime")
    private String datetime;

	@JsonProperty("AMLStatus")
	private AmlStatus amlStatus;

	@JsonProperty("AMLCaseUrl")
	private String amlCaseUrl;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public List<ErrorDetail> getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(List<ErrorDetail> errorDetails) {
		this.errorDetails = errorDetails;
	}

	public List<WatchListDetail> getWatchListDetails() {
		return watchListDetails;
	}

	public void setWatchListDetails(List<WatchListDetail> watchListDetails) {
		this.watchListDetails = watchListDetails;
	}

	public String getRecommentToOnboard() {
		return recommentToOnboard;
	}

	public void setRecommentToOnboard(String recommentToOnboard) {
		this.recommentToOnboard = recommentToOnboard;
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

	public String getCif() {
		return cif;
	}

	public void setCif(String cif) {
		this.cif = cif;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

	public AmlStatus getAmlStatus() {
		return amlStatus;
	}

	public void setAmlStatus(AmlStatus amlStatus) {
		this.amlStatus = amlStatus;
	}

	public String getAmlCaseUrl() {
		return amlCaseUrl;
	}

	public void setAmlCaseUrl(String amlCaseUrl) {
		this.amlCaseUrl = amlCaseUrl;
	}

	// ----------------------------------------------------------------------
	// Nested types
	// ----------------------------------------------------------------------

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorDetail {

		@JsonProperty("ResponseCode")
		private String responseCode;

		@JsonProperty("ResponseDescription")
		private String responseDescription;

		@JsonProperty("ApplicantId")
		private String applicantId;

		public String getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(String responseCode) {
			this.responseCode = responseCode;
		}

		public String getResponseDescription() {
			return responseDescription;
		}

		public void setResponseDescription(String responseDescription) {
			this.responseDescription = responseDescription;
		}

		public String getApplicantId() {
			return applicantId;
		}

		public void setApplicantId(String applicantId) {
			this.applicantId = applicantId;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class WatchListDetail {

		@JsonProperty("ApplicantID")
		private String applicantID;

		@JsonProperty("CSResult")
		private String csResult;

		public String getApplicantID() {
			return applicantID;
		}

		public void setApplicantID(String applicantID) {
			this.applicantID = applicantID;
		}

		public String getCsResult() {
			return csResult;
		}

		public void setCsResult(String csResult) {
			this.csResult = csResult;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AmlStatus {

		@JsonProperty("CustomerScreening")
		private String customerScreening;

		@JsonProperty("ModelBaseRisk")
		private String modelBaseRisk;

		public String getCustomerScreening() {
			return customerScreening;
		}

		public void setCustomerScreening(String customerScreening) {
			this.customerScreening = customerScreening;
		}

		public String getModelBaseRisk() {
			return modelBaseRisk;
		}

		public void setModelBaseRisk(String modelBaseRisk) {
			this.modelBaseRisk = modelBaseRisk;
		}
	}
}
