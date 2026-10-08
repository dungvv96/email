package com.msb.customer.aml.records;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.msb.customer.aml.records.MsbfCusAMLUtil;
import com.msbf.common.utilities.MsbUtils;
import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.api.TStructure;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.crrelationship.CrRelationshipRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.tables.ebmsbhinterfaceparameter.EbMsbhInterfaceParameterRecord;
//import com.temenos.t24.api.tables.msbhparameter.MsbhParameterRecord;

public class MsbUpdateFatca extends RecordLifecycle {

    private static final Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    DataAccess da = new DataAccess(this);

    @Override
    public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
            List<TransactionData> transactionData, List<TStructure> currentRecords) {
        T24RecordUtils t24RecordUtils = new T24RecordUtils(this);
        CrRelationshipRecord crRelRec = new CrRelationshipRecord(currentRecord);
        int sector = Integer.parseInt(crRelRec.getLocalRefField("MSB.SECTOR").getValue());
        EbMsbhInterfaceParameterRecord paramRec = null;
        String requestBody = "";
        if (sector < 2000) {
            paramRec = t24RecordUtils.getRecord("", "EB.MSBH.INTERFACE.PARAMETER", "", "AML.KYC.INDV",
                    EbMsbhInterfaceParameterRecord.class);
            requestBody = buildBodyIndv(paramRec, crRelRec);
            
            logger.info("MsbUpdateFatca requestBody khcn: " + requestBody);
        } else {
            paramRec = t24RecordUtils.getRecord("", "EB.MSBH.INTERFACE.PARAMETER", "", "AML.KYC.CORP",
                    EbMsbhInterfaceParameterRecord.class);
            requestBody = buildBodyCorp(paramRec, crRelRec);
            logger.info("MsbUpdateFatca requestBody khdn: " + requestBody);
        }

        try {
            // begin andn
            String apiKey = MsbUtils.msbGetParameter("KONG.PARAM", "COMMON", "APIKEY", da);
            if (apiKey.isEmpty()) {
                logger.error("{}.updateVAtable() KONG.PARAM API Key is missing",
                        MsbUpdateFatca.class.getName());
                return;
            }
            // end andn
            String data = MsbfCusAMLUtil.post(paramRec.getUrl().getValue(), requestBody, paramRec.getReserved6().getValue(),
                    apiKey);
            if (StringUtils.isNotEmpty(data)) {
                JSONObject response = new JSONObject(data);
                String respCode = "";
                if (sector < 2000) {
                    respCode = response.getJSONObject("updateFatcaPersonal").getJSONObject("respMessage")
                            .getString("respCode");
                } else {
                    respCode = response.getJSONObject("updateFatcaOrganization").getJSONObject("respMessage")
                            .getString("respCode");
                }
                crRelRec.getLocalRefField("MSB.FATCA.RESULT").setValue(respCode);

                TransactionData txnData = new TransactionData();
                txnData.setFunction("INPUT");
                txnData.setVersionId("CR.RELATIONSHIP,AML.OFS.POST");
                txnData.setTransactionId(currentRecordId);
                txnData.setNumberOfAuthoriser("0");
                
                logger.info("MsbUpdateFatca txnData: " + txnData);
                transactionData.add(txnData);
                
                logger.info("MsbUpdateFatca transactionData: " + transactionData);
                currentRecords.add(crRelRec.toStructure());
            }
        } catch (Exception ex) {
            logger.error("MsbUpdateFatca error exception: {}", ex.getMessage());
        }

    }

    private String buildBodyIndv(EbMsbhInterfaceParameterRecord paramRec, CrRelationshipRecord crRelRec) {
        JSONObject authenInfo = MsbfCusAMLUtil.buildAuthenInfo(paramRec);
        JSONObject updateFatcaPersonal = new JSONObject();
        updateFatcaPersonal.put("authenInfo", authenInfo);

        String msbHaveNotFatca = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.HAVE.NOT.FATCA").getValue()) ? "Y"
                : "N";
        String msbUsCitizen = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.CITIZEN").getValue()) ? "Y" : "N";
        String msbUsBirth = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.BIRTH").getValue()) ? "Y" : "N";
        String msbUsCustAddr = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.CUST.ADDR").getValue()) ? "Y"
                : "N";
        String msbUsPhoneNo = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.PH.NO").getValue()) ? "Y" : "N";
        String msbUsRegFix = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.REG.FIX").getValue()) ? "Y" : "N";
        String msbUsAttorneyAddr = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.ATTORNEY.ADDR").getValue())
                ? "Y" : "N";
        String msbUsMailPermAddr = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.US.MAIL.PERM.ADDR").getValue())
                ? "Y" : "N";
        String msbCommitForm = "Y".equalsIgnoreCase(crRelRec.getLocalRefField("MSB.COMMT.FORM").getValue()) ? "Y" : "N";
        String kycNumber = crRelRec.getLocalRefField("MSB.AML.REFERENCE").getValue();

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
            
        JSONObject requestBody = new JSONObject();
        requestBody.put("updateFatcaPersonal", updateFatcaPersonal);
        logger.info("MsbUpdateFatca requestBody: " + requestBody);
        return requestBody.toString();
    }

    private String buildBodyCorp(EbMsbhInterfaceParameterRecord paramRec, CrRelationshipRecord crRelRec) {
        JSONObject authenInfo = MsbfCusAMLUtil.buildAuthenInfo(paramRec);
        JSONObject updateFatcaOrganization = new JSONObject();
        updateFatcaOrganization.put("authenInfo", authenInfo);

        updateFatcaOrganization.put("msbCorpType", crRelRec.getLocalRefField("MSB.CORP.TYPE").getValue());
        updateFatcaOrganization.put("msbRemark1", crRelRec.getLocalRefField("MSB.REMARK.1").getValue());
        updateFatcaOrganization.put("msbDebtDoc1", crRelRec.getLocalRefField("MSB.DEBT.DOC.1").getValue());
        updateFatcaOrganization.put("msbConfCompPer1", crRelRec.getLocalRefField("MSB.CONF.COMP.PER.1").getValue());
        updateFatcaOrganization.put("msbOrgExmpt2", crRelRec.getLocalRefField("MSB.ORG.EXMPT.2").getValue());
        updateFatcaOrganization.put("msbFinCorp3", crRelRec.getLocalRefField("MSB.FIN.CORP.3").getValue());
        updateFatcaOrganization.put("msbGiinCode", crRelRec.getLocalRefField("MSB.GIIN.CODE").getValue());
        updateFatcaOrganization.put("msbFixExmpt34", crRelRec.getLocalRefField("MSB.FIX.EXMPT.3.4").getValue());
        updateFatcaOrganization.put("msbUsNonFin4", crRelRec.getLocalRefField("MSB.US.NON.FIN.4").getValue());
        updateFatcaOrganization.put("msbNonFinOrg4", crRelRec.getLocalRefField("MSB.NON.FIN.ORG.4").getValue());
        updateFatcaOrganization.put("msbNote", crRelRec.getLocalRefField("MSB.NOTE").getValue());
        updateFatcaOrganization.put("msbDissidentOrg5", crRelRec.getLocalRefField("MSB.DISSIDENT.ORG.5").getValue());
        updateFatcaOrganization.put("kycNumber", crRelRec.getLocalRefField("MSB.AML.REFERENCE").getValue());
        JSONObject requestBody = new JSONObject();
        requestBody.put("updateFatcaOrganization", updateFatcaOrganization);
        logger.info("MsbUpdateFatca requestBody KHDN: " + requestBody);
        return requestBody.toString();
    }

}
