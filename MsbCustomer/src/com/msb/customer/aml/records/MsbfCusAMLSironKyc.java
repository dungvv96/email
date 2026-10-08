package com.msb.customer.aml.records;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.customer.aml.api.AmlQueryResponse;
import com.msb.customer.aml.api.OnboardingApiClient;
import com.msb.customer.aml.api.OnboardingRequest;
import com.msb.customer.aml.api.OnboardingRequest.Address;
import com.msb.customer.aml.api.OnboardingRequest.AnticipatoryProfile;
import com.msb.customer.aml.api.OnboardingRequest.CountryRel;
import com.msb.customer.aml.api.OnboardingRequest.Extend;
import com.msb.customer.aml.api.OnboardingRequest.Identification;
import com.msb.customer.aml.api.OnboardingRequest.OnboardingCustomer;
import com.msb.customer.aml.api.OnboardingRequest.Product;
import com.msb.customer.aml.api.OnboardingResponse;
import com.msbf.common.utilities.MsbUtils;
import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.api.LocalRefGroup;
import com.temenos.api.LocalRefList;
import com.temenos.api.TField;
import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.InputValue;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.eblookup.EbLookupRecord;
import com.temenos.t24.api.records.override.OverrideRecord;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.temenos.t24.api.tables.ebmsbcifamllog.AmlRefClass;
import com.temenos.t24.api.tables.ebmsbcifamllog.EbMsbCifAmlLogRecord;
import com.temenos.t24.api.tables.ebmsbcifamllog.EbMsbCifAmlLogTable;
import com.temenos.t24.api.tables.ebmsbhinterfaceparameter.EbMsbhInterfaceParameterRecord;
import com.temenos.t24.api.tables.msbhparameter.GroupClass;
import com.temenos.t24.api.tables.msbhparameter.MsbhParameterRecord;
import com.temenos.t24.api.tables.msbhparameter.ParamNameClass;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;

public class MsbfCusAMLSironKyc extends RecordLifecycle {

    private static final String MSB_FULL_NAME = "MSB.FULL.NAME";
    private static final String AUTHEN_INFO = "authenInfo";
    private static final String EB_MSBH_INTERFACE_PARAMETER = "EB.MSBH.INTERFACE.PARAMETER";
    private static final String MSB_AUTO_CHECK_AML = "MSB.AUTO.CHECK.AML";
    private static final String MSB_AML_RESULT = "MSB.AML.RESULT";
    private static final String MSB_AML_REFERENCE = "MSB.AML.REFERENCE";

    private MsbhParameterRecord msbhParameterRecord;

    private static final Logger logger = LoggerFactory.getLogger("IRIS_DEV");

    DataAccess da = new DataAccess(this);
    Session session = new Session(this);
    ObjectMapper objectMapper = new ObjectMapper();
    T24RecordUtils utl = new T24RecordUtils(this);
    TAFJRuntime tafj = TAFJRuntimeFactory.getTAFJRuntime(this);

    @Override
    public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        CustomerRecord customerRec = new CustomerRecord(currentRecord);
        logger.info("MsbfCusAMLSironKyc validateRecord START:");

        logger.info("MsbfCusAMLSironKyc validateRecord MSB.PROD.OFFERED:"
                + customerRec.getLocalRefGroups("MSB.PROD.OFFERED").size());
        String overrideUrl = MsbUtils.msbGetParameter("CIF.AML.CFG", "COMMON", "OVERRIDE.AML.URL", da);
        // Kiem tra truong Prod Offer la bat buoc
        if (customerRec.getLocalRefGroups("MSB.PROD.OFFERED").size() == 0
                && !"PROSPECT".equals(customerRec.getCustomerType().getValue())) {
            // customerRec.getLocalRefGroups("MSB.PROD.OFFERED").
            // MsbUtils.setError(tafj, "Product Offered Input Missing");
            setErrOverLocalField(tafj, "INPUT MISSING", "CUSTOMER", "PROD.OFFERED");
            return customerRec.getValidationResponse();
        }
        // Check decision to onboard:
        if (customerRec.getLocalRefField("MSB.DECISION").getValue().equalsIgnoreCase("Y")) {
            return customerRec.getValidationResponse();
        } else if (customerRec.getLocalRefField("MSB.DECISION").getValue().equalsIgnoreCase("N")) {
            // customerRec.getLocalRefField("MSB.DECISION").setOverride("MSB.CIF.AML.DECISION.N");
            setErrOverLocalField(tafj, "MSB.CIF.AML.DECISION.N", "CUSTOMER", "MSB.DECISION");
            return customerRec.getValidationResponse();
        } else if (customerRec.getLocalRefField("MSB.DECISION").getValue().equalsIgnoreCase("P")) {
            // customerRec.getLocalRefField("MSB.DECISION").setOverride("MSB.CIF.AML.DECISION.P");
            String ovrMess = getOverMess("MSB.CIF.AML.DECISION.P");
            if (customerRec.getLocalRefField("MSB.OVERRIDE.REASON") != null) {
                ovrMess = ovrMess + ":" + overrideUrl + "?"
                        + customerRec.getLocalRefField("MSB.OVERRIDE.REASON").getValue();
            }
            setOverLocalField(tafj, ovrMess, "CUSTOMER", "MSB.DECISION");
            return customerRec.getValidationResponse();
        }
        logger.info("MsbfCusAMLSironKyc validateRecord :" + MSB_AML_RESULT);
        // Validate AML RESULT
        if (StringUtils.isEmpty(customerRec.getLocalRefField(MSB_AML_RESULT).getValue())) {
            // customerRec.getLocalRefField(MSB_AML_RESULT).setOverride("MSB.AML.RESULT.MISSING");
            setErrOverLocalField(tafj, "MSB.AML.RESULT.MISSING", "CUSTOMER", MSB_AML_RESULT);
            return customerRec.getValidationResponse();
        }

        if (customerRec.getLocalRefField(MSB_AML_RESULT).getValue().equals("KNOCKED-OUT")) {
            // customerRec.getLocalRefField(MSB_AML_RESULT).setOverride("MSB.AML.RESULT.KNOCK.OUT");
            setErrOverLocalField(tafj, "MSB.AML.RESULT.KNOCK.OUT", "CUSTOMER", MSB_AML_RESULT);
            return customerRec.getValidationResponse();
        }
        logger.info("MsbfCusAMLSironKyc validateRecord  HIT/NO HIT:");

