package com.msb.customer.aml.api;

import java.util.Arrays;
import java.util.Collections;

import com.msb.customer.aml.api.OnboardingRequest.Address;
import com.msb.customer.aml.api.OnboardingRequest.AnticipatoryProfile;
import com.msb.customer.aml.api.OnboardingRequest.CountryRel;
import com.msb.customer.aml.api.OnboardingRequest.Extend;
import com.msb.customer.aml.api.OnboardingRequest.Identification;
import com.msb.customer.aml.api.OnboardingRequest.OnboardingCustomer;
import com.msb.customer.aml.api.OnboardingRequest.Phone;
import com.msb.customer.aml.api.OnboardingRequest.Product;
import com.msb.customer.aml.api.OnboardingRequest.Relationship;

/**
 * Example showing how to build the request object and call the API. Mirrors the
 * payload from the original curl command.
 */
public class OnboardingApiExample {

    public static void main(String[] args) throws Exception {
        OnboardingRequest request = buildRequest();

        OnboardingApiClient client = new OnboardingApiClient(
                "https://internal-api-uat.msb.com.vn/aml/v1/initiateonboardingservice/ob/initiate")
                        .setBasicAuth("a3ljYWRtaW46YWJjMTIz").setApiKey("cvXxmsXFn22aVjluQiREKDFjTXjTmKri")
                        .setCookie("243057b72ff37beb39c891324d32abce=544d46f9fa4bd125b4e85089f8e4033a");

        try {

//            AmlQueryResponse queryResponse = client.amlQuery(
//                    "https://internal-api-uat.msb.com.vn/aml/v1/tabletojsonservice/tabletojson/createtabletojson",
//                    "SEARCH_STATUS", "2567", "");
//            System.out.println("getAmlCaseUrl        : " + queryResponse.getAmlCaseUrl());
//            System.out.println("CustomerScreening        : " + queryResponse.getAmlStatus().getCustomerScreening());
//            System.out.println("getModelBaseRisk        : " + queryResponse.getAmlStatus().getModelBaseRisk());
//            System.out.println("getProcessDate        : " + queryResponse.getCaseInfo().getProcessDate());

            OnboardingResponse response = client.initiateOnboardingTyped(request);
            System.out.println("AmlCaseUrl              : " + response.getAmlCaseUrl());
            System.out.println("Datetime        : " + response.getDatetime());
            System.out.println("RequestId        : " + response.getRequestId());
            System.out.println("CaseId           : " + response.getCaseId());
            System.out.println("RecommentToOnboard: " + response.getRecommentToOnboard());
            System.out.println("CIF              : " + response.getCif());
            if (response.getAmlStatus() != null) {
                System.out.println("CustomerScreening: " + response.getAmlStatus().getCustomerScreening());
                System.out.println("ModelBaseRisk    : " + response.getAmlStatus().getModelBaseRisk());
            }
            System.out.println("AMLCaseUrl       : " + response.getAmlCaseUrl());
        } catch (Exception e) {
            System.err.println("Error calling Onboarding API: " + e.getMessage());
        }

    }

    private static OnboardingRequest buildRequest() {
        OnboardingRequest request = new OnboardingRequest();
        request.setApplicationId("T24");
        request.setRequestUserId("DANGPH2");
        request.setSyncAPIFlag("Y");
        //request.setIsCallBack("N");

        OnboardingCustomer customer = new OnboardingCustomer();
        customer.setBranchCd("VN-001-1200");
        customer.setCustomerType("ORG");
        customer.setOrganizationName("DR LUBANA MUSHAWEH");
        customer.setDisplayName("NGAN HANG TMCP AN BINH");
        customer.setBusinessDomain("a");
        customer.setJurisdiction("All");
        customer.setOccupation("GIAO VIEN");
        customer.setIndustry("AGC");
        customer.setApplicantID("148562843");
        customer.setDateOfIncorporation("04-06-1990");
        customer.setPrimaryCitizenship("VN");
        

        customer.setOnboardingCustomerPhone(Collections.singletonList(new Phone("C", "+0971374004")));
        customer.setOnboardingCustomerProduct(Collections.singletonList(new Product("1")));
        Extend extend1 = new Extend();
        extend1.setBusinessJourneyType("ONBOARDING");
        customer.setOnboardingCustomerExtend(Collections.singletonList(extend1));
        
        customer.setOnboardingCustomerIdentification(
                Collections.singletonList(new Identification("BL", "08456513584", "10-20-1982", "AML-USER")));
        customer.setOnboardingCustomerAddress(
                Arrays.asList(new Address("L", "US", null, "Times City"), new Address("B", "", "", "Royal City")));
        customer.setOnboardingCustomerCountry(Collections.singletonList(new CountryRel("O", "VN")));
        customer.setOnboardingCustomerAnticipatoryProfile(
                Collections.singletonList(new AnticipatoryProfile("KYC", "D")));

        // Related customer (nested)
        OnboardingCustomer related = new OnboardingCustomer();
        related.setCustomerType("IND");
        related.setDisplayName("VO XUAN CUONG");
        related.setDateOfBirth("04-30-1970");
        related.setPrimaryCitizenship("VN");
        related.setBusinessDomain("a");
        related.setJurisdiction("All");
        related.setApplicantID("148562843213213");
        related.setAlias("TEST_API");
        related.setIndustry("VNG");
        related.setOrganizationName("VNG");
        related.setSecondaryCitizenship("VNI");
        related.setWebsiteURL("https://10.14.107.151:4000/");
        related.setExistingCustomerInternalId("31212323113");
        related.setOnboardingCustomerPhone(Collections.singletonList(new Phone("C", "+1323-221-1423")));
        related.setOnboardingCustomerIdentification(
                Collections.singletonList(new Identification("CID", "093823424", "10-20-1992", "AML-USER")));
        related.setOnboardingCustomerRelationship(Collections.singletonList(new Relationship("SIG")));

        Extend extend = new Extend();
        extend.setBusinessJourneyType("ALL");
        extend.setEnglishName("DAVID");
        extend.setThirdCitizenship("US");
        extend.setFourthCitizenship("JP");
        extend.setFifthCitizenship("IR");
        related.setOnboardingCustomerExtend(Collections.singletonList(extend));

        related.setOnboardingCustomerAddress(Arrays.asList(new Address("A", null, null, "34 Lo Duc"),
                new Address("H", "", null, "23 XinZhongGuong")));

        customer.setOnboardingCustomerRelatedCustomer(Collections.singletonList(related));

        request.setOnboardingCustomer(customer);
        return request;
    }
}
