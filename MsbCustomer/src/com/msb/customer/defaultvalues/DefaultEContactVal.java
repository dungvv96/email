package com.msb.customer.defaultvalues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temenos.api.TStructure;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.complex.eb.templatehook.TransactionData;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.ContactTypeClass;
import com.temenos.t24.api.records.customer.CustomerRecord;

/**
 * TODO: Document me!
 *
 * @author dungvv7
 *
 */
public class DefaultEContactVal extends RecordLifecycle {

	Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	private final static List<String> TYPE_PHONE = Arrays.asList("MOBILE", "PHONE", "HOME", "OFFICE");

	@Override
	public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
			TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
		logger.info("DefaultEContractVal start process cif: {}, v1.0.0", currentRecordId);
		CustomerRecord currentCus = new CustomerRecord(currentRecord);
		CustomerRecord liveCus = new CustomerRecord(liveRecord);
		if (liveCus.getContactType().isEmpty()) {
			// live record not exists contact check prefix and default value
			for (ContactTypeClass ct : currentCus.getContactType()) {
				String contactType = ct.getContactType().getValue();
				if (TYPE_PHONE.stream().anyMatch(x -> x.equals(contactType))
						&& ct.getIddPrefixPhone().getValue().isEmpty()) {
					ct.setIddPrefixPhone("+84");
				}
			}
			logger.info("DefaultEContractVal live contact is null");
			currentRecord.set(currentCus.toStructure());
			return;
		}
		if (currentCus.getContactType().isEmpty()) {
			return;
		}
		List<ContactTypeClass> resultContact = new ArrayList<>();
		resultContact.addAll(liveCus.getContactType());
		List<ContactTypeClass> currentContact = currentCus.getContactType();

		// get first contact update in current record and update to live
		Map<String, ContactTypeClass> updateContact = new HashMap<>();
		for (ContactTypeClass ct : currentContact) {
			if (!updateContact.containsKey(ct.getContactType().getValue())) {
				updateContact.put(ct.getContactType().getValue(), ct);
			}
		}
		for (ContactTypeClass ct : resultContact) {
			if (updateContact.containsKey(ct.getContactType().getValue())) {
				ContactTypeClass update = updateContact.get(ct.getContactType().getValue());
				logger.info("DefaultEContractVal update: {}", update.toString());
				ct.setContactData(update.getContactData());
				if (TYPE_PHONE.stream().anyMatch(x -> x.equals(ct.getContactType().getValue()))) {
					if (ct.getIddPrefixPhone().getValue().isEmpty()) {
						ct.setIddPrefixPhone("+84");
					}
				} else {
					ct.setIddPrefixPhone("");
				}
				updateContact.remove(ct.getContactType().getValue());
			}
		}
		// if list update not exists in live record add contact new
		if (!updateContact.isEmpty()) {
			for (ContactTypeClass ct : updateContact.values()) {
				logger.info("DefaultEContractVal add new contact: {}", ct.toString());
				if (TYPE_PHONE.stream().anyMatch(x -> x.equals(ct.getContactType().getValue()))
						&& ct.getIddPrefixPhone().getValue().isEmpty()) {
					ct.setIddPrefixPhone("+84");
				}
				resultContact.add(ct);
			}
		}

		// clear currentContact and set new list updated
		currentCus.clearContactType();
		for (ContactTypeClass ct : resultContact) {
			currentCus.addContactType(ct);
		}
		currentRecord.set(currentCus.toStructure());
		logger.info("DefaultEContractVal is success");
	}

	@Override
	public void updateRecord(String application, String currentRecordId, TStructure currentRecord,
			TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext,
			List<TransactionData> transactionData, List<TStructure> currentRecords) {
		// not use
	}

}
