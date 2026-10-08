package com.msb.customer.defaultvalues;

import java.util.HashMap;
import java.util.Map;

import com.msb.customer.models.MsbAdditionalAddressClass;
import com.msbf.common.utilities.MsbUtils;
import com.temenos.api.LocalRefGroup;
import com.temenos.api.TStructure;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

/**
 *
 * @author hieunt35
 * 
 *         update customer address from srv iris put method without ingore
 *         replace indicator
 * 
 */
public class CustAddrDefaultValue extends RecordLifecycle {

    Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

    // This routine updates customer address value
    // CUSTOMER>MSB.ADDR.TYPE
    // CUSTOMER>MSB.STREET
    // cases:

    // - case 1: KHCN -> MSB.ADDR.TYPE=TT MSB.STREET=customerAddress1
    // - case 2: KHDN -> MSB.ADDR.TYPE=KD MSB.STREET=customerAddress1

    // - case 3: KHCN -> MSB.ADDR.TYPE=LL MSB.STREET=customerAddress2
    // - case 4: KHDN -> MSB.ADDR.TYPE=GD MSB.STREET=customerAddress2

    // ADDRESS.COUNTRY core field
    // MSB.PROV.CITY shortName MSB.PROVI.CITY

    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        // new record
        CustomerRecord newRec = new CustomerRecord(currentRecord);

        // old record
        CustomerRecord updtRec = new CustomerRecord(liveRecord);

        // on old record, set MSB.ADDR.TYPE field value to new addr type

        // WORK WITH MANDATORY MSB.ADDR.TYPE GROUP

        // DO NOT UPDATE MSB.ADDR.TYPE VALUE
        // updtRec.getLocalRefField("MSB.ADDR.TYPE").setValue(newRec.getLocalRefField("MSB.ADDR.TYPE")
        // != null
        // ? newRec.getLocalRefField("MSB.ADDR.TYPE").getValue() : "");

        // if value of ADDRESS.COUNTRY field exists on new record, then set it
        // to old record
        if (!newRec.getAddressCountry().getValue().isEmpty())
            updtRec.setAddressCountry(newRec.getAddressCountry());

        // if value of MSB.PROV.CITY local field exists on new record, then set
        // it to old record
        if (newRec.getLocalRefField("MSB.PROV.CITY") != null
                && !newRec.getLocalRefField("MSB.PROV.CITY").getValue().isEmpty()) {
            updtRec.getLocalRefField("MSB.PROV.CITY").setValue(newRec.getLocalRefField("MSB.PROV.CITY").getValue());
        }

        // if value of MSB.STREET local field exists on new record, then CLEAR
        // value of MSB.STREET on old record

        if (newRec.getLocalRefGroups("MSB.STREET") != null && !newRec.getLocalRefGroups("MSB.STREET").isEmpty()) {
            updtRec.getLocalRefGroups("MSB.STREET").clear();

            // update value of MSB.STREET field on old record
            for (LocalRefGroup lrg : newRec.getLocalRefGroups("MSB.STREET")) {

                LocalRefGroup uLRG = updtRec.createLocalRefGroup("MSB.STREET");

                uLRG.getLocalRefField("MSB.STREET").setValue(lrg.getLocalRefField("MSB.STREET").getValue());

                updtRec.getLocalRefGroups("MSB.STREET").add(uLRG);
            }
        }
        // DONE WITH MANDATORY MSB.ADDR.TYPE GROUP

