package com.msb.customer.defaultvalues;

import java.util.ArrayList;
import java.util.List;

import com.temenos.api.TStructure;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;
import com.temenos.t24.api.records.customer.LegalIdClass;

/**
 * TODO: Document me!
 *
 * @author dungvv7
 *
 */
public class DefaultLegalVal extends RecordLifecycle {

	Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	@Override
	public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
			TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
		logger.info("DefaultLegalVal start process cif: {}, v1.0.0", currentRecordId);
		// iris always update first legal id
		CustomerRecord currentCus = new CustomerRecord(currentRecord);
		CustomerRecord liveCus = new CustomerRecord(liveRecord);
		if (liveCus.getLegalId().isEmpty() || currentCus.getLegalId().isEmpty()) {
			return;
		}
		List<LegalIdClass> result = new ArrayList<>();
		result.addAll(liveCus.getLegalId());

		// jupiter always update first legal 
		LegalIdClass legalUpdate = currentCus.getLegalId(0);
		// find index by doc name or legal id if found update it and move to first if not add to first
		int index = result.stream()
				.filter(item -> item.getLegalDocName().getValue().equals(legalUpdate.getLegalDocName().getValue())
						|| item.getLegalId().getValue().equals(legalUpdate.getLegalId().getValue()))
				.findFirst().map(result::indexOf).orElse(-1);
		if (index < 0) {
			result.add(0, legalUpdate);
		} else {
			result.remove(index);
			result.add(0, legalUpdate);
		}
		currentCus.clearLegalId();
		for (LegalIdClass legal : result) {
			currentCus.addLegalId(legal);
		}
		currentRecord.set(currentCus.toStructure());
		logger.info("DefaultLegalVal end process cif: {}, v1.0.0", currentRecordId);
	}

}
