package com.msb.signatures.enquires;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.imdocumentimage.ImDocumentImageRecord;
import com.temenos.t24.api.records.imdocumentupload.ImDocumentUploadRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

/**
 * Date        :- Aug 08, 2023 
 * Organization:- Banktech Software 
 * Description :- It is used to display list of unauthorised image upload records 
 * EB.API      :- MsbfEnqNofileImDocumentDetails 
 * Attached As :- Enquiry Nofile Routine 
 * Attached To :- STANDARD.SELECTION>NOFILE.MSB.IMDOCUMENT.DETAILS
 * ----------------------------------------------------------------------------------------------
 * Modification History
 * ----------------------------------------------------------------------------------------------
 * Date        Description            Reference No.  DeveloperName
 * -----------------------------------------------------------------------------------------------
 * 09-Aug-2023 Initial Routine        MSBCORE-1157   Sharmila K 
 *             developed for JIRA fix
 * 27-Sep-2023 JIRA FIX               MSBCORE-971    Sharmila K   
 * 27-Dec-2023 DuyVV3 Fix Jira DACBINT-2697
 * -----------------------------------------------------------------------------------------------
 */

public class MsbfEnqNofileImDocumentDetails extends Enquiry {
    private List<String> listOfIds = new ArrayList<String>();
    private List<String> listOfIds2  = new ArrayList<String>();
    private List<String> listOfIdsHis = new ArrayList<String>();

    private String filterAndSort = "";
    private String filterAndSort2 = "";

    private String fixedSelection = "";
    private String fixedSelection2 = "";

