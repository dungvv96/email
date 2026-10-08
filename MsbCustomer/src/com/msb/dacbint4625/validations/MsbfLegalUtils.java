package com.msb.dacbint4625.validations;

import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.temenos.tafj.api.client.impl.T24Context;

/**
 * TODO: Document me!
 *
 * @author duyvv3
 *
 */
public class MsbfLegalUtils {
    DataAccess da;
    T24Context t24Context;
    Session session;
    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

    /**
     * @param t24Context
     */
    public MsbfLegalUtils(T24Context t24Context) {
        this.t24Context = t24Context;
        this.da = new DataAccess(t24Context);
        this.session = new Session(t24Context);
    }

    /**
     * @param accountId
     * @return 
     * Empty nếu giấy tờ tùy thân còn hạn sử dụng 
     * Mã lỗi trong bảng OVERRIDE nếu giấy tờ tùy thân hết hạn sử dụng
     */
    public String getDocumentExpStatus(String accountId) {
        String ret = "";
        try {
            AccountRecord acRec = new AccountRecord(da.getRecord("ACCOUNT", accountId));
            String cusId = acRec.getCustomer().getValue();
            
            // Check NOSTRO:
            if (acRec.getLimitRef().getValue().equals("NOSTRO")) {
                return "";
            }
            
            // Get sector and check KHCN or KHDN
            ret = getDocumentExpStatusByCif(cusId);
            return ret;
        } catch (Exception e) {
            logger.error("MsbfLegalUtils().getDocumentExpStatus exception:" + e.getMessage());
        }
        return "";
    }
    
    /**
     * @param customerId
     * @return 
     * Empty nếu giấy tờ tùy thân còn hạn sử dụng 
     * Mã lỗi trong bảng OVERRIDE nếu giấy tờ tùy thân hết hạn sử dụng
     */
    public String getDocumentExpStatusByCif(String customerId) {
        String ret = "";
        try {
            // Get sector and check KHCN or KHDN
            CustomerRecord cusRec = new CustomerRecord(da.getRecord("CUSTOMER", customerId));
            String sector = cusRec.getSector().getValue();
            if (sector.compareTo("1999") > 0 && sector.compareTo("6000") < 0) {
                ret = getDocumentExpStatusByCifKHDN(cusRec);
            } else if (sector.compareTo("1000") >= 0 && sector.compareTo("2000") < 0) {
                ret = getDocumentExpStatusByCifKHCN(cusRec);
            }

            return ret;
        } catch (Exception e) {
            logger.error("MsbfLegalUtils().getDocumentExpStatusByCif exception:" + e.getMessage());
        }
        return "";
    }
    
    /**
     * @param cusRec là KHCN
     * @return 
     * Empty nếu giấy tờ tùy thân còn hạn sử dụng 
     * Mã lỗi trong bảng OVERRIDE nếu giấy tờ tùy thân hết hạn sử dụng
     */
    private String getDocumentExpStatusByCifKHCN(CustomerRecord cusRec) {
        String overrideIdKHCN = "MSB.KHCN.GTTT.EXPIRE.OVE";
        String overrideIdKHCNNone = "MSB.KHCN.GTTT.NONE.OVE";
        String overrideIdKHCNPPVS = "MSB.KHCN.NN.PP.VS.OVE";
        String today = session.getCurrentVariable("!TODAY");
        try {
            String national = cusRec.getNationality().getValue();
            // Nếu KH là người VN
            if (national.equals("VN")) {
                // Kiểm tra IDC:
                for (int i = 0; i < cusRec.getLegalId().size(); i++) {
                    String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                    if (docName.equals("IDC")) {
                        String legalExpDate = cusRec.getLegalId(i).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return overrideIdKHCN;
                        } else {
                            return "";
                        }
                    }
                }

                // Nếu KH không có IDC: Kiểm tra PP
                for (int i = 0; i < cusRec.getLegalId().size(); i++) {
                    String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                    if (docName.equals("PP")) {
                        String legalExpDate = cusRec.getLegalId(i).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return overrideIdKHCN;
                        } else {
                            return "";
                        }
                    }
                }

                // Nếu KH không có IDC, PP: Kiểm tra GKS
                for (int i = 0; i < cusRec.getLegalId().size(); i++) {
                    String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                    if (docName.equals("BC")) {
                        String legalExpDate = cusRec.getLegalId(i).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return overrideIdKHCN;
                        } else {
                            return "";
                        }
                    }
                }

