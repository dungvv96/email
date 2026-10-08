package com.msb.customer.aml.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing the request body of the InitiateOnboardingService API.
 * Field names are mapped to the exact JSON keys via {@link JsonProperty}.
 * Null fields are not serialized.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnboardingRequest {

	@JsonProperty("requestUserId")
	private String requestUserId;

	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("SyncAPIFlag")
	private String syncAPIFlag;

	@JsonProperty("IsCallBack")
	private String isCallBack;

	@JsonProperty("OnboardingCustomer")
	private OnboardingCustomer onboardingCustomer;

	public String getRequestUserId() {
		return requestUserId;
	}

	public void setRequestUserId(String requestUserId) {
		this.requestUserId = requestUserId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getSyncAPIFlag() {
		return syncAPIFlag;
	}

	public void setSyncAPIFlag(String syncAPIFlag) {
		this.syncAPIFlag = syncAPIFlag;
	}

	public String getIsCallBack() {
		return isCallBack;
	}

	public void setIsCallBack(String isCallBack) {
		this.isCallBack = isCallBack;
	}

	public OnboardingCustomer getOnboardingCustomer() {
		return onboardingCustomer;
	}

	public void setOnboardingCustomer(OnboardingCustomer onboardingCustomer) {
		this.onboardingCustomer = onboardingCustomer;
	}

	// ----------------------------------------------------------------------
	// Nested types
	// ----------------------------------------------------------------------

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class OnboardingCustomer {

		@JsonProperty("BranchCd")
		private String branchCd;

		@JsonProperty("CustomerType")
		private String customerType;

		@JsonProperty("OrganizationName")
		private String organizationName;

		@JsonProperty("DisplayName")
		private String displayName;

		@JsonProperty("BusinessDomain")
		private String businessDomain;

		@JsonProperty("Jurisdiction")
		private String jurisdiction;
		
		@JsonProperty("Occupation")
        private String occupation;

		@JsonProperty("Industry")
		private String industry;

		@JsonProperty("ApplicantID")
		private String applicantID;

		@JsonProperty("DateOfIncorporation")
		private String dateOfIncorporation;

		@JsonProperty("PrimaryCitizenship")
		private String primaryCitizenship;

		@JsonProperty("DateOfBirth")
		private String dateOfBirth;

		@JsonProperty("Alias")
		private String alias;

		@JsonProperty("SecondaryCitizenship")
		private String secondaryCitizenship;

		@JsonProperty("WebsiteURL")
		private String websiteURL;

		@JsonProperty("ExistingCustomerInternalId")
		private String existingCustomerInternalId;

		@JsonProperty("OnboardingCustomerPhone")
		private List<Phone> onboardingCustomerPhone;
		
		@JsonProperty("OnboardingCustomerProduct")
        private List<Product> onboardingCustomerProduct;

		@JsonProperty("OnboardingCustomerIdentification")
		private List<Identification> onboardingCustomerIdentification;

		@JsonProperty("OnboardingCustomerAddress")
		private List<Address> onboardingCustomerAddress;

		@JsonProperty("OnboardingCustomerCountry")
		private List<CountryRel> onboardingCustomerCountry;

		@JsonProperty("OnboardingCustomerAnticipatoryProfile")
		private List<AnticipatoryProfile> onboardingCustomerAnticipatoryProfile;

		@JsonProperty("OnboardingCustomerRelationship")
		private List<Relationship> onboardingCustomerRelationship;

		@JsonProperty("OnboardingCustomerExtend")
		private List<Extend> onboardingCustomerExtend;

		@JsonProperty("OnboardingCustomerRelatedCustomer")
		private List<OnboardingCustomer> onboardingCustomerRelatedCustomer;

		public String getBranchCd() {
			return branchCd;
		}

		public void setBranchCd(String branchCd) {
			this.branchCd = branchCd;
		}

		public String getCustomerType() {
			return customerType;
		}

		public void setCustomerType(String customerType) {
			this.customerType = customerType;
		}

		public String getOrganizationName() {
			return organizationName;
		}

		public void setOrganizationName(String organizationName) {
			this.organizationName = organizationName;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getBusinessDomain() {
			return businessDomain;
		}

		public void setBusinessDomain(String businessDomain) {
			this.businessDomain = businessDomain;
		}

		public String getJurisdiction() {
			return jurisdiction;
		}
		
		public void setJurisdiction(String jurisdiction) {
			this.jurisdiction = jurisdiction;
		}
		
		public String getOccupation() {
            return occupation;
        }

		public void setOccupation(String occupation) {
            this.occupation = occupation;
        }

		public String getIndustry() {
			return industry;
		}

		public void setIndustry(String industry) {
			this.industry = industry;
		}

		public String getApplicantID() {
			return applicantID;
		}

		public void setApplicantID(String applicantID) {
			this.applicantID = applicantID;
		}

		public String getDateOfIncorporation() {
			return dateOfIncorporation;
		}

		public void setDateOfIncorporation(String dateOfIncorporation) {
			this.dateOfIncorporation = dateOfIncorporation;
		}

		public String getPrimaryCitizenship() {
			return primaryCitizenship;
		}

		public void setPrimaryCitizenship(String primaryCitizenship) {
			this.primaryCitizenship = primaryCitizenship;
		}

		public String getDateOfBirth() {
			return dateOfBirth;
		}

		public void setDateOfBirth(String dateOfBirth) {
			this.dateOfBirth = dateOfBirth;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getSecondaryCitizenship() {
			return secondaryCitizenship;
		}

		public void setSecondaryCitizenship(String secondaryCitizenship) {
			this.secondaryCitizenship = secondaryCitizenship;
		}

		public String getWebsiteURL() {
			return websiteURL;
		}

		public void setWebsiteURL(String websiteURL) {
			this.websiteURL = websiteURL;
		}

		public String getExistingCustomerInternalId() {
			return existingCustomerInternalId;
		}

		public void setExistingCustomerInternalId(String existingCustomerInternalId) {
			this.existingCustomerInternalId = existingCustomerInternalId;
		}

		public List<Phone> getOnboardingCustomerPhone() {
			return onboardingCustomerPhone;
		}

		public void setOnboardingCustomerPhone(List<Phone> onboardingCustomerPhone) {
			this.onboardingCustomerPhone = onboardingCustomerPhone;
		}

		public List<Product> getOnboardingCustomerProduct() {
            return onboardingCustomerProduct;
        }

        public void setOnboardingCustomerProduct(List<Product> onboardingCustomerProduct) {
            this.onboardingCustomerProduct = onboardingCustomerProduct;
        }
        
		public List<Identification> getOnboardingCustomerIdentification() {
			return onboardingCustomerIdentification;
		}

		public void setOnboardingCustomerIdentification(List<Identification> onboardingCustomerIdentification) {
			this.onboardingCustomerIdentification = onboardingCustomerIdentification;
		}

		public List<Address> getOnboardingCustomerAddress() {
			return onboardingCustomerAddress;
		}

		public void setOnboardingCustomerAddress(List<Address> onboardingCustomerAddress) {
			this.onboardingCustomerAddress = onboardingCustomerAddress;
		}

		public List<CountryRel> getOnboardingCustomerCountry() {
			return onboardingCustomerCountry;
		}

		public void setOnboardingCustomerCountry(List<CountryRel> onboardingCustomerCountry) {
			this.onboardingCustomerCountry = onboardingCustomerCountry;
		}

		public List<AnticipatoryProfile> getOnboardingCustomerAnticipatoryProfile() {
			return onboardingCustomerAnticipatoryProfile;
		}

		public void setOnboardingCustomerAnticipatoryProfile(
				List<AnticipatoryProfile> onboardingCustomerAnticipatoryProfile) {
			this.onboardingCustomerAnticipatoryProfile = onboardingCustomerAnticipatoryProfile;
		}

		public List<Relationship> getOnboardingCustomerRelationship() {
			return onboardingCustomerRelationship;
		}

		public void setOnboardingCustomerRelationship(List<Relationship> onboardingCustomerRelationship) {
			this.onboardingCustomerRelationship = onboardingCustomerRelationship;
		}

		public List<Extend> getOnboardingCustomerExtend() {
			return onboardingCustomerExtend;
		}

		public void setOnboardingCustomerExtend(List<Extend> onboardingCustomerExtend) {
			this.onboardingCustomerExtend = onboardingCustomerExtend;
		}

		public List<OnboardingCustomer> getOnboardingCustomerRelatedCustomer() {
			return onboardingCustomerRelatedCustomer;
		}

		public void setOnboardingCustomerRelatedCustomer(List<OnboardingCustomer> onboardingCustomerRelatedCustomer) {
			this.onboardingCustomerRelatedCustomer = onboardingCustomerRelatedCustomer;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Phone {

		@JsonProperty("PhonePurpose")
		private String phonePurpose;

		@JsonProperty("PhoneNumber")
		private String phoneNumber;

		public Phone() {
		}

		public Phone(String phonePurpose, String phoneNumber) {
			this.phonePurpose = phonePurpose;
			this.phoneNumber = phoneNumber;
		}

		public String getPhonePurpose() {
			return phonePurpose;
		}

		public void setPhonePurpose(String phonePurpose) {
			this.phonePurpose = phonePurpose;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}
	}
	
	   @JsonInclude(JsonInclude.Include.NON_NULL)
	    public static class Product {

	        @JsonProperty("ProductOffered")
	        private String productOffered;

	        public Product() {
	        }

	        public Product(String productOffered) {
	            this.productOffered = productOffered;
	        }

	        public String getProductOffered() {
	            return productOffered;
	        }

	        public void setProductOffered(String productOffered) {
	            this.productOffered = productOffered;
	        }
	    }

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Identification {

		@JsonProperty("DocumentType")
		private String documentType;

		@JsonProperty("DocumentNumber")
		private String documentNumber;

		@JsonProperty("IssuingDate")
		private String issuingDate;

		@JsonProperty("IssuingAuthority")
		private String issuingAuthority;

		public Identification() {
		}

		public Identification(String documentType, String documentNumber, String issuingDate, String issuingAuthority) {
			this.documentType = documentType;
			this.documentNumber = documentNumber;
			this.issuingDate = issuingDate;
			this.issuingAuthority = issuingAuthority;
		}

		public String getDocumentType() {
			return documentType;
		}

		public void setDocumentType(String documentType) {
			this.documentType = documentType;
		}

		public String getDocumentNumber() {
			return documentNumber;
		}

		public void setDocumentNumber(String documentNumber) {
			this.documentNumber = documentNumber;
		}

		public String getIssuingDate() {
			return issuingDate;
		}

		public void setIssuingDate(String issuingDate) {
			this.issuingDate = issuingDate;
		}

		public String getIssuingAuthority() {
			return issuingAuthority;
		}

		public void setIssuingAuthority(String issuingAuthority) {
			this.issuingAuthority = issuingAuthority;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Address {

		@JsonProperty("AddressPurpose")
		private String addressPurpose;

		@JsonProperty("Country")
		private String country;

		@JsonProperty("City")
		private String city;

		@JsonProperty("StreetLine1")
		private String streetLine1;

		public Address() {
		}

		public Address(String addressPurpose, String country, String city, String streetLine1) {
			this.addressPurpose = addressPurpose;
			this.country = country;
			this.city = city;
			this.streetLine1 = streetLine1;
		}

		public String getAddressPurpose() {
			return addressPurpose;
		}

		public void setAddressPurpose(String addressPurpose) {
			this.addressPurpose = addressPurpose;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getStreetLine1() {
			return streetLine1;
		}

		public void setStreetLine1(String streetLine1) {
			this.streetLine1 = streetLine1;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class CountryRel {

		@JsonProperty("RelationshipType")
		private String relationshipType;

		@JsonProperty("Country")
		private String country;

		public CountryRel() {
		}

		public CountryRel(String relationshipType, String country) {
			this.relationshipType = relationshipType;
			this.country = country;
		}

		public String getRelationshipType() {
			return relationshipType;
		}

		public void setRelationshipType(String relationshipType) {
			this.relationshipType = relationshipType;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class AnticipatoryProfile {

		@JsonProperty("AccountOpeningMethod")
		private String accountOpeningMethod;

		@JsonProperty("DebitOrCreditIdentifier")
		private String debitOrCreditIdentifier;

		public AnticipatoryProfile() {
		}

		public AnticipatoryProfile(String accountOpeningMethod, String debitOrCreditIdentifier) {
			this.accountOpeningMethod = accountOpeningMethod;
			this.debitOrCreditIdentifier = debitOrCreditIdentifier;
		}

		public String getAccountOpeningMethod() {
			return accountOpeningMethod;
		}

		public void setAccountOpeningMethod(String accountOpeningMethod) {
			this.accountOpeningMethod = accountOpeningMethod;
		}

		public String getDebitOrCreditIdentifier() {
			return debitOrCreditIdentifier;
		}

		public void setDebitOrCreditIdentifier(String debitOrCreditIdentifier) {
			this.debitOrCreditIdentifier = debitOrCreditIdentifier;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Relationship {

		@JsonProperty("RelationshipCode")
		private String relationshipCode;

		public Relationship() {
		}

		public Relationship(String relationshipCode) {
			this.relationshipCode = relationshipCode;
		}

		public String getRelationshipCode() {
			return relationshipCode;
		}

		public void setRelationshipCode(String relationshipCode) {
			this.relationshipCode = relationshipCode;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Extend {

		@JsonProperty("BusinessJourneyType")
		private String businessJourneyType;

		@JsonProperty("EnglishName")
		private String englishName;

		@JsonProperty("ThirdCitizenship")
		private String thirdCitizenship;

		@JsonProperty("FourthCitizenship")
		private String fourthCitizenship;

		@JsonProperty("FifthCitizenship")
		private String fifthCitizenship;

		public String getBusinessJourneyType() {
			return businessJourneyType;
		}

		public void setBusinessJourneyType(String businessJourneyType) {
			this.businessJourneyType = businessJourneyType;
		}

		public String getEnglishName() {
			return englishName;
		}

		public void setEnglishName(String englishName) {
			this.englishName = englishName;
		}

		public String getThirdCitizenship() {
			return thirdCitizenship;
		}

		public void setThirdCitizenship(String thirdCitizenship) {
			this.thirdCitizenship = thirdCitizenship;
		}

		public String getFourthCitizenship() {
			return fourthCitizenship;
		}

		public void setFourthCitizenship(String fourthCitizenship) {
			this.fourthCitizenship = fourthCitizenship;
		}

		public String getFifthCitizenship() {
			return fifthCitizenship;
		}

		public void setFifthCitizenship(String fifthCitizenship) {
			this.fifthCitizenship = fifthCitizenship;
		}
	}
}