    private DataAccess da = new DataAccess();
    private Session sn = new Session(this);
    String mnemonic = sn.getCompanyRecord().getMnemonic().getValue();
    private HashMap<Integer, String> map = new HashMap<Integer, String>();
    String mneomonic = sn.getCompanyRecord().getMnemonic().getValue();
    String finalArray = "";
    private List<String> listOfFinalIds = new ArrayList<String>();
    String inputter = null;
    String status = null;
    
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        try {
            String companyCode = sn.getCompanyId();
            
            map.put(1, "EQ");
            map.put(2, "BT");
            map.put(3, "LT");
            map.put(4, "GT");
            map.put(5, "NE");
            map.put(6, "LIKE");
            map.put(8, "LE");
            map.put(9, "GE");
            int sellen = filterCriteria.size();
            for (int i = 0; i < sellen; i++) {
                String fieldName = filterCriteria.get(i).getFieldname();
                String fieldValue = filterCriteria.get(i).getValue();
                String Operand = filterCriteria.get(i).getOperand();
                if (!fieldValue.isEmpty()) {
                    switch (fieldName) {
                    case "ID":
                        filterAndSort = " @ID " + map.get(Integer.parseInt(Operand)) + " " + fieldValue + " ";
                        filterAndSort2 = filterAndSort;
                        break;
                    case "INPUTTER":
                        filterAndSort = " INPUTTER " + map.get(Integer.parseInt(Operand)) + " " + fieldValue + " ";
                        filterAndSort2 = filterAndSort;
                        break;
                    case "IMAGE.REFERENCE":
                        filterAndSort2 = " IMAGE.REFERENCE " + map.get(Integer.parseInt(Operand)) + " "
                                + fieldValue + " " + " ";
                        break;
                    }
                }
            }
            
            fixedSelection = "WITH RECORD.STATUS EQ INAU RNAU AND CO.CODE EQ " + companyCode;
            if (filterAndSort != null && !filterAndSort.isEmpty()) {
                filterAndSort = fixedSelection.concat(" AND ").concat(filterAndSort);
            } else {
                filterAndSort = fixedSelection;
            }         
            logger.info("MsbfEnqNofileImDocumentDetails: filterAndSort = " + filterAndSort);
       
            fixedSelection2 = "WITH CO.CODE EQ " + companyCode;
            if (filterAndSort2 != null && !filterAndSort2.isEmpty()) {
                filterAndSort2 = fixedSelection2.concat(" AND ").concat(filterAndSort2);
            } else {
                filterAndSort2 = fixedSelection2;
            }
            logger.info("MsbfEnqNofileImDocumentDetails: filterAndSort2 = " + filterAndSort2);
            
            listOfIds = da.selectRecords("", "IM.DOCUMENT.UPLOAD", "$NAU", filterAndSort);
            logger.info("MsbfEnqNofileImDocumentDetails: listOfIds size = " + listOfIds.size());
            getNauIds();
            logger.info("MsbfEnqNofileImDocumentDetails: listOfIds size = " + listOfIds.size());
            
            listOfIds2 = da.selectRecords("", "IM.DOCUMENT.IMAGE", "",filterAndSort2);
            logger.info("MsbfEnqNofileImDocumentDetails: listOfIds2 size = " + listOfIds2.size());
            
            listOfIdsHis = da.selectRecords("", "IM.DOCUMENT.IMAGE", "$HIS",filterAndSort2);
            logger.info("MsbfEnqNofileImDocumentDetails: listOfIdsHis size = " + listOfIdsHis.size());
            getHistoryIds();
            logger.info("MsbfEnqNofileImDocumentDetails: listOfIdsHis size = " + listOfIdsHis.size());

            if (listOfIds2 != null && !listOfIds2.isEmpty()) {
                listOfIds.retainAll(listOfIds2);
            }

            if (listOfIds != null) {

                ReadImRecord();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listOfFinalIds;
    }

    
    private void getNauIds() {
        // TODO Auto-generated method stub
        if (listOfIds != null) {            
            String listIds = String.join(" ", listOfIds);
            String filterAndSort1 = "WITH @ID EQ ".concat(listIds);
            if (filterAndSort2 != null && !filterAndSort2.isEmpty()) {
                filterAndSort1 = filterAndSort1.concat(" AND ").concat(filterAndSort2.substring(5));
                listOfIds = da.selectRecords("", "IM.DOCUMENT.IMAGE", "", filterAndSort1);

                // Remove duplicate ids
                listOfIds = removeDuplicates(listOfIds);
            }
        }
    }
    
    private void getHistoryIds() {
        // TODO Auto-generated method stub
        if (listOfIdsHis != null) {
            for (String id : listOfIdsHis) {
                listOfIds2.add(id.split(";")[0]);
            }
            // Remove duplicate ids
            listOfIds2 = removeDuplicates(listOfIds2);
        }
    }

    private void ReadImRecord() {
        // TODO Auto-generated method stub
        for (String recordId : listOfIds) {
            ImDocumentUploadRecord imUploadRec = new ImDocumentUploadRecord(
                    da.getRecord("", "IM.DOCUMENT.UPLOAD", "$NAU", recordId));
            if (imUploadRec != null) {
                status = imUploadRec.getRecordStatus();
                inputter = imUploadRec.getInputter().get(0);
                if (status.contentEquals("RNAU")) {
                    ReadImDocumentImageRecord(recordId);

                } else if (status.contentEquals("INAU")) {
                    ReadImDocumentImageRecord(recordId);
                }
            }
            listOfFinalIds.add(finalArray);
            finalArray = "";
        }
    }

    private void ReadImDocumentImageRecord(String recordId) {
        // TODO Auto-generated method stub
        ImDocumentImageRecord imDocImgRec = null;
        try {
            imDocImgRec = new ImDocumentImageRecord(da.getHistoryRecord("IM.DOCUMENT.IMAGE", recordId));
        } catch (Exception e) {

        }
        if (imDocImgRec == null) {
            try {
                imDocImgRec = new ImDocumentImageRecord(da.getRecord("IM.DOCUMENT.IMAGE", recordId));
            } catch (Exception e) {

            }
        }
        GetFieldValues(imDocImgRec, recordId);
    }

    private void GetFieldValues(ImDocumentImageRecord imDocImgRec, String recordId) {
        try {
            // TODO Auto-generated method stub
            String customerno = "";
            String imageRef = null;
            String type = null;
            String desription = null;
            String shortDesription = null;
            String effDate = null;
            String expDate = null;
            String application = null;
            String multiMediaType = null;            
            AccountRecord accRecObj = null;
            application = imDocImgRec.getImageApplication().getValue();
            if (application.contentEquals("ACCOUNT")) {
                String accountNo = imDocImgRec.getImageReference().getValue();
                try {
                    accRecObj = new AccountRecord(da.getRecord("ACCOUNT", accountNo));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (accRecObj != null) {
                    customerno = accRecObj.getCustomer().getValue();
                }
            } else {
                customerno = imDocImgRec.getImageReference().getValue();
            }
            imageRef = imDocImgRec.getImageReference().getValue();
            type = imDocImgRec.getImageType().getValue();
            desription = imDocImgRec.getDescription().get(0).getValue();
            shortDesription = imDocImgRec.getShortDescription().getValue();
            effDate = imDocImgRec.getLocalRefField("MSB.IM.EFF.DATE").getValue();
            expDate = imDocImgRec.getLocalRefField("MSB.IM.EXPIRY.DATE").getValue();
            multiMediaType = imDocImgRec.getMultiMediaType().getValue();
            finalArray = recordId + "*" + customerno + "*" + application + "*" + imageRef + "*" + type + "*"
                    + desription + "*" + shortDesription + "*" + effDate + "*" + expDate + "*" + status + "*"
                    + inputter + "*" + multiMediaType;

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static <T> List<T> removeDuplicates(List<T> list) {
        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();
        // Add the elements to set
        set.addAll(list);
        // Clear the list
        list.clear();
        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);
        // return the list
        return list;
    }
}