package com.msb.customer.enquires;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.msb.customer.model.RelCustResult;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.tafj.api.client.TAFJRuntime;
import com.temenos.tafj.api.client.impl.TAFJRuntimeFactory;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;

/**
 * This class handles enquiries related to customer relations. It processes
 * filter criteria and calls an Oracle stored function to retrieve relevant
 * customer relation records.
 */
public class RelationCustomerEnquiry extends Enquiry {

    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");
    DataAccess da = new DataAccess(this);
    String customerId = "";

    @Override
    public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
        List<RelCustResult> result = new ArrayList<>();
        TAFJRuntime tafjRuntime = TAFJRuntimeFactory.getTAFJRuntime(this);
        List<String> signRepIds = new ArrayList<String>();
        logger.info("RelationCustomerEnquiry START");

        try (Connection conn = tafjRuntime.getConnectionAPIDB();
                CallableStatement cs = conn
                        .prepareCall("{ ? = call msb_customer_pkg.get_relation_customer_data(?) }")) {

            cs.registerOutParameter(1, java.sql.Types.CLOB);
            String filterClause = processFilterCriteria(filterCriteria);
            cs.setString(2, filterClause);
            cs.execute();

            Clob resultClob = cs.getClob(1);
            String jsonStr = resultClob.getSubString(1, (int) resultClob.length());
            JSONArray jsonArray = new JSONArray(jsonStr);
            
            // Doc thong tin Id nguoi dai dien
            String sector = "";
            logger.info("RelationCustomerEnquiry: customerId = " + customerId);
            if(jsonArray.length() > 0){
                CustomerRecord cusRec = new CustomerRecord(da.getRecord("CUSTOMER", customerId));
                sector = cusRec.getSector().getValue();
                int repSignCount = cusRec.getLocalRefGroups("MSB.SIGN.REP").size();
                logger.info("RelationCustomerEnquiry: sector = " + sector);
                logger.info("RelationCustomerEnquiry: repSignCount = " + repSignCount);
                if(repSignCount > 0){
                    for(int i = 0; i < repSignCount; i++){
                        String repId = cusRec.getLocalRefGroups("MSB.SIGN.REP").get(i).getLocalRefField("MSB.SIGN.REP").getValue();
                        signRepIds.add(repId);
                        logger.info("RelationCustomerEnquiry: repId = " + repId);
                    }
                }
            }
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                RelCustResult relcust = new RelCustResult();
                relcust.setCustomer(jsonObject.getString("CUSTOMER"));
                relcust.setIsrelation(jsonObject.getString("IS_RELATION"));
                
                String ofCustomer = jsonObject.getString("OF_CUSTOMER");
                relcust.setOfcustomer(ofCustomer);
                if(signRepIds.contains(ofCustomer) && sector.compareTo("2000") >=0){
                    logger.info("RelationCustomerEnquiry: ofCustomer = " + ofCustomer);
                    relcust.setRepId(ofCustomer);
                }
                else{
                    relcust.setRepId("");
                }
                
                result.add(relcust);
            }

        } catch (SQLException e) {
            logger.error("Error in RelationCustomerEnquiry: ", e);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            // Uncomment and replace with appropriate logger
            // LOGGER.error(e, e);
            logger.error("RelationCustomerEnquiry Error close connection: {}", ex);
        }

        return result.stream().map(RelCustResult::toString).collect(Collectors.toList());
    }

    private String processFilterCriteria(List<FilterCriteria> filterCriteria) {
        StringBuilder params = new StringBuilder();
        // OPERAND EQ: 1, BETWEEN: 2, LESS THAN: 3, GREATER THAN: 4
        for (FilterCriteria fc : filterCriteria) {
            switch (fc.getFieldname()) {
            case "R.DATA":
                break;
            case "F.CUSTOMER":
                params.append(" AND RECID IN (").append(fc.getValue().replaceAll("\\s+", ",")).append(")");
                customerId = fc.getValue().replaceAll("\\s+", ",");
                break;
            case "F.ISRELATION":
                if (fc.getOperand().equals("1")) {
                    params.append(" AND MIS_RELATION IN (").append(fc.getValue().replaceAll("\\s+", ",")).append(")");
                } else if (fc.getOperand().equals("2")) {
                    String[] value = fc.getValue().split(" ");
                    if (value != null && value.length == 2) {
                        params.append(" AND MIS_RELATION BETWEEN ").append(value[0]).append(" AND ").append(value[1]);
                    }
                } else if (fc.getOperand().equals("3")) {
                    params.append(" AND MIS_RELATION < ").append(fc.getValue());
                } else if (fc.getOperand().equals("4")) {
                    params.append(" AND MIS_RELATION > ").append(fc.getValue());
                }

                break;
            case "F.OFCUSTOMER":
                params.append(" AND MOF_CUSTOMER IN (").append(fc.getValue().replaceAll("\\s+", ",")).append(")");
                break;
            default:
                logger.warn("Unexpected filter criteria: " + fc.getFieldname());
            }
        }

        return params.toString();
    }
}