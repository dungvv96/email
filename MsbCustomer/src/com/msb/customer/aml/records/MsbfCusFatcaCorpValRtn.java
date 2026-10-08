package com.msb.customer.aml.records;

import org.apache.commons.lang3.StringUtils;

import com.temenos.api.TStructure;
import com.temenos.api.TValidationResponse;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.customer.CustomerRecord;

public class MsbfCusFatcaCorpValRtn extends RecordLifecycle {

	@Override
	public TValidationResponse validateRecord(String application, String currentRecordId, TStructure currentRecord,
			TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {
		CustomerRecord cusRec = new CustomerRecord(currentRecord);
		String corpValue = cusRec.getLocalRefField("MSB.CORP.TYPE").getValue();
		String remark1 = cusRec.getLocalRefField("MSB.REMARK.1").getValue();
		String debitDoc1 = cusRec.getLocalRefField("MSB.DEBT.DOC.1").getValue();
		String debitDoc2 = cusRec.getLocalRefField("MSB.DEBT.DOC.2").getValue();
		String debitDoc4 = cusRec.getLocalRefField("MSB.DEBT.DOC.4").getValue();
		String finCorp3 = cusRec.getLocalRefField("MSB.FIN.CORP.3").getValue();
		String orgExmpt2 = cusRec.getLocalRefField("MSB.ORG.EXMPT.2").getValue();
		String confCompPer1 = cusRec.getLocalRefField("MSB.CONF.COMP.PER.1").getValue();
		String fixExemption34 = cusRec.getLocalRefField("MSB.FIX.EXMPT.3.4").getValue();
		String debitDoc34 = cusRec.getLocalRefField("MSB.DEBT.DOC.3.4").getValue();
		String usnonfinOrg4 = cusRec.getLocalRefField("MSB.US.NON.FIN.4").getValue();
		String nonfinOrg4 = cusRec.getLocalRefField("MSB.NON.FIN.ORG.4").getValue();
		String dissidentOrg5 = cusRec.getLocalRefField("MSB.DISSIDENT.ORG.5").getValue();
		String msbnote = cusRec.getLocalRefField("MSB.NOTE").getValue();
		String ginnCode = cusRec.getLocalRefField("MSB.GIIN.CODE").getValue();
		if (!corpValue.contentEquals("1") && remark1 != null && !remark1.isEmpty())
			cusRec.getLocalRefField("MSB.REMARK.1").setError("EB-MSB.CR.INP.NOT.ALLOWED");
		if (!corpValue.contentEquals("1") && debitDoc1 != null && !debitDoc1.isEmpty())
			cusRec.getLocalRefField("MSB.DEBT.DOC.1").setError("EB-MSB.CR.INP.NOT.ALLOWED");
		if (corpValue.contentEquals("1") && StringUtils.isEmpty(confCompPer1) && StringUtils.isEmpty(debitDoc1))
			cusRec.getLocalRefField("MSB.CONF.COMP.PER.1").setError(
					"This field or Khách hàng nợ hồ sơ (Nếu chọn giá trị là 01) is mandatory when corporate type is 01");
		if (!corpValue.contentEquals("1") && confCompPer1 != null && !confCompPer1.isEmpty())
			cusRec.getLocalRefField("MSB.CONF.COMP.PER.1").setError("EB-MSB.CR.INP.NOT.ALLOWED");
		if (!corpValue.contentEquals("2") && orgExmpt2 != null && !orgExmpt2.isEmpty())
			cusRec.getLocalRefField("MSB.ORG.EXMPT.2").setError("EB-MSB.CR.INP.NOT.ALLOWED2");
		if (!corpValue.contentEquals("2") && debitDoc2 != null && !debitDoc2.isEmpty())
			cusRec.getLocalRefField("MSB.DEBT.DOC.2").setError("EB-MSB.CR.INP.NOT.ALLOWED2");
		if (!corpValue.contentEquals("3") && finCorp3 != null && !finCorp3.isEmpty())
			cusRec.getLocalRefField("MSB.FIN.CORP.3").setError("EB-MSB.CR.INP.NOT.ALLOWED3");
		if ((finCorp3.equals("4") || finCorp3.equals("5")) && !ginnCode.isEmpty() && ginnCode != null)
			cusRec.getLocalRefField("MSB.GIIN.CODE").setError("EB-MSB.CR.INP.ALLW.FINCORP1");
		if ((finCorp3.equals("2") || finCorp3.equals("3")) && ginnCode.isEmpty())
			cusRec.getLocalRefField("MSB.GIIN.CODE").setError("EB-MSB.CR.INP.MAND.FINCORP2");
		if (finCorp3.equals("4")) {
			if (fixExemption34.equals("") && fixExemption34.isEmpty())
				cusRec.getLocalRefField("MSB.FIX.EXMPT.3.4").setError("EB-MSB.CR.INP.MAND.FINCORP");
		} else if (!debitDoc34.equals("") && !debitDoc34.isEmpty()) {
			cusRec.getLocalRefField("MSB.DEBT.DOC.3.4").setError("EB-MSB.CR.INP.ALLW.FINCORP");
		}
		if (!corpValue.contentEquals("4") && usnonfinOrg4 != null && !usnonfinOrg4.isEmpty())
			cusRec.getLocalRefField("MSB.US.NON.FIN.4").setError("EB-MSB.CR.INP.NOT.ALLOWED4");
		if (corpValue.contentEquals("4") && StringUtils.isEmpty(usnonfinOrg4))
			cusRec.getLocalRefField("MSB.US.NON.FIN.4").setError("EB-MSB.CR.INP.MAND4");
		if (!corpValue.contentEquals("4") && debitDoc4 != null && !debitDoc4.isEmpty())
			cusRec.getLocalRefField("MSB.DEBT.DOC.4").setError("EB-MSB.CR.INP.NOT.ALLOWED4");
		if (corpValue.contentEquals("4") && usnonfinOrg4.contentEquals("1") && nonfinOrg4.isEmpty()
				&& StringUtils.isEmpty(debitDoc4))
			cusRec.getLocalRefField("MSB.NON.FIN.ORG.4").setError(
					"This field is mandatory when corporate type is set to 04 and and US non financial org is set to Co Cshhl My");
		if (corpValue.contentEquals("5") && dissidentOrg5.isEmpty())
			cusRec.getLocalRefField("MSB.DISSIDENT.ORG.5").setError("EB-MSB.CR.INP.MAND5");
		if (!corpValue.contentEquals("5") && dissidentOrg5 != null && !dissidentOrg5.isEmpty())
			cusRec.getLocalRefField("MSB.DISSIDENT.ORG.5").setError("EB-MSB.CR.INP.NOT.ALLOWED5");
		if (nonfinOrg4.equalsIgnoreCase("Passive") && msbnote.isEmpty())
			cusRec.getLocalRefField("MSB.NOTE").setError("EB-MSB.CR.INP.MAND6");
		return cusRec.getValidationResponse();
	}
}