                // Nếu không có IDC,PP,GKS: Không có thông tin GTTT, cần
                // chặn giao dịch
                return overrideIdKHCNNone;
            } else {
                // Nếu KH là người nước ngoài:
                // Kiểm tra PP: Nếu không còn hạn thì chặn
                boolean flagPP = false;
                boolean flagVS = false;
                for (int i = 0; i < cusRec.getLegalId().size(); i++) {
                    String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                    if (docName.equals("PP")) {
                        flagPP = true;
                        String legalExpDate = cusRec.getLegalId(i).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return overrideIdKHCN;
                        }
                    }
                }

                // Kiểm tra VS: Nếu không còn hạn thì chặn
                for (int i = 0; i < cusRec.getLegalId().size(); i++) {
                    String docName = cusRec.getLegalId(i).getLegalDocName().getValue();
                    if (docName.equals("VS")) {
                        flagVS = true;
                        String legalExpDate = cusRec.getLegalId(i).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return overrideIdKHCN;
                        }
                    }
                }

                // Nếu PP và VS còn hạn sử dụng:
                if (flagPP && flagVS) {
                    return "";
                } else {
                    // Nếu không có PP và VS: Chặn giao dịch
                    return overrideIdKHCNPPVS;
                }

            }
            
        } catch (Exception e) {
            logger.error("MsbfLegalUtils().getDocumentExpStatusByCifKHCN exception:" + e.getMessage());
        }

        return "";
    }
    
    /**
     * @param cusRec
     * @return 
     * Empty nếu giấy tờ tùy thân còn hạn sử dụng 
     * Mã lỗi trong bảng OVERRIDE nếu giấy tờ tùy thân hết hạn sử dụng
     */
    private String getDocumentExpStatusByCifKHDN(CustomerRecord cusRec) {
        String overrideIdKHDN95 = "MSB.KHDN.GTTT.EXPIRE.95.OVE";
        String overrideIdKHDN95None = "MSB.KHDN.GTTT.95.NONE.OVE";
        String overrideIdKHDN96 = "MSB.CIF.GTTT.96.EXPIRE";
        String overrideIdKHDN97 = "MSB.KHDN.GTTT.EXPIRE.97.OVE";
        String overrideIdKHDN98 = "MSB.CIF.GTTT.98.EXPIRE";
        String overrideIdKHDNPpVs = "MSB.KHDN.NN.9597.PP.VS.OVE";
        String overrideIdKHDNPpVsOve = "MSB.KHDN.NN.PP.VS.OVE";
        
        String today = session.getCurrentVariable("!TODAY");
        try {
            // Kiểm tra từng relation CIF: Người đại diện pháp luật
            boolean flag95 = false;
            int checkStatus95 = 0;
            for(int i = 0; i < cusRec.getRelationCode().size(); i++){
                String relationCode = cusRec.getRelationCode(i).getRelationCode().getValue();
                String relationCif = cusRec.getRelationCode(i).getRelCustomer().getValue();
                if(relationCode.equals("95")){
                    flag95 = true;
                    CustomerRecord relationCusRec = new CustomerRecord(da.getRecord("CUSTOMER", relationCif));
                    checkStatus95 = getRelationCifExpStatus(relationCusRec, today);
                    if(checkStatus95 == 1){
                        return overrideIdKHDN95;
                    }
                    else if(checkStatus95 == 2){
                        return overrideIdKHDNPpVs;
                    }
                }
            }
            
            // Chặn nếu không có người đại diện PL
            if(!flag95){
                return overrideIdKHDN95None;
            }
            
            // Kiểm tra từng relation CIF: Kế toán trưởng
            for(int i = 0; i < cusRec.getRelationCode().size(); i++){
                String relationCode = cusRec.getRelationCode(i).getRelationCode().getValue();
                String relationCif = cusRec.getRelationCode(i).getRelCustomer().getValue();
                if(relationCode.equals("97")){
                    CustomerRecord relationCusRec = new CustomerRecord(da.getRecord("CUSTOMER", relationCif));
                    int checkStatus97 = getRelationCifExpStatus(relationCusRec, today);
                    if(checkStatus97 == 1){
                        return overrideIdKHDN97;
                    }
                    else if(checkStatus97 == 2){
                        return overrideIdKHDNPpVs;
                    }
                }
            }
            
            // Kiểm tra thông tin người ủy quyền của người đại diện pháp luật
            for(int i = 0; i < cusRec.getRelationCode().size(); i++){
                String relationCode = cusRec.getRelationCode(i).getRelationCode().getValue();
                String relationCif = cusRec.getRelationCode(i).getRelCustomer().getValue();
                if(relationCode.equals("96")){
                    CustomerRecord relationCusRec = new CustomerRecord(da.getRecord("CUSTOMER", relationCif));
                    int checkStatus = getRelationCifExpStatus(relationCusRec, today);
                    if(checkStatus == 1){
                        return overrideIdKHDN96;
                    }
                    else if(checkStatus == 2){
                        return overrideIdKHDNPpVsOve;
                    }
                }
            }
            
            // Kiểm tra thông tin người ủy quyền của KTT
            for(int i = 0; i < cusRec.getRelationCode().size(); i++){
                String relationCode = cusRec.getRelationCode(i).getRelationCode().getValue();
                String relationCif = cusRec.getRelationCode(i).getRelCustomer().getValue();
                if(relationCode.equals("98")){
                    CustomerRecord relationCusRec = new CustomerRecord(da.getRecord("CUSTOMER", relationCif));
                    int checkStatus = getRelationCifExpStatus(relationCusRec, today);
                    if(checkStatus == 1){
                        return overrideIdKHDN98;
                    }
                    else if(checkStatus == 2){
                        return overrideIdKHDNPpVsOve;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("MsbfLegalUtils().getDocumentExpStatusByCifKHDN exception:" + e.getMessage());
        }

        return "";
    }
    
    
    /**
     * @param relationCusRec, today
     * @return 
     * 0 nếu giấy tờ tùy thân còn hạn sử dụng 
     * 1 nếu hết hạn
     * 2 nếu là người NN nhưng thiếu PP và VS
     * 
     */
    private int getRelationCifExpStatus(CustomerRecord relationCusRec, String today) {
        try {
            String national = relationCusRec.getNationality().getValue();
            
            // Nếu người đại diện là VN
            if (national.equals("VN")) {
                // Kiểm tra IDC: 
                for (int j = 0; j < relationCusRec.getLegalId().size(); j++) {
                    String docName = relationCusRec.getLegalId(j).getLegalDocName().getValue();
                    if (docName.equals("IDC")) {
                        String legalExpDate = relationCusRec.getLegalId(j).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return 1;
                        }
                        else{
                            return 0;
                        }
                    }
                }
                
                // Nếu KH không có IDC: Kiểm tra PP
                for (int j = 0; j < relationCusRec.getLegalId().size(); j++) {
                    String docName = relationCusRec.getLegalId(j).getLegalDocName().getValue();
                    if (docName.equals("PP")) {
                        String legalExpDate = relationCusRec.getLegalId(j).getLegalExpDate().getValue();
                        if (today.compareTo(legalExpDate) > 0) {
                            return 1;
                        }
                        else{
                            return 0;
                        }
                    }
                }
                
                // Nếu KH không có IDC và PP: báo lỗi
                return 1;
            }
            else{
                // Nếu người đại diện là người nước ngoài:
                // Kiểm tra PP và VS nếu không tồn tại hoặc hết hạn thì chặn:
                boolean flagPp = false;
                boolean flagVs = false;
                for (int j = 0; j < relationCusRec.getLegalId().size(); j++) {
                    String docName = relationCusRec.getLegalId(j).getLegalDocName().getValue();
                    String legalExpDate = relationCusRec.getLegalId(j).getLegalExpDate().getValue();
                    if (docName.equals("PP")) {
                        flagPp = true;
                        if (today.compareTo(legalExpDate) > 0) {
                            return 1;
                        }
                    }
                    if (docName.equals("VS")) {
                        flagVs = true;
                        if (today.compareTo(legalExpDate) > 0) {
                            return 1;
                        }
                    }
                }
                
                // Nếu không có đủ PP và VS: Chặn giao dịch
                if(!flagPp || !flagVs){
                    return 2;
                }
            }

        } catch (Exception e) {
            logger.error("MsbfLegalUtils.getRelationCifExpStatus exception:" + e.getMessage());
        }

        return 0;
    }
    
    
}