        // Override with result = HIT/NO HIT
        try {
            OverrideRecord oveRec = new OverrideRecord(da.getRecord("OVERRIDE", "MSB.AML.RESULT.HIT"));
            String oveMsg = oveRec.getMessage(0).getMessage(0).getValue();
            String caseurl = customerRec.getLocalRefField("MSB.OVERRIDE.REASON").getValue();
            // if(!"".equals(caseurl)){
            // caseurl = caseurl.replace('*', '/');
            // }
            // oveMsg = oveMsg + "//" + caseurl;

            if (customerRec.getLocalRefField(MSB_AML_RESULT).getValue().equals("HIT")) {
                // customerRec.getLocalRefField(MSB_AML_RESULT).setOverride(overrideUrl
                // + "?" + caseurl);
                logger.info("MsbfCusAMLSironKyc validateRecord HIT oveMsg:" + overrideUrl + "?" + caseurl);
                setOverLocalField(tafj, oveMsg + ":" + overrideUrl + "?" + caseurl, "CUSTOMER", MSB_AML_RESULT);
                return customerRec.getValidationResponse();
                // setErrOverLocalField(tafj, overrideUrl + "?" + oveMsg,
                // "CUSTOMER", MSB_AML_RESULT);
            }

            if (customerRec.getLocalRefField(MSB_AML_RESULT).getValue().equals("NO HIT")) {
                if (customerRec.getLocalRefField("MSB.RR.RESULT").getValue().equals("HIGH")) {
                    // customerRec.getLocalRefField(MSB_AML_RESULT).setOverride(overrideUrl
                    // + "?" + caseurl);
                    logger.info(
                            "MsbfCusAMLSironKyc validateRecord NO HIT + HIGH oveMsg:" + overrideUrl + "?" + caseurl);
                    setOverLocalField(tafj, oveMsg + ":" + overrideUrl + "?" + caseurl, "CUSTOMER", MSB_AML_RESULT);
                    return customerRec.getValidationResponse();
                }
            }
        } catch (Exception ex) {
            logger.error("MsbfCusAMLSironKyc exception:" + ex.getMessage());
            //
            customerRec.getLocalRefField(MSB_AML_RESULT).setError(ex.getMessage());
            setErrOverLocalField(tafj, ex.getMessage(), "CUSTOMER", MSB_AML_RESULT);
        }
        logger.info("MsbfCusAMLSironKyc validateRecord :" + MSB_AUTO_CHECK_AML);
        // Validate AML REFERENCE
        if (!"No".equalsIgnoreCase(customerRec.getLocalRefField(MSB_AUTO_CHECK_AML).getValue())) {
            if (StringUtils.isEmpty(customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue())) {
                // customerRec.getLocalRefField(MSB_AML_REFERENCE).setOverride("MSB.AML.RESULT.MISSING");
                setErrOverLocalField(tafj, "MSB.AML.RESULT.MISSING", "CUSTOMER", MSB_AML_REFERENCE);
                return customerRec.getValidationResponse();
            }
        }
        logger.info("MsbfCusAMLSironKyc validateRecord END:");
        return customerRec.getValidationResponse();
    }

    @Override
    public String checkId(String currentRecordId, TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        switch (transactionContext.getCurrentFunction().toUpperCase().charAt(0)) {
        case 'I':
            try {
                CustomerRecord cusInauRec = new CustomerRecord(da.getRecord("", "CUSTOMER", "$NAU", currentRecordId));
                TField autoAMLField = cusInauRec.getLocalRefField(MSB_AUTO_CHECK_AML);
                TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
                if ("Yes".equalsIgnoreCase(autoAMLField.getValue())) {
                    String[] localFieldNoinput = new String[] { "MSB.AML.REF", MSB_AML_RESULT, "MSB.RR.RESULT",
                            "MSB.OVERRIDE" };
                    MsbUtils.setLocalFieldNoInput(tafjRuntime, true, "CUSTOMER", localFieldNoinput);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // Uncomment and replace with appropriate logger
                // LOGGER.error(exception_var, exception_var);
            }
            break;
        default:
            break;
        }
        return currentRecordId;
    }

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
        // T24RecordUtils t24RecordUtils = new T24RecordUtils(this);
        TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
        CustomerRecord customerRec = new CustomerRecord(currentRecord);
        CustomerRecord livecustomerRec = new CustomerRecord(liveRecord);
        CustomerRecord unaucustomerRec = new CustomerRecord(unauthorisedRecord);
        TField autoAMLField = customerRec.getLocalRefField(MSB_AUTO_CHECK_AML);
        logger.info("MsbfCusAMLSironKyc autoAMLField: " + autoAMLField);
        logger.info("MsbfCusAMLSironKyc customerRec.toString(): " + customerRec.toString());
        logger.info("MsbfCusAMLSironKyc livecustomerRec.toString(): " + livecustomerRec.toString());
        logger.info("MsbfCusAMLSironKyc unaucustomerRec.toString(): " + unaucustomerRec.toString());
        logger.info("MsbfCusAMLSironKyc transactionContext.toString(): " + transactionContext.toString());
        // Không thực hiện khi validate:
        if (transactionContext.getCurrentOperation().equals("VALIDATE")) {
            return;
        }

        if (transactionContext.getCurrentFunction().toUpperCase().charAt(0) == 'A') {
            return;
        }
        // }

        // Kiem tra truong Prod Offer la bat buoc
        if (customerRec.getLocalRefGroups("MSB.PROD.OFFERED").size() == 0
                && !"PROSPECT".equals(customerRec.getCustomerType().getValue())) {
            // customerRec.getLocalRefGroups("MSB.PROD.OFFERED").
            // MsbUtils.setError(tafj, "Product Offered Input Missing");
            setErrOverLocalField(tafj, "INPUT MISSING", "CUSTOMER", "PROD.OFFERED");
            currentRecord.set(customerRec.toStructure());
            return;
            // return customerRec.getValidationResponse();
        }

        int sector = Integer.parseInt(customerRec.getSector().getValue());
        if (sector < 2000) {
            // Check khi nguoi nuoc ngoai bat buoc nhap PP
            boolean flagPp = false;
            boolean flagIdcBc = false;
            String national = customerRec.getNationality().getValue();
            for (int i = 0; i < customerRec.getLegalId().size(); i++) {
                String docName = customerRec.getLegalId(i).getLegalDocName().getValue();
                if (national.equals("VN") && (docName.equals("IDC") || docName.equals("BC"))) {
                    flagIdcBc = true;
                }
                if (docName.equals("PP")) {
                    flagPp = true;
                }
            }

            // Check bat buoc nhap ca PP voi KHCN la nguoi NN:
            if (!national.equals("VN") && !flagPp) {
                MsbUtils.setError(tafj, "Nguoi nuoc ngoai bat buoc nhap thong tin Passport", "CUSTOMER",
                        "LEGAL.DOC.NAME");
                // customerRec.getLegalId(0).getLegalDocName().setError("Nguoi
                // nuoc
                // ngoai bat buoc nhap thong tin Passport");
                currentRecord.set(customerRec.toStructure());
                return;
            }

            // Chan neu loai giay to VN ma khong co CCCD hay GKS
            if (national.equals("VN") && !flagIdcBc && customerRec.getLegalId().size() > 0) {
                MsbUtils.setError(tafj, "GTTT khong con hieu luc den het 31/12/2024", "CUSTOMER", "LEGAL.DOC.NAME");
                // cusRec.getLegalId(0).getLegalDocName().setError("GTTT khong
                // con
                // hieu luc den het 31/12/2024");
            }
        }

        // Kiem tra da KYC trong ngay chUa:
        String idlog = currentRecordId + "-" + session.getCurrentVariable("!TODAY");
        String oldChecksum = "";
        String checksum = getCheckSumCustomerInfo(customerRec);
        logger.info("MsbfCusAMLSironKyc checksum:" + checksum);
        logger.info("MsbfCusAMLSironKyc livecus:");

        // logger.info("MsbfCusAMLSironKyc livecustomerRec.getCurrNo():" +
        // livecustomerRec.getCurrNo());
        // logger.info("MsbfCusAMLSironKyc customerRec.getCurrNo():" +
        // customerRec.getCurrNo());
        if (livecustomerRec != null && livecustomerRec.getLocalRefGroups("MSB.CHK.SUM") != null
                && livecustomerRec.getLocalRefGroups("MSB.CHK.SUM").size() > 0
                && livecustomerRec.getCurrNo().equals(customerRec.getCurrNo())) {
            logger.info("MsbfCusAMLSironKyc livecustomerRec.getLocalRefGroups('MSB.CHK.SUM').size():"
                    + livecustomerRec.getLocalRefGroups("MSB.CHK.SUM").size());
            oldChecksum = getStringVal(livecustomerRec.getLocalRefGroups("MSB.CHK.SUM"), "MSB.CHK.SUM");
        } else {
            // String today = session.getCurrentVariable("!TODAY");
            // today = today.substring(2, 8);
            // String datetime = customerRec.getDateTime(0).substring(0, 6);
            logger.info("MsbfCusAMLSironKyc currentcus:");
            // if (unaucustomerRec.getLocalRefGroups("MSB.CHK.SUM") != null
            // && unaucustomerRec.getLocalRefGroups("MSB.CHK.SUM").size() > 0) {
            // logger.info("MsbfCusAMLSironKyc
            // unaucustomerRec.getLocalRefGroups('MSB.CHK.SUM').size():"
            // + unaucustomerRec.getLocalRefGroups("MSB.CHK.SUM").size());
            // oldChecksum =
            // getStringVal(unaucustomerRec.getLocalRefGroups("MSB.CHK.SUM"),
            // "MSB.CHK.SUM");
            // } else {
            if (customerRec.getLocalRefGroups("MSB.CHK.SUM") != null
                    && customerRec.getLocalRefGroups("MSB.CHK.SUM").size() > 0) {
                logger.info("MsbfCusAMLSironKyc customerRec.getLocalRefGroups('MSB.CHK.SUM').size():"
                        + customerRec.getLocalRefGroups("MSB.CHK.SUM").size());
                oldChecksum = getStringVal(customerRec.getLocalRefGroups("MSB.CHK.SUM"), "MSB.CHK.SUM");
            }
            // }
        }
        logger.info("MsbfCusAMLSironKyc oldChecksum:" + oldChecksum);
        logger.info("MsbfCusAMLSironKyc idlog: " + idlog);
        EbMsbCifAmlLogRecord amlLogRecord;
        boolean flagNewLog = false;
        boolean chkAmlRef = false;
        int amlPosition = -1;
        // Kiểm tra có override không
        // CustomerRecord cusNauRecord;
        // boolean flagOverride = false;
        String amlReference = "", amlResult = "", amlRrResult = "";
        // try {
        // cusNauRecord = new CustomerRecord(da.getRecord("", "CUSTOMER",
        // "$NAU", currentRecordId));
        // flagOverride = checkOverride(cusNauRecord);
        // } catch (Exception ex) {
        // logger.info("MsbfCusAMLSironKyc : CUSTOMER NAU record is not exist");
        // }
        // logger.info("MsbfCusAMLSironKyc flagOverride: " + flagOverride);
        if (!customerRec.getCurrNo().isEmpty() && Integer.parseInt(customerRec.getCurrNo()) >= 1
                && oldChecksum.equals(checksum)
                && (!customerRec.getRecordStatus().isEmpty() && !"INAU".equals(customerRec.getRecordStatus()))) {
            logger.info("MsbfCusAMLSironKyc customerRec.getCurrNo(): " + customerRec.getCurrNo());
            return;
        }

        // Không thực hiện gọi KYC khi user chọn AUTO AML = NO
        if (!"Yes".equalsIgnoreCase(autoAMLField.getValue())) {
            if (!checksum.equals(oldChecksum)) {
                // customerRec.getLocalRefField(MSB_AML_REFERENCE).setValue("");
                // customerRec.getLocalRefField(MSB_AML_RESULT).setValue("");
                // customerRec.getLocalRefField("MSB.RR.RESULT").setValue("");
                // customerRec.getLocalRefField("MSB.DECISION").setValue("");
                customerRec.getLocalRefField("MSB.AML.DATETIME").setValue(session.getCurrentVariable("!TODAY"));

                logger.info("MsbfCusAMLSironKyc write AML AUTO = NO log START");
                boolean existsLog = false;
                try {
                    amlLogRecord = new EbMsbCifAmlLogRecord(da.getRecord("", "EB.MSB.CIF.AML.LOG", "", idlog));
                    existsLog = true;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // Uncomment and replace with appropriate logger
                    // LOGGER.error(exception_var, exception_var);
                    amlLogRecord = new EbMsbCifAmlLogRecord();
                }
                String amlPurpose = "";
                if (customerRec.getCurrNo().compareTo("1") > 0) {
                    amlPurpose = "Amend CIF";
                } else {
                    amlPurpose = "Create CIF";
                }
                if (!existsLog) {
                    AmlRefClass amlRefClass = new AmlRefClass();
                    amlRefClass.setAmlDatetime(customerRec.getLocalRefField("MSB.AML.DATETIME").getValue());
                    amlRefClass.setAmlDecision("");
                    amlRefClass.setAmlPurpose(amlPurpose);
                    amlRefClass.setAmlRef(customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue());
                    amlRefClass.setAmlResult(customerRec.getLocalRefField(MSB_AML_RESULT).getValue());
                    amlRefClass.setAmlRrResult(customerRec.getLocalRefField("MSB.RR.RESULT").getValue());
                    amlRefClass.setApplication("CUSTOMER");
                    amlRefClass.setAppplicationId(currentRecordId);
                    if (customerRec.getLocalRefField("MSB.OVERRIDE.REASON") != null) {
                        amlRefClass.setAmlUrl(customerRec.getLocalRefField("MSB.OVERRIDE.REASON").getValue());
                    }
                    amlRefClass.setChecksum(checksum);
                    amlLogRecord.setAmlRef(amlRefClass, amlLogRecord.getAmlRef().size());
                } else {
                    int foundPosition = -1;
                    for (int i = 0; i < amlLogRecord.getAmlRef().size(); i++) {
                        if ("CUSTOMER".equals(amlLogRecord.getAmlRef(i).getApplication().getValue())
                                && currentRecordId.equals(amlLogRecord.getAmlRef(i).getAppplicationId().getValue())) {
                            foundPosition = i;
                        }
                    }
                    if (foundPosition >= 0) {
                        amlLogRecord.getAmlRef(foundPosition)
                                .setAmlDatetime(customerRec.getLocalRefField("MSB.AML.DATETIME").getValue());
                        amlLogRecord.getAmlRef(foundPosition).setAmlDecision("");
                        amlLogRecord.getAmlRef(foundPosition).setAmlPurpose(amlPurpose);
                        amlLogRecord.getAmlRef(foundPosition)
                                .setAmlRef(customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue());
                        amlLogRecord.getAmlRef(foundPosition)
                                .setAmlResult(customerRec.getLocalRefField(MSB_AML_RESULT).getValue());
                        amlLogRecord.getAmlRef(foundPosition)
                                .setAmlRrResult(customerRec.getLocalRefField("MSB.RR.RESULT").getValue());
                        amlLogRecord.getAmlRef(foundPosition).setApplication("CUSTOMER");
                        amlLogRecord.getAmlRef(foundPosition).setAppplicationId(currentRecordId);
                        if (customerRec.getLocalRefField("MSB.OVERRIDE.REASON") != null) {
                            amlLogRecord.getAmlRef(foundPosition)
                                    .setAmlUrl(customerRec.getLocalRefField("MSB.OVERRIDE.REASON").getValue());
                        }
                        amlLogRecord.getAmlRef(foundPosition).setChecksum(checksum);
                    } else {
                        AmlRefClass amlRefClass = new AmlRefClass();
                        amlRefClass.setAmlDatetime(customerRec.getLocalRefField("MSB.AML.DATETIME").getValue());
                        amlRefClass.setAmlDecision("");
                        amlRefClass.setAmlPurpose(amlPurpose);
                        amlRefClass.setAmlRef(customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue());
                        amlRefClass.setAmlResult(customerRec.getLocalRefField(MSB_AML_RESULT).getValue());
                        amlRefClass.setAmlRrResult(customerRec.getLocalRefField("MSB.RR.RESULT").getValue());
                        amlRefClass.setApplication("CUSTOMER");
                        amlRefClass.setAppplicationId(currentRecordId);
                        if (customerRec.getLocalRefField("MSB.OVERRIDE.REASON") != null) {
                            amlRefClass.setAmlUrl(customerRec.getLocalRefField("MSB.OVERRIDE.REASON").getValue());
                        }
                        amlRefClass.setChecksum(checksum);
                        amlLogRecord.setAmlRef(amlRefClass, amlLogRecord.getAmlRef().size());
                    }
                }
                try {
                    EbMsbCifAmlLogTable amlLogTable = new EbMsbCifAmlLogTable(this);
                    amlLogTable.write(idlog, amlLogRecord);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // Uncomment and replace with appropriate logger
                    // LOGGER.error(exception_var, exception_var);
                    MsbUtils.setErrorLocalField(tafjRuntime, "Write log file error:" + e.getMessage(), "CUSTOMER",
                            "MSB.AUTO.AML");
                }
                logger.info("MsbfCusAMLSironKyc write AML AUTO = NO log END");
                currentRecord.set(customerRec.toStructure());
                return;
            }
            // set input field
            logger.info("MsbfCusAMLSironKyc write AML AUTO = NO set up input field");
            String[] localFieldNoinput = new String[] { "MSB.AML.REF", MSB_AML_RESULT, "MSB.RR.RESULT",
                    "MSB.OVERRIDE" };
            MsbUtils.setLocalFieldNoInput(tafjRuntime, false, "CUSTOMER", localFieldNoinput);
            return;
        }

        // Khởi tạo client để gọi service AML
        logger.info("MsbfCusAMLSironKyc KHOI TAO idlog: " + idlog);
        String url = MsbUtils.msbGetParameter("CIF.AML.CFG", "COMMON", "AML.URL", da);
        String urlQuery = MsbUtils.msbGetParameter("CIF.AML.CFG", "COMMON", "AML.ENQ", da);
        String token = MsbUtils.msbGetParameter("CIF.AML.CFG", "COMMON", "TOKEN", da);
        String apiKey = MsbUtils.msbGetParameter("CIF.AML.CFG", "COMMON", "API.KEY", da);
        String cookie = MsbUtils.msbGetParameter("CIF.AML.CFG", "COMMON", "COOKIE", da);

        OnboardingApiClient client = new OnboardingApiClient(url).setBasicAuth(token).setApiKey(apiKey);
        // .setCookie(cookie);

        // else {
        // if ("Yes".equalsIgnoreCase(autoAMLField.getValue())) {
        String[] localFieldNoinput = new String[] { "MSB.AML.REF", MSB_AML_RESULT, "MSB.RR.RESULT", "MSB.OVERRIDE" };
        MsbUtils.setLocalFieldNoInput(tafjRuntime, true, "CUSTOMER", localFieldNoinput);
        try {
            logger.info("MsbfCusAMLSironKyc defaultFieldValues CHECK LOG: ");
            amlLogRecord = new EbMsbCifAmlLogRecord(da.getRecord("EB.MSB.CIF.AML.LOG", idlog));
            int logsize = amlLogRecord.getAmlRef().size();
            logger.info("MsbfCusAMLSironKyc defaultFieldValues CHECK LOG logsize: " + logsize);
            if (logsize > 0) {
                if (checksum.equals(oldChecksum)) {
                    chkAmlRef = true;
                    for (int i = 0; i < logsize; i++) {
                        if ("CUSTOMER".equals(amlLogRecord.getAmlRef(i).getApplication().getValue())
                                && currentRecordId.equals(amlLogRecord.getAmlRef(i).getAppplicationId().getValue())) {
                            amlPosition = i;
                            if (amlLogRecord.getAmlRef(i).getAmlRef().getValue()
                                    .equals(customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue())) {
                                amlReference = amlLogRecord.getAmlRef(i).getAmlRef().getValue();
                                amlResult = amlLogRecord.getAmlRef(i).getAmlResult().getValue();
                                amlRrResult = amlLogRecord.getAmlRef(i).getAmlRrResult().getValue();
                            }
                            // else {
                            // amlReference =
                            // customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue();
                            // amlResult =
                            // customerRec.getLocalRefField(MSB_AML_RESULT).getValue();
                            // amlRrResult =
                            // customerRec.getLocalRefField("MSB.RR.RESULT").getValue();
                            // logamlReference =
                            // amlLogRecord.getAmlRef(i).getAmlRef().getValue();
                            // }
                            // logger.info("MsbfCusAMLSironKyc
                            // defaultFieldValues logamlReference: " +
                            // logamlReference);
                            break;
                        }
                    }
                    if (amlPosition >= 0 && "".equals(amlReference) && "".equals(amlResult) && "".equals(amlRrResult)) {
                        amlReference = hasFieldValue(customerRec.getLocalRefField(MSB_AML_REFERENCE));
                        amlResult = hasFieldValue(customerRec.getLocalRefField(MSB_AML_RESULT));
                        amlRrResult = hasFieldValue(customerRec.getLocalRefField("MSB.RR.RESULT"));
                    }
                    logger.info("MsbfCusAMLSironKyc defaultFieldValues amlReference: " + amlReference);
                    logger.info("MsbfCusAMLSironKyc defaultFieldValues amlResult: " + amlResult);
                    logger.info("MsbfCusAMLSironKyc defaultFieldValues amlRrResult: " + amlRrResult);
                }
            }

            if (chkAmlRef) {
                logger.info("MsbfCusAMLSironKyc QUERY START amlPosition: " + amlPosition);
                if (amlPosition >= 0) {
                    // QUERY và default dữ liệu lên bản ghi::
                    // Truong hop goi query khi da call AML

                    logger.info("MsbfCusAMLSironKyc checksum: " + checksum);
                    logger.info("MsbfCusAMLSironKyc oldChecksum: " + oldChecksum);
                    // if (checksum.equals(oldChecksum)) {
                    // return;
                    // }
                    if (("HIT".equals(amlResult) || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))
                            || "KNOCKED-OUT".equals(amlResult))) {
                        logger.info("MsbfCusAMLSironKyc QUERY START: ");
                        try {
                            AmlQueryResponse queryResponse = client.amlQuery(urlQuery, "SEARCH_STATUS", amlReference,
                                    "");

                            String responseCode = queryResponse.getErrorDetails().get(0).getResponseCode();
                            String responseDescription = queryResponse.getErrorDetails().get(0).getResponseCode();
                            logger.info("MsbfCusAMLSironKyc QUERY ResponseCode: " + responseCode);
                            logger.info("MsbfCusAMLSironKyc QUERY ResponseDescription: " + responseDescription);

                            if (!responseCode.equals("200")) {
                                MsbUtils.setErrorLocalField(tafjRuntime,
                                        "Error code = " + responseCode + "/" + responseDescription, "CUSTOMER",
                                        "MSB.AUTO.AML");
                                return;
                            }

                            if (queryResponse.getAmlStatus() != null) {
                                amlResult = queryResponse.getAmlStatus().getCustomerScreening();

                                // format date:
                                // String processDate =
                                // queryResponse.getCaseInfo().getProcessDate();
                                // DateTimeFormatter inputFormatter =
                                // DateTimeFormatter.ofPattern("yyyy-MM-dd
                                // hh:mm:ss");
                                // DateTimeFormatter outputFormatter =
                                // DateTimeFormatter.ofPattern("yyyyMMdd");
                                // LocalDate date =
                                // LocalDate.parse(processDate,
                                // inputFormatter);
                                // processDate =
                                // date.format(outputFormatter);

                                customerRec.getLocalRefField(MSB_AML_RESULT).setValue(amlResult);
                                customerRec.getLocalRefField("MSB.AML.DATETIME")
                                        .setValue(session.getCurrentVariable("!TODAY"));

                                logger.info("MsbfCusAMLSironKyc QUERY amlResult: " + amlResult);
                                String amlCaseUrl = queryResponse.getAmlCaseUrl();
                                String amlDecision = queryResponse.getDecisionToOnboard();
                                if ("HIT".equals(amlResult)
                                        || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))
                                        || ("NO HIT".equals(amlResult) && "P".equals(amlDecision))) {
                                    if (amlCaseUrl != null) {

                                        customerRec.getLocalRefField("MSB.OVERRIDE.REASON")
                                                .setValue(amlCaseUrl.split("\\?")[1]);
                                    }
                                } else {
                                    customerRec.getLocalRefField("MSB.OVERRIDE.REASON").setValue("");
                                }

                                // // Xac dinh muc dich goi AML:
                                // String amlPurpose = "";
                                // if
                                // (customerRec.getCurrNo().compareTo("1") >
                                // 0) {
                                // amlPurpose = "Amend CIF";
                                // } else {
                                // amlPurpose = "Create CIF";
                                // }

                                // update AML log table:
                                logger.info("MsbfCusAMLSironKyc QUERY write log START: idlog = " + idlog);
                                amlResult = queryResponse.getAmlStatus().getCustomerScreening();
                                amlRrResult = queryResponse.getAmlStatus().getModelBaseRisk();
                                customerRec.getLocalRefField("MSB.DECISION").setValue(amlDecision);
                                logger.info("MsbfAaaCIFAMLCasa defaultFieldValues QUERY amlReference: " + amlReference);
                                logger.info("MsbfAaaCIFAMLCasa defaultFieldValues QUERY amlResult: " + amlResult);

                                // update log
                                amlLogRecord.getAmlRef(amlPosition).setAmlResult(amlResult);
                                amlLogRecord.getAmlRef(amlPosition).setAmlRrResult(amlRrResult);
                                amlLogRecord.getAmlRef(amlPosition).setAmlDecision(amlDecision);
                                amlLogRecord.getAmlRef(amlPosition).setChecksum(checksum);

                                EbMsbCifAmlLogTable amlLogTable = new EbMsbCifAmlLogTable(this);
                                amlLogTable.write(idlog, amlLogRecord);
                                logger.info("MsbfCusAMLSironKyc write log END");

                                // // update fatca
                                // updateFatca(customerRec, t24RecordUtils);

                                // set noinput field
                                // localFieldNoinput = new String[] {
                                // "MSB.AML.REF", MSB_AML_RESULT,
                                // "MSB.RR.RESULT" };
                                MsbUtils.setLocalFieldNoInput(tafjRuntime, true, "CUSTOMER", localFieldNoinput);
                            } else {
                                MsbUtils.setErrorLocalField(tafjRuntime,
                                        "Call service AML error: " + responseDescription, "CUSTOMER", "MSB.AUTO.AML");
                            }

                        } catch (Exception e) {
                            logger.info("MsbfCusAMLSironKyc QUERY Error calling Onboarding API: {}" + e);
                            MsbUtils.setErrorLocalField(tafjRuntime, "Call service AML error: " + e.toString(),
                                    "CUSTOMER", "MSB.AUTO.AML");
                            return;
                        }
                    }
                }
            } else {
                if (!checksum.equals(oldChecksum)) {
                    // Thực hiện KYC và default dữ liệu lên bản ghi:
                    logger.info("MsbfCusAMLSironKyc KYC START !checksum.equals(oldChecksum): ");
                    OnboardingRequest request = buildRequest(customerRec);
                    try {
                        String jsonBody = objectMapper.writeValueAsString(request);
                        logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) request: " + jsonBody);

                        OnboardingResponse response = client.initiateOnboardingTyped(request);
                        String responseCode = response.getErrorDetails().get(0).getResponseCode();
                        String responseDescription = response.getErrorDetails().get(0).getResponseCode();
                        logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) ResponseCode: " + responseCode);
                        logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) ResponseDescription: "
                                + responseDescription);

                        if (!responseCode.equals("200")) {
                            MsbUtils.setErrorLocalField(tafjRuntime,
                                    "Error code = " + responseCode + "/" + responseDescription, "CUSTOMER",
                                    "MSB.AUTO.AML");
                            return;
                        }

                        if (response.getAmlStatus() != null) {
                            amlReference = response.getRequestId();
                            amlResult = response.getAmlStatus().getCustomerScreening();
                            amlRrResult = response.getAmlStatus().getModelBaseRisk();
                            String amlCaseUrl = response.getAmlCaseUrl();
                            logger.info(
                                    "MsbfCusAMLSironKyc !checksum.equals(oldChecksum) amlReference: " + amlReference);
                            logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) amlResult: " + amlResult);
                            logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) rrResult: " + amlRrResult);
                            logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) amlCaseUrl: " + amlCaseUrl);

                            customerRec.getLocalRefField(MSB_AML_REFERENCE).setValue(amlReference);
                            customerRec.getLocalRefField(MSB_AML_RESULT).setValue(amlResult);
                            customerRec.getLocalRefField("MSB.RR.RESULT").setValue(amlRrResult);
                            customerRec.getLocalRefField("MSB.AML.DATETIME")
                                    .setValue(session.getCurrentVariable("!TODAY"));
                            customerRec.getLocalRefField("MSB.DECISION").setValue("");
                            if ("HIT".equals(amlResult) || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))) {
                                if (amlCaseUrl != null) {

                                    customerRec.getLocalRefField("MSB.OVERRIDE.REASON")
                                            .setValue(amlCaseUrl.split("\\?")[1]);
                                }
                            } else {
                                customerRec.getLocalRefField("MSB.OVERRIDE.REASON").setValue("");
                            }

                            // Xac dinh muc dich goi AML:
                            String amlPurpose = "";
                            if (customerRec.getCurrNo().compareTo("1") > 0) {
                                amlPurpose = "Amend CIF";
                            } else {
                                amlPurpose = "Create CIF";
                            }

                            // update AML log table:
                            logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) write NEW log START: idlog = "
                                    + idlog);
                            AmlRefClass amlRefClass = new AmlRefClass();
                            amlRefClass.setAmlDatetime(response.getDatetime());
                            amlRefClass.setAmlDecision("");
                            amlRefClass.setAmlPurpose(amlPurpose);
                            amlRefClass.setAmlRef(amlReference);
                            amlRefClass.setAmlResult(amlResult);
                            amlRefClass.setAmlRrResult(amlRrResult);
                            amlRefClass.setApplication("CUSTOMER");
                            amlRefClass.setAppplicationId(currentRecordId);
                            if ("HIT".equals(amlResult) || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))) {
                                if (amlCaseUrl != null) {
                                    amlRefClass.setAmlUrl(amlCaseUrl.split("\\?")[1]);
                                }
                            } else {
                                amlRefClass.setAmlUrl("");
                            }
                            amlRefClass.setChecksum(checksum);

                            // if (flagNewLog) {
                            // amlLogRecord.setId(idlog);
                            // amlLogRecord.setAmlRef(amlRefClass,
                            // 0);
                            // } else {
                            // amlLogRecord.setAmlRef(amlRefClass,
                            // amlLogRecord.getAmlRef().size());
                            for (int i = 0; i < amlLogRecord.getAmlRef().size(); i++) {
                                if ("CUSTOMER".equals(amlLogRecord.getAmlRef(i).getApplication().getValue())
                                        && currentRecordId
                                                .equals(amlLogRecord.getAmlRef(i).getAppplicationId().getValue())) {
                                    amlLogRecord.getAmlRef(i).setAmlDatetime(session.getCurrentVariable("!TODAY"));
                                    amlLogRecord.getAmlRef(i).setAmlDecision("");
                                    amlLogRecord.getAmlRef(i).setAmlPurpose(amlPurpose);
                                    amlLogRecord.getAmlRef(i).setAmlRef(amlReference);
                                    amlLogRecord.getAmlRef(i).setAmlResult(amlResult);
                                    amlLogRecord.getAmlRef(i).setAmlRrResult(amlRrResult);
                                    if ("HIT".equals(amlResult)
                                            || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))) {
                                        if (amlCaseUrl != null) {
                                            amlLogRecord.getAmlRef(i).setAmlUrl(amlCaseUrl.split("\\?")[1]);
                                        }
                                    } else {
                                        amlLogRecord.getAmlRef(i).setAmlUrl("");
                                    }
                                    amlLogRecord.getAmlRef(i).setChecksum(checksum);
                                    logger.info(
                                            "MsbfCusAMLSironKyc !checksum.equals(oldChecksum) write NEW log: amlLogRecord = "
                                                    + amlLogRecord.toString());
                                    break;
                                }
                            }
                            // }

                            EbMsbCifAmlLogTable amlLogTable = new EbMsbCifAmlLogTable(this);
                            amlLogTable.write(idlog, amlLogRecord);
                            logger.info("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) write NEW log END");

                            // update fatca
                            // updateFatca(customerRec,
                            // t24RecordUtils);

                            // set noinput field
                            // String[] localFieldNoinput = new
                            // String[] { "MSB.AML.REF",
                            // MSB_AML_RESULT,
                            // "MSB.RR.RESULT" };
                            MsbUtils.setLocalFieldNoInput(tafjRuntime, true, "CUSTOMER", localFieldNoinput);
                        } else {
                            MsbUtils.setErrorLocalField(tafjRuntime, "Call service AML error: " + responseDescription,
                                    "CUSTOMER", "MSB.AUTO.AML");
                        }

                    } catch (Exception e) {
                        logger.error("MsbfCusAMLSironKyc !checksum.equals(oldChecksum) Error calling Onboarding API: {}"
                                + e);
                        MsbUtils.setErrorLocalField(tafjRuntime, "Call service AML error: " + e.toString(), "CUSTOMER",
                                "MSB.AUTO.AML");
                        return;
                    }
                    // customerRec.getLocalRefField("MSB.AML.CHECKSUM").setValue(checksum);
                    if (customerRec.getLocalRefGroups("MSB.CHK.SUM").size() > 0) {
                        for (int i = customerRec.getLocalRefGroups("MSB.CHK.SUM").size() - 1; i >= 0; i--) {
                            customerRec.getLocalRefGroups("MSB.CHK.SUM").remove(i);
                        }
                    }
                    // Matcher m = Pattern.compile(".{1,70}").matcher(checksum);
                    // while (m.find()) {
                    // // System.out.println(m.group());
                    // LocalRefGroup localRefGrp = new LocalRefGroup(m.group());
                    // customerRec.getLocalRefGroups("MSB.CHK.SUM").add(localRefGrp);
                    // }
                    String[] listOfVal = checksum.split("\\*");
                    for (int i = 0; i < listOfVal.length; i++) {
                        LocalRefGroup localRefGrp = new LocalRefGroup(listOfVal[i]);
                        customerRec.getLocalRefGroups("MSB.CHK.SUM").add(i, localRefGrp);
                    }
                }
            }
        } catch (Exception ex) {
            flagNewLog = true;
            amlLogRecord = new EbMsbCifAmlLogRecord();
            logger.info("MsbfCusAMLSironKyc flagNewLog: " + flagNewLog);

            // Xác định KYC hay QUERY với bản ghi INAU: Có override thì chỉ
            // QUERY
            // Đã KYC trong ngày và không thay đổi thông tin KH: Không thực
            // hiện
            // KYC lại
            if (!chkAmlRef && amlPosition < 0 && session.getCurrentVariable("!TODAY")
                    .compareTo(customerRec.getLocalRefField("MSB.AML.DATETIME").getValue()) > 0) {

                // Thực hiện KYC và default dữ liệu lên bản ghi:
                logger.info("MsbfCusAMLSironKyc KYC START: ");
                if (!checksum.equals(oldChecksum)) {
                    OnboardingRequest request = buildRequest(customerRec);
                    try {
                        String jsonBody = objectMapper.writeValueAsString(request);
                        logger.info("MsbfCusAMLSironKyc request: " + jsonBody);

                        OnboardingResponse response = client.initiateOnboardingTyped(request);
                        String responseCode = response.getErrorDetails().get(0).getResponseCode();
                        String responseDescription = response.getErrorDetails().get(0).getResponseCode();
                        logger.info("MsbfCusAMLSironKyc ResponseCode: " + responseCode);
                        logger.info("MsbfCusAMLSironKyc ResponseDescription: " + responseDescription);

                        if (!responseCode.equals("200")) {
                            MsbUtils.setErrorLocalField(tafjRuntime,
                                    "Error code = " + responseCode + "/" + responseDescription, "CUSTOMER",
                                    "MSB.AUTO.AML");
                            return;
                        }

                        if (response.getAmlStatus() != null) {
                            amlReference = response.getRequestId();
                            amlResult = response.getAmlStatus().getCustomerScreening();
                            amlRrResult = response.getAmlStatus().getModelBaseRisk();
                            String amlCaseUrl = response.getAmlCaseUrl();
                            logger.info("MsbfCusAMLSironKyc amlReference: " + amlReference);
                            logger.info("MsbfCusAMLSironKyc amlResult: " + amlResult);
                            logger.info("MsbfCusAMLSironKyc rrResult: " + amlRrResult);
                            logger.info("MsbfCusAMLSironKyc amlCaseUrl: " + amlCaseUrl);

                            customerRec.getLocalRefField(MSB_AML_REFERENCE).setValue(amlReference);
                            customerRec.getLocalRefField(MSB_AML_RESULT).setValue(amlResult);
                            customerRec.getLocalRefField("MSB.RR.RESULT").setValue(amlRrResult);
                            customerRec.getLocalRefField("MSB.AML.DATETIME")
                                    .setValue(session.getCurrentVariable("!TODAY"));
                            if ("HIT".equals(amlResult) || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))) {
                                if (amlCaseUrl != null) {

                                    customerRec.getLocalRefField("MSB.OVERRIDE.REASON")
                                            .setValue(amlCaseUrl.split("\\?")[1]);
                                }
                            } else {
                                customerRec.getLocalRefField("MSB.OVERRIDE.REASON").setValue("");
                            }

                            // Xac dinh muc dich goi AML:
                            String amlPurpose = "";
                            if (customerRec.getCurrNo().compareTo("1") > 0) {
                                amlPurpose = "Amend CIF";
                            } else {
                                amlPurpose = "Create CIF";
                            }

                            // update AML log table:
                            logger.info("MsbfCusAMLSironKyc write NEW log START: idlog = " + idlog);
                            AmlRefClass amlRefClass = new AmlRefClass();
                            amlRefClass.setAmlDatetime(session.getCurrentVariable("!TODAY"));
                            amlRefClass.setAmlDecision("");
                            amlRefClass.setAmlPurpose(amlPurpose);
                            amlRefClass.setAmlRef(amlReference);
                            amlRefClass.setAmlResult(amlResult);
                            amlRefClass.setAmlRrResult(amlRrResult);
                            amlRefClass.setApplication("CUSTOMER");
                            amlRefClass.setAppplicationId(currentRecordId);
                            if ("HIT".equals(amlResult) || ("NO HIT".equals(amlResult) && "HIGH".equals(amlRrResult))) {
                                if (amlCaseUrl != null) {
                                    amlRefClass.setAmlUrl(amlCaseUrl.split("\\?")[1]);
                                }
                            } else {
                                amlRefClass.setAmlUrl("");
                            }
                            amlRefClass.setChecksum(checksum);
                            customerRec.getLocalRefField("MSB.DECISION").setValue("");

                            // if (flagNewLog) {
                            // amlLogRecord.setId(idlog);
                            // amlLogRecord.setAmlRef(amlRefClass, 0);
                            // } else {
                            amlLogRecord.setAmlRef(amlRefClass, amlLogRecord.getAmlRef().size());
                            // }

                            EbMsbCifAmlLogTable amlLogTable = new EbMsbCifAmlLogTable(this);
                            amlLogTable.write(idlog, amlLogRecord);
                            logger.info("MsbfCusAMLSironKyc write NEW log END");

                            // update fatca
                            // updateFatca(customerRec, t24RecordUtils);

                            // set noinput field
                            // String[] localFieldNoinput = new String[] {
                            // "MSB.AML.REF", MSB_AML_RESULT,
                            // "MSB.RR.RESULT" };
                            MsbUtils.setLocalFieldNoInput(tafjRuntime, true, "CUSTOMER", localFieldNoinput);
                        } else {
                            MsbUtils.setErrorLocalField(tafjRuntime, "Call service AML error: " + responseDescription,
                                    "CUSTOMER", "MSB.AUTO.AML");
                        }

                    } catch (Exception e) {
                        logger.error("MsbfCusAMLSironKyc Error calling Onboarding API {}: " + e);
                        MsbUtils.setErrorLocalField(tafjRuntime, "Call service AML error: " + e.toString(), "CUSTOMER",
                                "MSB.AUTO.AML");
                        return;
                    }
                    if (customerRec.getLocalRefGroups("MSB.CHK.SUM").size() > 0) {
                        for (int i = customerRec.getLocalRefGroups("MSB.CHK.SUM").size() - 1; i >= 0; i--) {
                            customerRec.getLocalRefGroups("MSB.CHK.SUM").remove(i);
                        }
                    }
                    // Matcher m = Pattern.compile(".{1,70}").matcher(checksum);
                    // while (m.find()) {
                    // // System.out.println(m.group());
                    // LocalRefGroup localRefGrp = new LocalRefGroup(m.group());
                    // customerRec.getLocalRefGroups("MSB.CHK.SUM").add(localRefGrp);
                    // }
                    // customerRec.getLocalRefGroups("MSB.CHK.SUM").add(getLocalRefGrp(checksum));
                    String[] listOfVal = checksum.split("\\*");
                    for (int i = 0; i < listOfVal.length; i++) {
                        LocalRefGroup localRefGrp = new LocalRefGroup(listOfVal[i]);
                        customerRec.getLocalRefGroups("MSB.CHK.SUM").add(i, localRefGrp);
                    }
                }
            } else {
                if (session.getCurrentVariable("!TODAY")
                        .compareTo(customerRec.getLocalRefField("MSB.AML.DATETIME").getValue()) == 0) {
                    if (!checksum.equals(oldChecksum) || (!chkAmlRef
                            && ("HIT".equals(customerRec.getLocalRefField(MSB_AML_RESULT).getValue()))
                            || ("NO HIT".equals(customerRec.getLocalRefField(MSB_AML_RESULT).getValue())
                                    && "HIGH".equals(customerRec.getLocalRefField("MSB.RR.RESULT").getValue())))) {
                        logger.info("MsbfCusAMLSironKyc write CALL AGAIN log START");
                        String amlPurpose = "";
                        if (customerRec.getCurrNo().compareTo("1") > 0) {
                            amlPurpose = "Amend CIF";
                        } else {
                            amlPurpose = "Create CIF";
                        }
                        AmlRefClass amlRefClass = new AmlRefClass();
                        amlRefClass.setAmlDatetime(customerRec.getLocalRefField("MSB.AML.DATETIME").getValue());
                        amlRefClass.setAmlDecision("");
                        amlRefClass.setAmlPurpose(amlPurpose);
                        amlRefClass.setAmlRef(customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue());
                        amlRefClass.setAmlResult(customerRec.getLocalRefField(MSB_AML_RESULT).getValue());
                        amlRefClass.setAmlRrResult(customerRec.getLocalRefField("MSB.RR.RESULT").getValue());
                        amlRefClass.setApplication("CUSTOMER");
                        amlRefClass.setAppplicationId(currentRecordId);
                        if (customerRec.getLocalRefField("MSB.OVERRIDE.REASON") != null) {
                            amlRefClass.setAmlUrl(customerRec.getLocalRefField("MSB.OVERRIDE.REASON").getValue());
                        }
                        amlRefClass.setChecksum(checksum);
                        amlLogRecord.setAmlRef(amlRefClass, amlLogRecord.getAmlRef().size());
                        try {
                            EbMsbCifAmlLogTable amlLogTable = new EbMsbCifAmlLogTable(this);
                            amlLogTable.write(idlog, amlLogRecord);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            // Uncomment and replace with appropriate logger
                            // LOGGER.error(exception_var, exception_var);
                            MsbUtils.setErrorLocalField(tafjRuntime, "Write log file error:" + e.getMessage(),
                                    "CUSTOMER", "MSB.AUTO.AML");
                        }
                        logger.info("MsbfCusAMLSironKyc write CALL AGAIN log END");
                    }
                }
            }
        }
        logger.info("MsbfCusAMLSironKyc END customerRec.toString():" + customerRec.toString());
        currentRecord.set(customerRec.toStructure());
    }

    private void updateFatca(CustomerRecord customerRec, T24RecordUtils t24RecordUtils) {
        int sector = Integer.parseInt(customerRec.getSector().getValue());
        EbMsbhInterfaceParameterRecord paramRec = null;
        msbhParameterRecord = t24RecordUtils.getRecord("", "MSBH.PARAMETER", "", "MSB.AML.CORP",
                MsbhParameterRecord.class);
        String requestBody = "";
        if (sector < 2000) {
            paramRec = t24RecordUtils.getRecord("", EB_MSBH_INTERFACE_PARAMETER, "", "AML.KYC.INDV",
                    EbMsbhInterfaceParameterRecord.class);
            requestBody = buildBodyIndv(paramRec, customerRec);
            logger.info("MsbfCusAMLSironKyc requestBody: " + requestBody);
        } else {
            paramRec = t24RecordUtils.getRecord("", EB_MSBH_INTERFACE_PARAMETER, "", "AML.KYC.CORP",
                    EbMsbhInterfaceParameterRecord.class);
            requestBody = buildBodyCorp(paramRec, customerRec, t24RecordUtils);
        }

        try {
            // begin andn
            String apiKey = MsbUtils.msbGetParameter("KONG.PARAM", "COMMON", "APIKEY", da);
            if (apiKey.isEmpty()) {
                logger.error("{}.MsbfCusAMLSironKyc() KONG.PARAM API Key is missing", MsbUpdateFatca.class.getName());
                return;
            }
            // end andn
            String data = MsbfCusAMLUtil.post(paramRec.getUrl().getValue(), requestBody,
                    paramRec.getReserved6().getValue(), apiKey);
            if (StringUtils.isNotEmpty(data)) {
                JSONObject response = new JSONObject(data);
                String respCode = "";
                if (sector < 2000) {
                    respCode = response.getJSONObject("updateFatcaPersonal").getJSONObject("respMessage")
                            .getString("respCode");
                    logger.info("MsbfCusAMLSironKyc respCode KHCN: " + respCode);
                } else {
                    respCode = response.getJSONObject("updateFatcaOrganization").getJSONObject("respMessage")
                            .getString("respCode");
                    logger.info("MsbfCusAMLSironKyc respCode KHDN: " + respCode);
                }
                customerRec.getLocalRefField("MSB.FATCA.RESULT").setValue(respCode);
                logger.info("MsbfCusAMLSironKyc customerRec.getLocalRefField(MSB.FATCA.RESULT).getValue(): "
                        + customerRec.getLocalRefField("MSB.FATCA.RESULT").getValue());
            }
        } catch (Exception ex) {
            logger.error("MsbUpdateFatca error exception: {}", ex.getMessage());
        }
    }

    @Override
    public void defaultFieldValuesOnHotField(String application, String currentRecordId, TStructure currentRecord,
            InputValue currentInputValue, TStructure unauthorisedRecord, TStructure liveRecord,
            TransactionContext transactionContext) {
        // TODO Auto-generated method stub
        CustomerRecord customerRec = new CustomerRecord(currentRecord);
        logger.info("MsbfCusAMLSironKyc Start defaultFieldValuesOnHotField currentInputValue.toString():"
                + currentInputValue.toString());
        TField autoAMLField = customerRec.getLocalRefField(MSB_AUTO_CHECK_AML);
        TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
        if (currentInputValue.getFieldName().startsWith(".MSB.AUTO.AML")) {
            if (!"Yes".equalsIgnoreCase(autoAMLField.getValue())) {
                logger.info(
                        "MsbfCusAMLSironKyc defaultFieldValuesOnHotField customerRec.getLocalRefField(MSB_AUTO_CHECK_AML).getValue():"
                                + customerRec.getLocalRefField(MSB_AUTO_CHECK_AML).getValue());
                customerRec.getLocalRefField(MSB_AML_REFERENCE).setValue("");
                customerRec.getLocalRefField(MSB_AML_RESULT).setValue("");
                customerRec.getLocalRefField("MSB.RR.RESULT").setValue("");
                customerRec.getLocalRefField("MSB.DECISION").setValue("");
                customerRec.getLocalRefField("MSB.AML.DATETIME").setValue(session.getCurrentVariable("!TODAY"));
                customerRec.getLocalRefField("MSB.OVERRIDE.REASON").setValue("");
                // set input field
                String[] localFieldNoinput = new String[] { "MSB.AML.REF", MSB_AML_RESULT, "MSB.RR.RESULT",
                        "MSB.DECISION", "MSB.OVERRIDE" };
                MsbUtils.setLocalFieldNoInput(tafjRuntime, false, "CUSTOMER", localFieldNoinput);
            } else {
                customerRec.getLocalRefField(MSB_AML_REFERENCE).setValue("");
                customerRec.getLocalRefField(MSB_AML_RESULT).setValue("");
                customerRec.getLocalRefField("MSB.RR.RESULT").setValue("");
                customerRec.getLocalRefField("MSB.DECISION").setValue("");
                customerRec.getLocalRefField("MSB.AML.DATETIME").setValue("");
                customerRec.getLocalRefField("MSB.OVERRIDE.REASON").setValue("");
                // set noinput field
                String[] localFieldNoinput = new String[] { "MSB.AML.REF", MSB_AML_RESULT, "MSB.RR.RESULT",
                        "MSB.DECISION", "MSB.OVERRIDE" };
                MsbUtils.setLocalFieldNoInput(tafjRuntime, true, "CUSTOMER", localFieldNoinput);
            }
        }
        logger.info(
                "MsbfCusAMLSironKyc END defaultFieldValuesOnHotField customerRec.toString():" + customerRec.toString());
        currentRecord.set(customerRec.toStructure());
    }

    private String buildBodyIndv(EbMsbhInterfaceParameterRecord paramRec, CustomerRecord customerRec) {
        JSONObject authenInfo = MsbfCusAMLUtil.buildAuthenInfo(paramRec);
        JSONObject updateFatcaPersonal = new JSONObject();
        updateFatcaPersonal.put(AUTHEN_INFO, authenInfo);

        String msbHaveNotFatca = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.HAVE.NOT.FATCA").getValue())
                ? "Y" : "N";
        String msbUsCitizen = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.CITIZEN").getValue()) ? "Y"
                : "N";
        String msbUsBirth = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.BIRTH").getValue()) ? "Y" : "N";
        String msbUsCustAddr = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.CUST.ADDR").getValue()) ? "Y"
                : "N";
        String msbUsPhoneNo = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.PH.NO").getValue()) ? "Y" : "N";
        String msbUsRegFix = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.REG.FIX").getValue()) ? "Y"
                : "N";
        String msbUsAttorneyAddr = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.ATTORNEY.ADDR").getValue())
                ? "Y" : "N";
        String msbUsMailPermAddr = "Y"
                .equalsIgnoreCase(customerRec.getLocalRefField("MSB.US.MAIL.PERM.ADDR").getValue()) ? "Y" : "N";
        String msbCommitForm = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.COMMT.FORM").getValue()) ? "Y"
                : "N";
        String kycNumber = customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue();

        updateFatcaPersonal.put("msbHaveNotFatca", msbHaveNotFatca);
        updateFatcaPersonal.put("msbUsCitizen", msbUsCitizen);
        updateFatcaPersonal.put("msbUsBirth", msbUsBirth);
        updateFatcaPersonal.put("msbUsCustAddr", msbUsCustAddr);
        updateFatcaPersonal.put("msbUsPhoneNo", msbUsPhoneNo);
        updateFatcaPersonal.put("msbUsRegFix", msbUsRegFix);
        updateFatcaPersonal.put("msbUsAttorneyAddr", msbUsAttorneyAddr);
        updateFatcaPersonal.put("msbUsMailPermAddr", msbUsMailPermAddr);
        updateFatcaPersonal.put("msbCommitForm", msbCommitForm);
        updateFatcaPersonal.put("kycNumber", kycNumber);

        logger.info("MsbfCusAMLSironKyc updateFatcaPersonal: " + updateFatcaPersonal);
        JSONObject requestBody = new JSONObject();
        requestBody.put("updateFatcaPersonal", updateFatcaPersonal);
        logger.info("MsbfCusAMLSironKyc requestBody: " + requestBody);
        return requestBody.toString();
    }

    private String buildBodyCorp(EbMsbhInterfaceParameterRecord paramRec, CustomerRecord customerRec,
            T24RecordUtils t24RecordUtils) {
        JSONObject authenInfo = MsbfCusAMLUtil.buildAuthenInfo(paramRec);
        JSONObject updateFatcaOrganization = new JSONObject();
        updateFatcaOrganization.put(AUTHEN_INFO, authenInfo);

        String msbDebtDoc1 = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.DEBT.DOC.1").getValue()) ? "Y"
                : "N";
        String msbDebtDoc2 = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.DEBT.DOC.2").getValue()) ? "Y"
                : "N";
        String msbDebtDoc34 = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.DEBT.DOC.3.4").getValue()) ? "Y"
                : "N";
        String msbDebtDoc4 = "Y".equalsIgnoreCase(customerRec.getLocalRefField("MSB.DEBT.DOC.4").getValue()) ? "Y"
                : "N";

        updateFatcaOrganization.put("msbCorpType", getValueFromEbLookUp("MSB.CORP.TYPE", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("msbRemark1", customerRec.getLocalRefField("MSB.REMARK.1").getValue());
        updateFatcaOrganization.put("msbDebtDoc1", msbDebtDoc1);
        updateFatcaOrganization.put("msbDebtDoc2", msbDebtDoc2);
        updateFatcaOrganization.put("msbDebtDoc34", msbDebtDoc34);
        updateFatcaOrganization.put("msbDebtDoc4", msbDebtDoc4);
        updateFatcaOrganization.put("msbConfCompPer1",
                getValueFromEbLookUp("MSB.CONF.COMP.PER.1", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("msbOrgExmpt2",
                getValueFromEbLookUp("MSB.ORG.EXMPT.2", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("msbFinCorp3", getValueFromEbLookUp("MSB.FIN.CORP.3", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("msbGiinCode", customerRec.getLocalRefField("MSB.GIIN.CODE").getValue());
        updateFatcaOrganization.put("msbFixExmpt34",
                getValueFromEbLookUp("MSB.FIX.EXMPT.3.4", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("msbUsNonFin4",
                getValueFromEbLookUp("MSB.US.NON.FIN.4", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("msbNonFinOrg4", customerRec.getLocalRefField("MSB.NON.FIN.ORG.4").getValue());
        updateFatcaOrganization.put("msbNote", customerRec.getLocalRefField("MSB.NOTE").getValue());
        updateFatcaOrganization.put("msbDissidentOrg5",
                getValueFromEbLookUp("MSB.DISSIDENT.ORG.5", customerRec, t24RecordUtils));
        updateFatcaOrganization.put("kycNumber", customerRec.getLocalRefField(MSB_AML_REFERENCE).getValue());
        logger.info("MsbfCusAMLSironKyc updateFatcaOrganization: " + updateFatcaOrganization);
        JSONObject requestBody = new JSONObject();
        requestBody.put("updateFatcaOrganization", updateFatcaOrganization);

        logger.info("MsbfCusAMLSironKyc requestBody khdn: " + requestBody);
        return requestBody.toString();
    }

    private String getValueFromEbLookUp(String fieldname, CustomerRecord customerRec, T24RecordUtils t24RecordUtils) {
        String result = "";
        TField tfield = customerRec.getLocalRefField(fieldname);
        if (StringUtils.isEmpty(tfield.getValue())) {
            return result;
        }

        // try to get value from msbh param special first if not found get value
        // from
        // eblookup
        result = getValueFromMsbhParam(fieldname, tfield.getValue());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }

        String recid = fieldname + "*" + tfield.getValue();
        EbLookupRecord ebLookupRecord = t24RecordUtils.getRecord("", "EB.LOOKUP", "", recid, EbLookupRecord.class);
        if (ebLookupRecord != null) {
            result = ebLookupRecord.getDescription().get(0).getValue();
        }
        return result;
    }

    private String getValueFromMsbhParam(String fieldName, String key) {
        if (msbhParameterRecord == null) {
            return "";
        }
        List<GroupClass> groups = msbhParameterRecord.getGroup();
        for (GroupClass group : groups) {
            if (fieldName.equalsIgnoreCase(group.getGroup().getValue())) {
                List<ParamNameClass> paramNameClasses = group.getParamName();
                for (ParamNameClass param : paramNameClasses) {
                    if (key.equalsIgnoreCase(param.getParamName().getValue())) {
                        return param.getValue().getValue();
                    }
                }
            }
        }
        return "";
    }

    // build request to call KYC
    private OnboardingRequest buildRequest(CustomerRecord customerRec) {
        OnboardingRequest request = new OnboardingRequest();
        request.setApplicationId("T24");
        UserRecord userRec = session.getUserRecord();
        String userSsoId = userRec.getLocalRefField("MSB.SSO.ID").getValue();
        if (!"".equals(userSsoId)) {
            request.setRequestUserId(userRec.getLocalRefField("MSB.SSO.ID").getValue());
        } else {
            request.setRequestUserId("KOB_ONLINE");
        }

        request.setSyncAPIFlag("Y");
        // request.setIsCallBack("N");
        logger.info(
                "MsbfCusAMLSironKyc.buildRequest RequestUserId: " + userRec.getLocalRefField("MSB.SSO.ID").getValue());

        OnboardingCustomer customer = new OnboardingCustomer();

        // LEGAL.ID(ApplicantID)
        String legalId = "";
        String legalIssDate = "";
        String legalType = "";
        String legalIssAuth = "";
        String national = customerRec.getNationality().getValue();
        int sector = Integer.parseInt(customerRec.getSector().getValue());
        logger.info("MsbfCusAMLSironKyc.buildRequest sector: " + sector);
        // List<Identification> listOfIdKHCN = new ArrayList<>();
        List<Identification> listOfIdKHTC = new ArrayList<>();
        for (int i = 0; i < customerRec.getLegalId().size(); i++) {
            String docName = customerRec.getLegalId(i).getLegalDocName().getValue();
            if (sector < 2000) {
                if ((national.equals("VN") && docName.equals("IDC"))
                        || (!national.equals("VN") && docName.equals("PP"))) {
                    legalId = hasFieldValue(customerRec.getLegalId(i).getLegalId());
                    legalType = docName;
                    legalIssDate = hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(4, 6) + "-"
                            + hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(6, 8) + "-"
                            + hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(0, 4);
                    legalIssAuth = hasFieldValue(customerRec.getLegalId(i).getLegalIssAuth());
                }
                // else if (!national.equals("VN") && docName.equals("PP")) {
                // legalId =
                // hasFieldValue(customerRec.getLegalId(i).getLegalId());
                // legalType = docName;
                // legalIssDate =
                // hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(4,
                // 6) + "-"
                // +
                // hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(6,
                // 8) + "-"
                // +
                // hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(0,
                // 4);
                // legalIssAuth =
                // hasFieldValue(customerRec.getLegalId(i).getLegalIssAuth());
                // }
                customer.setDateOfIncorporation("");
                String dtOfBirth = hasFieldValue(customerRec.getDateOfBirth()).substring(4, 6) + "-"
                        + hasFieldValue(customerRec.getDateOfBirth()).substring(6, 8) + "-"
                        + hasFieldValue(customerRec.getDateOfBirth()).substring(0, 4);
                customer.setDateOfBirth(dtOfBirth);
            } else {
                // if (docName.equals("BL") || docName.equals("BR")) {
                legalId = customerRec.getLegalId(i).getLegalId().getValue();
                legalType = docName;
                legalIssDate = hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(4, 6) + "-"
                        + hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(6, 8) + "-"
                        + hasFieldValue(customerRec.getLegalId(i).getLegalIssDate()).substring(0, 4);
                legalIssAuth = hasFieldValue(customerRec.getLegalId(i).getLegalIssAuth());
                listOfIdKHTC.add(new Identification(legalType, legalId, legalIssDate, legalIssAuth));
                // legalIssDate =
                // customerRec.getLegalId(i).getLegalIssDate().getValue();
                // DateTimeFormatter inputFormatter =
                // DateTimeFormatter.ofPattern("yyyyMMdd");
                // DateTimeFormatter outputFormatter =
                // DateTimeFormatter.ofPattern("dd-MM-yyyy");
                // LocalDate date = LocalDate.parse(legalIssDate,
                // inputFormatter);
                // legalIssDate = date.format(outputFormatter);
                // }
                // KHDN: Ngày thành lập
                String dtOfIncorBirth = hasFieldValue(customerRec.getBirthIncorpDate()).substring(4, 6) + "-"
                        + hasFieldValue(customerRec.getBirthIncorpDate()).substring(6, 8) + "-"
                        + hasFieldValue(customerRec.getBirthIncorpDate()).substring(0, 4);
                customer.setDateOfIncorporation(dtOfIncorBirth);
            }
        }
        logger.info("MsbfCusAMLSironKyc.buildRequest legalId: " + legalId);
        logger.info("MsbfCusAMLSironKyc.buildRequest legalIssDate: " + legalIssDate);
        logger.info("MsbfCusAMLSironKyc.buildRequest legalType: " + legalType);
        customer.setApplicantID(legalId);
        customer.setBusinessDomain("A");
        String msbFullName = getCustomerFullName(customerRec);
        // CUSTOMER.TYPE
        if (sector < 2000) {
            customer.setCustomerType("IND"); // KHCN: IND, KHDN: ORG
            customer.setIndustry("");
            customer.setOnboardingCustomerCountry(Collections.singletonList(new CountryRel(
                    customerRec.getLocalRefField("MSB.RESIDENT").getValue(), customerRec.getResidence().getValue())));
            customer.setPrimaryCitizenship(customerRec.getNationality().getValue());
            customer.setOnboardingCustomerIdentification(
                    Collections.singletonList(new Identification(legalType, legalId, legalIssDate, legalIssAuth)));
        } else {
            customer.setCustomerType("ORG"); // KHCN: IND, KHDN: ORG
            customer.setIndustry(customerRec.getIndustry().getValue());
            customer.setOnboardingCustomerCountry(
                    Collections.singletonList(new CountryRel(customerRec.getLocalRefField("MSB.RESIDENT").getValue(),
                            customerRec.getAddressCountry().getValue())));
            List<Address> listAddr = new ArrayList<>();
            Address addr = new Address();
            addr.setCountry(customerRec.getAddressCountry().getValue());
            addr.setAddressPurpose("B");
            listAddr.add(addr);
            customer.setOnboardingCustomerAddress(listAddr);
            customer.setOrganizationName(msbFullName);
            customer.setOnboardingCustomerIdentification(listOfIdKHTC);
        }

        customer.setBranchCd(customerRec.getCompanyBook().getValue());

        // Get MSB.FULL.NAME

        logger.info("MsbfCusAMLSironKyc.buildRequest msbFullName: " + msbFullName);

        customer.setDisplayName(msbFullName); // MSB.FULL.NAME
        customer.setJurisdiction("All");

        logger.info(
                "MsbfCusAMLSironKyc.buildRequest EmploymentStatus size: " + customerRec.getEmploymentStatus().size());
        if (customerRec.getEmploymentStatus().size() > 0) {
            customer.setOccupation(customerRec.getEmploymentStatus(0).getJobTitle().getValue());
        } else {
            customer.setOccupation("");
        }

        customer.setOnboardingCustomerAnticipatoryProfile(
                Collections.singletonList(new AnticipatoryProfile("KYC", "D")));

        logger.info("MsbfCusAMLSironKyc.buildRequest MSB.PROD.OFFERED size: "
                + customerRec.getLocalRefGroups("MSB.PROD.OFFERED").size());
        if (customerRec.getLocalRefGroups("MSB.PROD.OFFERED").size() > 0) {
            List<Product> listProdOffer = new ArrayList<Product>();
            for (int i = 0; i < customerRec.getLocalRefGroups("MSB.PROD.OFFERED").size(); i++) {
                logger.info(
                        "MsbfCusAMLSironKyc.buildRequest MSB.PROD.OFFERED get(i).getLocalRefField('MSB.PROD.OFFERED').getValue(): "
                                + customerRec.getLocalRefGroups("MSB.PROD.OFFERED").get(i)
                                        .getLocalRefField("MSB.PROD.OFFERED").getValue());

                try {
                    EbLookupRecord ebLookupRec = new EbLookupRecord(da.getRecord("", "EB.LOOKUP", "",
                            "MSB.PROD.OFFERED*" + customerRec.getLocalRefGroups("MSB.PROD.OFFERED").get(i)
                                    .getLocalRefField("MSB.PROD.OFFERED").getValue()));
                    listProdOffer.add(listProdOffer.size(), new Product(ebLookupRec.getDescription(1).getValue()));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // Uncomment and replace with appropriate logger
                    // LOGGER.error(exception_var, exception_var);
                }
            }
            logger.info("MsbfCusAMLSironKyc.buildRequest MSB.PROD.OFFERED listProdOffer.toString(): "
                    + listProdOffer.toString());
            customer.setOnboardingCustomerProduct(listProdOffer);
        } else {
            customer.setOnboardingCustomerProduct(Collections.singletonList(new Product("")));
        }

        Extend extend = new Extend();
        if (customerRec.getCurrNo().compareTo("1") > 0) {
            extend.setBusinessJourneyType("UPDATE KYC");
        } else {
            extend.setBusinessJourneyType("ONBOARDING");
        }
        customer.setOnboardingCustomerExtend(Collections.singletonList(extend));

        // customer.setOnboardingCustomerPhone(Collections.singletonList(new
        // Phone("C", "+0971374004")));
        // customer.setOnboardingCustomerAddress(
        // Arrays.asList(new Address("L", "US", null, "Times City"), new
        // Address("B", "", "", "Royal City")));

        request.setOnboardingCustomer(customer);
        return request;
    }

    // Hàm kiểm tra xem có thay đổi thông tin bản ghi KH cần KYC lại không
    private String getCheckSumCustomerInfo(CustomerRecord cusRecCurrent) {
        StringBuilder sb = new StringBuilder();
        int sector = Integer.parseInt(cusRecCurrent.getSector().getValue());
        String customerType = cusRecCurrent.getCustomerType().getValue();
        String msbFullName = getCustomerFullName(cusRecCurrent);
        String legalId = getLegalId(cusRecCurrent);
        String resident = cusRecCurrent.getLocalRefField("MSB.RESIDENT").getValue();
        sb.append(sector).append("*");
        sb.append(customerType).append("*");
        sb.append(msbFullName).append("*");
        sb.append(legalId).append("*");
        sb.append(resident).append("*");

        String offerdCurr = "";
        if (cusRecCurrent.getLocalRefGroups("MSB.PROD.OFFERED").size() > 0) {
            offerdCurr = cusRecCurrent.getLocalRefGroups("MSB.PROD.OFFERED").get(0).getLocalRefField("MSB.PROD.OFFERED")
                    .getValue();
        }

        // KH cá nhân
        if (sector < 2000) {
            sb.append(cusRecCurrent.getDateOfBirth().getValue()).append("*");
            sb.append(cusRecCurrent.getNationality().getValue()).append("*");

            if (!customerType.equals("PROSPECT")) {
                sb.append(cusRecCurrent.getEmploymentStatus(0).getJobTitle().getValue()).append("*");
                sb.append(offerdCurr).append("*");
            }
        } else {
            // KH Doanh nghiệp:
            sb.append(cusRecCurrent.getShortName(0).getValue()).append("*");

            String nameCurr = "";
            if (cusRecCurrent.getName1().size() > 0) {
                nameCurr = cusRecCurrent.getName1(0).getValue();
            }
            if (cusRecCurrent.getName2().size() > 0) {
                nameCurr = nameCurr + cusRecCurrent.getName2(0).getValue();
            }
            sb.append(nameCurr).append("*");

            sb.append(cusRecCurrent.getIndustry().getValue()).append("*");
            sb.append(cusRecCurrent.getAddressCountry().getValue()).append("*");
            sb.append(offerdCurr).append("*");
            sb.append(cusRecCurrent.getBirthIncorpDate().getValue()).append("*");
        }
        sb.append(session.getCurrentVariable("!TODAY"));
        return sb.toString();
    }

    // Hàm lấy thông tin MSB.FULL.NAME
    private String getCustomerFullName(CustomerRecord cusRecord) {
        LocalRefList rfullname = cusRecord.getLocalRefGroups(MSB_FULL_NAME);
        StringBuilder fullnameBuilder = new StringBuilder();
        for (LocalRefGroup group : rfullname) {
            TField name = group.getLocalRefField(MSB_FULL_NAME);
            fullnameBuilder.append(name.getValue());
        }
        return fullnameBuilder.toString();
    }

    /*
     * Ham lay thong tin tu truong TEXT
     */
    private String getStringVal(LocalRefList rfullname, String fieldName) {
        logger.info("Start MsbfCusAMLSironKyc getStringVal fieldName:" + fieldName);
        StringBuilder fullnameBuilder = new StringBuilder();
        // for (LocalRefGroup group : rfullname) {
        // TField name = group.getLocalRefField(fieldName);
        // fullnameBuilder.append(name.getValue());
        // }
        for (int i = 0; i < rfullname.size(); i++) {
            if (i < rfullname.size() - 1) {
                fullnameBuilder.append(rfullname.get(i).getLocalRefField(fieldName).getValue()).append("*");
            } else {
                fullnameBuilder.append(rfullname.get(i).getLocalRefField(fieldName).getValue());
            }
        }
        return fullnameBuilder.toString();
    }

    // Hàm lấy thông tin LegalId
    private String getLegalId(CustomerRecord customerRec) {
        String legalId = "";
        int sector = Integer.parseInt(customerRec.getSector().getValue());
        String national = customerRec.getNationality().getValue();
        for (int i = 0; i < customerRec.getLegalId().size(); i++) {
            String docName = customerRec.getLegalId(i).getLegalDocName().getValue();
            if (sector < 2000) {
                if (national.equals("VN") && docName.equals("IDC")) {
                    legalId = customerRec.getLegalId(i).getLegalId().getValue();
                } else if (!national.equals("VN") && docName.equals("PP")) {
                    legalId = customerRec.getLegalId(i).getLegalId().getValue();
                }
            } else {
                // if (docName.equals("BL") || docName.equals("BR")) {
                legalId = customerRec.getLegalId(i).getLegalId().getValue();
                // }
            }
        }
        return legalId;
    }

    // Hàm lấy thông tin Override
    private boolean checkOverride(CustomerRecord cusRecord) {
        boolean checkOve = false;
        OverrideRecord oveRec = new OverrideRecord(da.getRecord("OVERRIDE", "MSB.AML.RESULT.HIT"));
        String oveMsg = oveRec.getMessage(0).getMessage(0).getValue().replace(" &", "");
        logger.info("MsbfCusAMLSironKyc oveMsg: " + oveMsg);
        for (int i = 0; i < cusRecord.getOverride().size(); i++) {
            if (cusRecord.getOverride(i).getValue().contains(oveMsg)) {
                checkOve = true;
            }
        }
        return checkOve;
    }

    private List<String> getOverrideMessage(String msgId) {
        List<String> ls = new ArrayList<String>();
        OverrideRecord dr = utl.getRecord("OVERRIDE", msgId, OverrideRecord.class);
        if (dr != null) {
            ls.add(Objects.toString(dr.getMessage(0).getType().getValue(), ""));
            ls.add(Objects.toString(dr.getMessage(0).getMessage(0).getValue(), ""));
        } else {
            ls.add("Error");
            ls.add(msgId);
        }
        return ls;
    }

    private void setErrOverLocalField(TAFJRuntime tafj, String msgId, String application, String fieldName) {
        int sysFieldNo = MsbUtils.getSysFieldNo(tafj, application, "LOCAL.REF");
        int locFieldNo = MsbUtils.getLocalFieldNo(tafj, application, fieldName);
        List<String> ls = getOverrideMessage(msgId);
        if ("Error".equalsIgnoreCase(ls.get(0))) {
            MsbUtils.setErrorLocalField(tafj, ls.get(1), application, fieldName);
        } else {
            tafj.getSession().getCommonNamed("THE.GLOBUS.COMMON", 16, "AF").set(sysFieldNo);
            tafj.getSession().getCommonNamed("THE.GLOBUS.COMMON", 23, "AV").set(locFieldNo);
            tafj.getSession().getCommonNamed("THE.GLOBUS.COMMON", 84, null).set(ls.get(1));
            try {
                tafj.callJBC("STORE.OVERRIDE", sysFieldNo);
            } catch (Exception e) {
                logger.error("MsbfCusAMLSironKyc setErrOverLocalField callJBC error", e);
            }
        }
    }

    private void setOverLocalField(TAFJRuntime tafj, String msgId, String application, String fieldName) {
        int sysFieldNo = MsbUtils.getSysFieldNo(tafj, application, "LOCAL.REF");
        int locFieldNo = MsbUtils.getLocalFieldNo(tafj, application, fieldName);
        // List<String> ls = getOverrideMessage(msgId);
        // if ("Error".equalsIgnoreCase(ls.get(0))) {
        // MsbUtils.setErrorLocalField(tafj, ls.get(1), application, fieldName);
        // } else {
        tafj.getSession().getCommonNamed("THE.GLOBUS.COMMON", 16, "AF").set(sysFieldNo);
        tafj.getSession().getCommonNamed("THE.GLOBUS.COMMON", 23, "AV").set(locFieldNo);
        tafj.getSession().getCommonNamed("THE.GLOBUS.COMMON", 84, null).set(msgId);
        try {
            tafj.callJBC("STORE.OVERRIDE", sysFieldNo);
        } catch (Exception e) {
            logger.error("MsbfCusAMLSironKyc setOverLocalField callJBC error", e);
        }
        // }
    }

    private void setOverrideByMsbState(String msgId, String application, String fieldName) {
        // if (MsbOdIndustryConst.STATE_1.equals(state)) {
        // msgId = MsbOdIndustryConst.ERROR_1;
        // } else if (MsbOdIndustryConst.STATE_2.equals(state)) {
        // msgId = MsbOdIndustryConst.ERROR_2;
        // } else if (MsbOdIndustryConst.STATE_3.equals(state)) {
        // msgId = MsbOdIndustryConst.ERROR_3;
        // }
        if (isNotNull(msgId)) {
            setErrOverLocalField(tafj, msgId, application, fieldName);
        }
    }

    private Boolean isNotNull(String s) {
        return !isNull(s);
    }

    private Boolean isNull(String s) {
        return s == null || s.isEmpty();
    }

    /*
     * get Override Message
     */
    public String getOverMess(String overId) {
        String overMess = "";
        logger.info("Start MsbfAaaCIFAMLCasa getOverMess overId:" + overId);
        try {
            OverrideRecord overRec = new OverrideRecord(da.getRecord("", "OVERRIDE", "", overId));
            overMess = overRec.getMessage().get(0).getMessage().get(0).getValue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(exception_var, exception_var);
            logger.error("MsbfAaaCIFAMLCasa getOverMess {}:" + e);
        }
        logger.info("End MsbfAaaCIFAMLCasa getOverMess overId:" + overId);
        return overMess;

    }

    public String hasFieldValue(TField tField) {
        logger.info("Start MsbfCusAMLSironKyc hasFieldValue:");
        return tField == null ? "" : tField.getValue();
        // return java.util.Optional.ofNullable(tField.getValue()).orElse("");

    }

    // public LocalRefList getLocalRefGrp(String checkSum, String fieldName) {
    // logger.info("Start MsbfCusAMLSironKyc getLocalRefGrp:");
    // if (checkSum == null) {
    // return null;
    // }
    // String[] listOfVal = checkSum.split("\\*");
    // LocalRefList retVal = new ArrayList<>();
    // for (int i = 0; i < listOfVal.length; i++) {
    // retVal.getLocalRefField(fieldName).setValue(listOfVal[i]);
    // }
    // return new LocalRefGroup();
    // }
}