        // if value of MSB.ADDRESS.TYPE local field exists on new record, it
        // means
        // that user wants to update something in this group of values
        // BEGIN WITH NON-MADATORY MSB.ADD.TYPE GROUP
        if (newRec.getLocalRefGroups("MSB.ADDRESS.TYPE") != null
                && !newRec.getLocalRefGroups("MSB.ADDRESS.TYPE").isEmpty()) {
            // - case 1: KHCN -> MSB.ADDR.TYPE=TT MSB.STREET=customerAddress1
            // - case 2: KHDN -> MSB.ADDR.TYPE=KD MSB.STREET=customerAddress1

            // - case 3: KHCN -> MSB.ADDR.TYPE=LL MSB.STREET=customerAddress2
            // - case 4: KHDN -> MSB.ADDR.TYPE=GD MSB.STREET=customerAddress2

            // these are sub fields of MSB.ADDRESS.TYPE
            // - MSB.ADDRESS.TYPE shortName MSB.ADD.TYPE
            // - MSB.ADDRESS.COUNTRY shortName MSB.ADD.COUNTRY
            // - MSB.PROVINCE.CITY shortName MSB.PROV.CITY
            // - MSB.ADDRESS.STREET shortName MSB.STREET.1
            // - MSB.STREET.2 shortName MSB.STREET.2

            Map<String, MsbAdditionalAddressClass> addAddrMap = new HashMap<String, MsbAdditionalAddressClass>();

            // put old data to map
            // iterate throu group values of MSB.ADDRESS.TYPE on old record
            for (LocalRefGroup uLrg : updtRec.getLocalRefGroups("MSB.ADDRESS.TYPE")) {

                String uAddrType = uLrg.getLocalRefField("MSB.ADDRESS.TYPE") != null
                        ? uLrg.getLocalRefField("MSB.ADDRESS.TYPE").getValue() : "";

                String uAddrStreet1 = uLrg.getLocalRefField("MSB.ADDRESS.STREET") != null
                        ? uLrg.getLocalRefField("MSB.ADDRESS.STREET").getValue() : "";
                String uAddrStreet2 = uLrg.getLocalRefField("MSB.STREET.2") != null
                        ? uLrg.getLocalRefField("MSB.STREET.2").getValue() : "";

                // TEMPORARY CLEAR LINE 2 (MSB.STREET.2 - uAddrStreet2) IF ADDRESS TYPE IS "LL"
                // OR "KD"
                if (uAddrType.equalsIgnoreCase("LL") || uAddrType.equalsIgnoreCase("GD")) {
                    addAddrMap.put(uAddrType, new MsbAdditionalAddressClass(uAddrType, uAddrStreet1, ""));
                } else {
                    addAddrMap.put(uAddrType, new MsbAdditionalAddressClass(uAddrType, uAddrStreet1, uAddrStreet2));
                }
            }

            // MAP SAMPLE 1
            // TT - MsbAdditionalAddressClass
            // LL - MsbAdditionalAddressClass
            // ...

            // MAP SAMPLE 2
            // KD - MsbAdditionalAddressClass
            // GD - MsbAdditionalAddressClass
            // ...

            // put new data to map if existed then update map entry
            // iterate throu group values of MSB.ADDRESS.TYPE on new record
            // for (LocalRefGroup nLrg :
            // newRec.getLocalRefGroups("MSB.ADDRESS.TYPE")) {

            // get first element only
            LocalRefGroup nLrg = newRec.getLocalRefGroups("MSB.ADDRESS.TYPE").get(0);

            // String nAddrType = nLrg.getLocalRefField("MSB.ADDRESS.TYPE")
            // != null
            // ? nLrg.getLocalRefField("MSB.ADDRESS.TYPE").getValue() : "";

            // get input values from new record
            String nCusAddr1 = nLrg.getLocalRefField("MSB.ADDRESS.STREET") != null
                    ? nLrg.getLocalRefField("MSB.ADDRESS.STREET").getValue() : "";

            String nCusAddr2 = nLrg.getLocalRefField("MSB.STREET.2") != null
                    ? nLrg.getLocalRefField("MSB.STREET.2").getValue() : "";

            logger.debug("CustAddrDefaultValue.defaultFieldValues(), customerAddress1 = {}", nCusAddr1);
            logger.debug("CustAddrDefaultValue.defaultFieldValues(), customerAddress2 = {}", nCusAddr2);

            // NEW ALGO - 30th Sep

            /*
             * Get sector from old record. If sector in (1000-1999) then
             * addrType = LL (indi). If sector in (2000-* ) then addrType - GD
             * (corp)
             */

            String sector = updtRec.getSector().getValue();

            if (MsbUtils.isIndiCustomer(sector)) {
                // KHCN

                // get LL entry from map and set new value
                MsbAdditionalAddressClass llGrp = addAddrMap.get("LL");
                if (llGrp != null) {
                    llGrp.setAddressStreet1(nCusAddr1);
                    llGrp.setAddressStreet2(nCusAddr2);
                } else {
                    addAddrMap.put("LL", new MsbAdditionalAddressClass("LL", nCusAddr1, nCusAddr2));
                }

            } else {
                // KHDN

                // get LL entry from map and set new value
                MsbAdditionalAddressClass gdGrp = addAddrMap.get("GD");
                if (gdGrp != null) {
                    gdGrp.setAddressStreet1(nCusAddr1);
                    gdGrp.setAddressStreet2(nCusAddr2);
                } else {
                    addAddrMap.put("GD", new MsbAdditionalAddressClass("GD", nCusAddr1, nCusAddr2));
                }
            }

            /* clear old address data */
            updtRec.getLocalRefGroups("MSB.ADDRESS.TYPE").clear();

            /* set updated map values to old record */
            for (Map.Entry<String, MsbAdditionalAddressClass> entry : addAddrMap.entrySet()) {
                if (entry.getValue().getAddressStreet1().isEmpty() && entry.getValue().getAddressStreet2().isEmpty()) {
                    continue;
                }

                LocalRefGroup aLRG = updtRec.createLocalRefGroup("MSB.ADDRESS.TYPE");

                aLRG.getLocalRefField("MSB.ADDRESS.TYPE").setValue(entry.getValue().getAddressType());
                aLRG.getLocalRefField("MSB.ADDRESS.STREET").setValue(entry.getValue().getAddressStreet1());
                aLRG.getLocalRefField("MSB.STREET.2").setValue(entry.getValue().getAddressStreet2());

                updtRec.getLocalRefGroups("MSB.ADDRESS.TYPE").add(aLRG);
            }
        }
        // DONE WITH NON-MADATORY MSB.ADD.TYPE GROUP

        // WORK WITH MSB.CUST.REMARK
        updtRec.getLocalRefGroups("MSB.CUST.REMARK").clear();
        for (LocalRefGroup lrg : newRec.getLocalRefGroups("MSB.CUST.REMARK")) {

            LocalRefGroup uLRG = updtRec.createLocalRefGroup("MSB.CUST.REMARK");

            uLRG.getLocalRefField("MSB.CUST.REMARK").setValue(lrg.getLocalRefField("MSB.CUST.REMARK") != null
                    ? lrg.getLocalRefField("MSB.CUST.REMARK").getValue() : "");

            updtRec.getLocalRefGroups("MSB.CUST.REMARK").add(uLRG);
        }
        // DONE WITH MSB.CUST.REMARK

        currentRecord.set(updtRec.toStructure());
    }

}
