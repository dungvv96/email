package com.msb.signatures.enquires;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
//import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.msb.customer.model.ImDocImageSortCif;
import com.msbf.common.utilities.T24RecordUtils;
import com.temenos.logging.facade.Logger;
import com.temenos.logging.facade.LoggerFactory;
import com.temenos.t24.api.complex.eb.enquiryhook.EnquiryContext;
import com.temenos.t24.api.complex.eb.enquiryhook.FilterCriteria;
import com.temenos.t24.api.hook.system.Enquiry;
import com.temenos.t24.api.records.aaarrangement.AaArrangementRecord;
import com.temenos.t24.api.records.aaarrangement.CustomerClass;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.imdocumentimage.ImDocumentImageRecord;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;

public class AuthImDocumentImageEnquiry extends Enquiry {

	private static final String MSB_IM_EXPIRY_DATE = "MSB.IM.EXPIRY.DATE";

	private static final String MSB_IM_EFF_DATE = "MSB.IM.EFF.DATE";

	private static final Logger logger = LoggerFactory.getLogger("LOCAL_DEV");

	@Override
	public List<String> setIds(List<FilterCriteria> filterCriteria, EnquiryContext enquiryContext) {
		DataAccess da = new DataAccess(this);
		Session session = new Session(this);
		T24RecordUtils t24RecordUtils = new T24RecordUtils(this);
		String toDay = session.getCurrentVariable("!TODAY");
		String filterDate = convertToDate6(toDay) + "...";
        String userInpId = session.getUserId();
        UserRecord usrRecord = new UserRecord(da.getRecord("USER", userInpId));
        String userHub = usrRecord.getLocalRefField("MSB.HUB").getValue();		
        
		boolean isSearchToDay = true;
		boolean isSearchInpHubEQ = true;
		boolean isSearchUserIdLK = false;
		boolean isSearchUserIdEQ = false;
		String fUserId = "";
		List<String> result = new ArrayList<>();

		StringBuilder filter = new StringBuilder("");
//		filter.append(" AND CO.CODE EQ ");
//		filter.append(session.getCompanyId());
		if (enquiryContext.getEnquiryId().equalsIgnoreCase("MSBF.ENQ.AUTH.IM.DOC.IMG")) {
		    filter.append(" WITH IMAGE.TYPE EQ SIGNATURES ");
		    filter.append(" AND MULTI.MEDIA.TYPE EQ 'IMAGE'");
        } else if (enquiryContext.getEnquiryId().equalsIgnoreCase("MSBF.ENQ.AUTH.IM.DOC")){
            filter.append(" WITH IMAGE.TYPE EQ DOCUMENTS ");
            filter.append(" AND MULTI.MEDIA.TYPE EQ 'DOCUMENT'");
        }
		for (FilterCriteria fc : filterCriteria) {
			switch (fc.getFieldname()) {
			case "R.DATA":
				break;
			case "F.IM.REF":
				if (fc.getOperand().equals("1")) {
					filter.append(" AND IMAGE.REFERENCE EQ ");
					filter.append(fc.getValue());
				} else if (fc.getOperand().equals("2")) {
					String[] value = fc.getValue().split(" ");
					if (value != null) {
						if (value.length != 2) {
							return result;
						} else {
							filter.append(" AND IMAGE.REFERENCE GE ");
							filter.append(value[0]);
							filter.append(" AND IMAGE.REFERENCE LE ");
							filter.append(value[1]);
						}
					}

				}
				isSearchToDay = false;
				break;
//			case "F.IM.DATE":
//				if (fc.getOperand().equals("1")) {
//					if (!isValidDate(fc.getValue(), toDay)) {
//						result.add("ERROR");
//						return result;
//					}
//					filter.append(" AND DATE.TIME LIKE ");
//					filter.append(convertToDate6(fc.getValue()) + "...");
//				} else if (fc.getOperand().equals("2")) {
//					String[] value = fc.getValue().split(" ");
//					if (value != null) {
//						if (value.length != 2) {
//							return result;
//						} else {
//							if (!isValidDate(value[0], toDay) || !isValidDate(value[0], value[1])) {
//								result.add("ERROR");
//								return result;
//							}
//							filter.append(" AND DATE.TIME GE ");
//							filter.append(convertToDate6(value[0]) + "0000");
//							filter.append(" AND DATE.TIME LE ");
//							filter.append(convertToDate6(value[1]) + "2359");
//						}
//					}
//				}
			case "F.USER.ID":
				if (fc.getOperand().equals("1")) {
					isSearchUserIdEQ = true;
				} else if (fc.getOperand().equals("6")) {
					isSearchUserIdLK = true;
				}
				fUserId = fc.getValue();
				isSearchToDay = false;
				break;
			case "F.CURR.NO":
				if ("new".equalsIgnoreCase(fc.getValue())) {
					filter.append(" AND CURR.NO EQ 1 ");
				} else if ("amend".equalsIgnoreCase(fc.getValue())) {
					filter.append(" AND CURR.NO GT 1 ");
				} else if (!"".equals(fc.getValue())) {
					return result;
				}
				isSearchToDay = false;
				break;				
            case "F.INPUT.HUB":
                if (fc.getOperand().equals("")) {
                    filter.append(" AND MSB.INPUT.HUB EQ ");
                    filter.append(userHub);
                } else {
                    filter.append(" AND MSB.INPUT.HUB EQ ");
                    filter.append(fc.getValue());
                }
                //fUserId = fc.getValue();
                isSearchToDay = false;
                isSearchInpHubEQ = false;
                break;	
            case "F.IM.DATE":
              if (fc.getOperand().equals("1")) {
                  if (!isValidDate(fc.getValue(), toDay)) {
                      result.add("ERROR");
                      return result;
                  }
                  filter.append(" AND DATE.TIME LIKE ");
                  filter.append(convertToDate6(fc.getValue()) + "...");
              } else if (fc.getOperand().equals("2")) {
                  String[] value = fc.getValue().split(" ");
                  if (value != null) {
                      if (value.length != 2) {
                          return result;
                      } else {
                          if (!isValidDate(value[0], toDay) || !isValidDate(value[0], value[1])) {
                              result.add("ERROR");
                              return result;
                          }
                          filter.append(" AND DATE.TIME GE ");
                          filter.append(convertToDate6(value[0]) + "0000");
                          filter.append(" AND DATE.TIME LE ");
                          filter.append(convertToDate6(value[1]) + "2359");
                      }
                  }
              }
//                if (fc.getOperand().equals("1")) {
//                    if (!isValidDate(fc.getValue(), toDay)) {
//                        result.add("ERROR");
//                        return result;
//                    }
//                    filter.append(" AND @ID LIKE ");
//                    
//                    filter.append("\"'IM" + convertToJulianDate(fc.getValue()) + "'...\"");
//                } else if (fc.getOperand().equals("2")) {
//                    String[] value = fc.getValue().split(" ");
//                    if (value != null) {
//                        if (value.length != 2) {
//                            return result;
//                        } else {
//                            if (!isValidDate(value[0], toDay) || !isValidDate(value[0], value[1])) {
//                                result.add("ERROR");
//                                return result;
//                            }
//                            int fromDate = Integer.parseInt(convertToJulianDate(value[0]));
//                            int toDate = Integer.parseInt(convertToJulianDate(value[1]));                            
//                            for (int i = fromDate; i <= toDate; i++) {
//                                if (i == fromDate) {
//                                    filter.append(" AND (@ID LIKE ");
//                                    
//                                    filter.append("\"'IM" + i + "'...\"");
//                                } else {
//                                    filter.append(" OR @ID LIKE ");
//                                    filter.append("\"'IM" + i + "'...\"");
//                                }
//                            }
//                            filter.append(") ");
//                        }
//                    }
//                }               
                isSearchToDay = false;
                break;                
			default:
				break;
			}
		}
		// if not exists filter query data only today
		if (isSearchToDay) {
			filter.append(" AND DATE.TIME LIKE ");
			filter.append(filterDate);
		}
		// Neu khong nhap dieu kien tim kiem Input Hub thi se loc theo Msb Hub cua user
        if (isSearchInpHubEQ) {
            filter.append(" AND MSB.INPUT.HUB EQ " + userHub);
        }

		long startTime = System.currentTimeMillis();

		logger.info("AuthImDocumentImageEnquiry filter: {}", filter.toString());
		List<String> imDocumentImages = da.selectRecords("", "IM.DOCUMENT.IMAGE", "", filter.toString(), null);

		long endTime = System.currentTimeMillis();

		logger.info("AuthImDocumentImageEnquiry result: {}, Time: {}", imDocumentImages.size(),
				(endTime - startTime) / 1000);
		List<ImDocImageSortCif> imDocImageSortCif = new ArrayList<>();
		for (String id : imDocumentImages) {
			ImDocumentImageRecord rec = t24RecordUtils.getRecord("", "IM.DOCUMENT.IMAGE", "", id,
					ImDocumentImageRecord.class);
			if (isSearchUserIdEQ || isSearchUserIdLK) {
				String userId = rec.getInputter(0).split("\\_")[1];
				if ((isSearchUserIdEQ && !userId.equalsIgnoreCase(fUserId))
						|| (isSearchUserIdLK && !userId.contains(fUserId.replace("...", "")))) {
					continue;
				}
			}
			if (rec.getImageApplication().getValue().equalsIgnoreCase("CUSTOMER")) {
				imDocImageSortCif.add(new ImDocImageSortCif(rec.getImageReference().getValue(),
						rec.getImageApplication().getValue(), rec.getShortDescription().getValue(),
						rec.getDescription(0).getValue(), parseLong(rec.getLocalRefField(MSB_IM_EFF_DATE).getValue()),
						parseLong(rec.getLocalRefField(MSB_IM_EXPIRY_DATE).getValue()), id,
						parseLong(rec.getDateTime(0)), Integer.parseInt(rec.getCurrNo()),
						parseLong(rec.getImageReference().getValue()), rec.getInputter(0).split("\\_")[1]));
			} else if (rec.getImageApplication().getValue().equalsIgnoreCase("ACCOUNT")) {
			    String customerNo = "";
			    try {
	                AccountRecord account = t24RecordUtils.getRecord("", "ACCOUNT", "", rec.getImageReference().getValue(),
	                        AccountRecord.class);
	                customerNo = account.getCustomer().getValue();
                } catch (Exception e) {
                    AccountRecord account = new AccountRecord(da.getHistoryRecord("ACCOUNT", rec.getImageReference().getValue()));
//                    AccountRecord account = t24RecordUtils.getRecord("", "ACCOUNT", "$HIS", rec.getImageReference().getValue(),
//                            AccountRecord.class);
                    customerNo = account.getCustomer().getValue();
                }

				//if (account != null) {
					imDocImageSortCif.add(new ImDocImageSortCif(rec.getImageReference().getValue(),
							rec.getImageApplication().getValue(), rec.getShortDescription().getValue(),
							rec.getDescription(0).getValue(),
							parseLong(rec.getLocalRefField(MSB_IM_EFF_DATE).getValue()),
							parseLong(rec.getLocalRefField(MSB_IM_EXPIRY_DATE).getValue()), id,
							parseLong(rec.getDateTime(0)), Integer.parseInt(rec.getCurrNo()),
							parseLong(customerNo), rec.getInputter(0).split("\\_")[1]));
				//}
			} else if (rec.getImageApplication().getValue().equals("AA.ARRANGEMENT")) {
				AaArrangementRecord aa = t24RecordUtils.getRecord("", "AA.ARRANGEMENT", "",
						rec.getImageReference().getValue(), AaArrangementRecord.class);
				if (aa != null) {
					String customerId = "";
					for (CustomerClass cus : aa.getCustomer()) {
						if (cus.getCustomerRole().getValue().equalsIgnoreCase("OWNER")) {
							customerId = cus.getCustomer().getValue();
						}
					}
					imDocImageSortCif.add(new ImDocImageSortCif(rec.getImageReference().getValue(),
							rec.getImageApplication().getValue(), rec.getShortDescription().getValue(),
							rec.getDescription(0).getValue(),
							parseLong(rec.getLocalRefField(MSB_IM_EFF_DATE).getValue()),
							parseLong(rec.getLocalRefField(MSB_IM_EXPIRY_DATE).getValue()), id,
							parseLong(rec.getDateTime(0)), Integer.parseInt(rec.getCurrNo()), parseLong(customerId),
							rec.getInputter(0).split("\\_")[1]));
				}
			}
		}
		
// SELECT HIST
        List<String> imDocumentImagesHist = da.selectRecords("", "IM.DOCUMENT.IMAGE", "$HIS", filter.toString(), null);

//        long endTime = System.currentTimeMillis();

        logger.info("AuthImDocumentImageEnquiry result: {}, Time: {}", imDocumentImagesHist.size(),
                (endTime - startTime) / 1000);
//        List<ImDocImageSortCif> imDocImageSortCif = new ArrayList<>();
        for (String id : imDocumentImagesHist) {
            ImDocumentImageRecord rec = t24RecordUtils.getRecord("", "IM.DOCUMENT.IMAGE", "$HIS", id,
                    ImDocumentImageRecord.class);
            if (isSearchUserIdEQ || isSearchUserIdLK) {
                String userId = rec.getInputter(0).split("\\_")[1];
                if ((isSearchUserIdEQ && !userId.equalsIgnoreCase(fUserId))
                        || (isSearchUserIdLK && !userId.contains(fUserId.replace("...", "")))) {
                    continue;
                }
            }
            if (rec.getImageApplication().getValue().equalsIgnoreCase("CUSTOMER")) {
                imDocImageSortCif.add(new ImDocImageSortCif(rec.getImageReference().getValue(),
                        rec.getImageApplication().getValue(), rec.getShortDescription().getValue(),
                        rec.getDescription(0).getValue(), parseLong(rec.getLocalRefField(MSB_IM_EFF_DATE).getValue()),
                        parseLong(rec.getLocalRefField(MSB_IM_EXPIRY_DATE).getValue()), id,
                        parseLong(rec.getDateTime(0)), Integer.parseInt(rec.getCurrNo()),
                        parseLong(rec.getImageReference().getValue()), rec.getInputter(0).split("\\_")[1]));
            } else if (rec.getImageApplication().getValue().equalsIgnoreCase("ACCOUNT")) {
                String customerNo = "";
                try {
                    AccountRecord account = t24RecordUtils.getRecord("", "ACCOUNT", "", rec.getImageReference().getValue(),
                            AccountRecord.class);
                    customerNo = account.getCustomer().getValue();
                } catch (Exception e) {
                    AccountRecord account = new AccountRecord(da.getHistoryRecord("ACCOUNT", rec.getImageReference().getValue()));
//                    AccountRecord account = t24RecordUtils.getRecord("", "ACCOUNT", "$HIS", rec.getImageReference().getValue(),
//                            AccountRecord.class);
                    customerNo = account.getCustomer().getValue();
                }
//                AccountRecord account = t24RecordUtils.getRecord("", "ACCOUNT", "", rec.getImageReference().getValue(),
//                        AccountRecord.class);
                //if (account != null) {
                    imDocImageSortCif.add(new ImDocImageSortCif(rec.getImageReference().getValue(),
                            rec.getImageApplication().getValue(), rec.getShortDescription().getValue(),
                            rec.getDescription(0).getValue(),
                            parseLong(rec.getLocalRefField(MSB_IM_EFF_DATE).getValue()),
                            parseLong(rec.getLocalRefField(MSB_IM_EXPIRY_DATE).getValue()), id,
                            parseLong(rec.getDateTime(0)), Integer.parseInt(rec.getCurrNo()),
                            parseLong(customerNo), rec.getInputter(0).split("\\_")[1]));
                //}
            } else if (rec.getImageApplication().getValue().equals("AA.ARRANGEMENT")) {
                AaArrangementRecord aa = t24RecordUtils.getRecord("", "AA.ARRANGEMENT", "",
                        rec.getImageReference().getValue(), AaArrangementRecord.class);
                if (aa != null) {
                    String customerId = "";
                    for (CustomerClass cus : aa.getCustomer()) {
                        if (cus.getCustomerRole().getValue().equalsIgnoreCase("OWNER")) {
                            customerId = cus.getCustomer().getValue();
                        }
                    }
                    imDocImageSortCif.add(new ImDocImageSortCif(rec.getImageReference().getValue(),
                            rec.getImageApplication().getValue(), rec.getShortDescription().getValue(),
                            rec.getDescription(0).getValue(),
                            parseLong(rec.getLocalRefField(MSB_IM_EFF_DATE).getValue()),
                            parseLong(rec.getLocalRefField(MSB_IM_EXPIRY_DATE).getValue()), id,
                            parseLong(rec.getDateTime(0)), Integer.parseInt(rec.getCurrNo()), parseLong(customerId),
                            rec.getInputter(0).split("\\_")[1]));
                }
            }
        }		

//		Map<Long, List<ImDocImageSortCif>> groupByCif = imDocImageSortCif.stream()
//				.sorted(Comparator.comparingLong(ImDocImageSortCif::getDateTime)
//						.thenComparing(ImDocImageSortCif::getImageId))
//				.collect(Collectors.groupingBy(ImDocImageSortCif::getCifId));
		
//		Map<Long, List<ImDocImageSortCif>> groupByCif = imDocImageSortCif.stream()
//		        .sorted(Comparator.comparingLong(ImDocImageSortCif::getCifId)
//		                .thenComparingLong(ImDocImageSortCif::getDateTime))
//		        .collect(Collectors.groupingBy(ImDocImageSortCif::getCifId));
//
//
//		logger.info("AuthImDocumentImageEnquiry end process calculate cif and sort: {}",
//				(System.currentTimeMillis() - startTime) / 1000);
		// create map sort distinct cif by date time
//		Map<Long, Long> sortCif = new TreeMap<>();
//		Set<Long> sortCif = new HashSet<>();
//		imDocImageSortCif.stream().sorted(Comparator.comparingLong(ImDocImageSortCif::getDateTime)).forEach(item -> {
//			if (!sortCif.contains(item.getCifId())) {
//				sortCif.add(item.getCifId());
//			}
//		});
//		for (Long cifId : sortCif) {
//			result.addAll(groupByCif.get(cifId).stream().map(ImDocImageSortCif::toString).collect(Collectors.toList()));
//		}
		
		// Sort and group by CifId, then sort by DateTime within each group
		Map<Long, List<ImDocImageSortCif>> groupByCif = imDocImageSortCif.stream()
		    .sorted(Comparator.comparingLong(ImDocImageSortCif::getDateTime))
		    .collect(Collectors.groupingBy(ImDocImageSortCif::getCifId,
		             LinkedHashMap::new,
		             Collectors.toList()));

		logger.info("AuthImDocumentImageEnquiry end process calculate cif and sort: {}",
		        (System.currentTimeMillis() - startTime) / 1000);

		// Create a LinkedHashSet to maintain order of CifIds sorted by their earliest DateTime
		Set<Long> sortedCifIds = new LinkedHashSet<>();
		imDocImageSortCif.stream()
		    .sorted(Comparator.comparingLong(ImDocImageSortCif::getCifId))
		    .forEachOrdered(item -> sortedCifIds.add(item.getCifId()));

		// Add results to the final list, maintaining the CifId and DateTime order
		for (Long cifId : sortedCifIds) {
		    result.addAll(groupByCif.get(cifId).stream()
		                  .map(ImDocImageSortCif::toString)
		                  .collect(Collectors.toList()));
		}
		return result;
	}

	private boolean isValidDate(String date, String toDay) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate givenDate = LocalDate.parse(date, formatter);
		LocalDate today = LocalDate.parse(toDay, formatter);
		Period period = Period.between(givenDate, today);
		return (period.getYears() * 12 + period.getMonths()) <= 3;
	}

	private long parseLong(String value) {
		try {
			return Long.parseLong(value);
		} catch (Exception ex) {

		}
		return 0l;
	}

	private String convertToDate6(String input) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat outputFormat = new SimpleDateFormat("yyMMdd");
		try {
			Date date = inputFormat.parse(input);
			return outputFormat.format(date);
		} catch (ParseException e) {
			logger.error("AuthImDocumentImageEnquiry Error format Date: {}", e.getMessage());
		}
		return input;
	}
	
//	private String convertToJulianDate(String input) {
//	        // Định dạng đầu vào là YYYYMMDD
//	        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
//            SimpleDateFormat julianFormat = new SimpleDateFormat("yyDDD");	   
//            try {
//                Date date = inputFormat.parse(input);
//                // Định dạng đầu ra là Julian Date YYDDD
//                return julianFormat.format(date);                
//            } catch (ParseException e) {
//                logger.error("AuthImDocumentImageEnquiry Error format Date: {}", e.getMessage());                
//                // TODO Auto-generated catch block
//                // Uncomment and replace with appropriate logger
//                // LOGGER.error(exception_var, exception_var);
//            }
//	        return input;
//	    }	
}
