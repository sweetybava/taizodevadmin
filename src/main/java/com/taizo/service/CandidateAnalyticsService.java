package com.taizo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taizo.model.Admin;
import com.taizo.model.AdminAnalyticsModel;
import com.taizo.model.CandidateAnalyticsModel;
import com.taizo.repository.AdminRepository;
import com.taizo.repository.CandidateAnalyticsRepositroy;

@Service
public class CandidateAnalyticsService {
	
	@Autowired
	CandidateAnalyticsRepositroy candidateAnalyticsRepositroy;
	
	@Autowired
	AdminRepository adminRepository;

	//FbMetaLead Count
	public void fbMetaLeadcount(Long adminId, LocalDate currentDate) {
	    List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

	    if (!candidateAnalyticsList.isEmpty()) {
	        boolean dateMatch = false;


	        for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
	            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	            if (currentDate.isEqual(createdOnDate)) {
	                dateMatch = true;
	                adminAnalytics.setFbMetaLeads(
	                        adminAnalytics.getFbMetaLeads() != null
	                                ? adminAnalytics.getFbMetaLeads() + 1
	                                : 1
	                );
	            }
	        }

	        if (!dateMatch) {
	            // If no matching date found, insert a new record for the current date
	        	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
	            newAdminAnalytics.setAdminId(adminId);
	            newAdminAnalytics.setFbMetaLeads(1);
	            candidateAnalyticsRepositroy.save(newAdminAnalytics);
	        } else {
	            // If matching date found, update the existing records
	        	candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
	        }
	    } else {
	        // If there are no existing records for the adminId, insert a new record for the current date
	    	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
	        newAdminAnalytics.setAdminId(adminId);
	        newAdminAnalytics.setFbMetaLeads(1);
	        candidateAnalyticsRepositroy.save(newAdminAnalytics);
	    }
	}
	
	// Qualified FbMetaLead Counts
	public void QualifiedfbMetaLeadcounts(Long adminId, LocalDate currentDate) {
	    List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

	    if (candidateAnalyticsList != null && !candidateAnalyticsList.isEmpty()) {
	        boolean dateMatch = false;

	        for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
	            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	            if (currentDate.isEqual(createdOnDate)) {
	                dateMatch = true;
	                adminAnalytics.setQualifiedFbMetaLeads(
	                        adminAnalytics.getQualifiedFbMetaLeads() != null
	                                ? adminAnalytics.getQualifiedFbMetaLeads() + 1
	                                : 1
	                );
	            }
	        }

	        if (!dateMatch) {
	            // If no matching date found, insert a new record for the current date
	            CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
	            newAdminAnalytics.setAdminId(adminId);
	            newAdminAnalytics.setQualifiedFbMetaLeads(1);
	            candidateAnalyticsRepositroy.save(newAdminAnalytics);
	        } else {
	            // If matching date found, update the existing records
	            candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
	        }
	    } else {
	        // If there are no existing records for the adminId, insert a new record for the current date
	        CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
	        newAdminAnalytics.setAdminId(adminId);
	        newAdminAnalytics.setQualifiedFbMetaLeads(1);
	        candidateAnalyticsRepositroy.save(newAdminAnalytics);
	    }
	}

	
	//CanLead Counts
	public void CanLeadcount(Long adminId, LocalDate currentDate) {
	    List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

	    if (!candidateAnalyticsList.isEmpty()) {
	        boolean dateMatch = false;

	        for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
	            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
	            if (currentDate.isEqual(createdOnDate)) {
	                dateMatch = true;
	                adminAnalytics.setCanLeads(
	                        adminAnalytics.getCanLeads() != null
	                                ? adminAnalytics.getCanLeads() + 1
	                                : 1
	                );
	            }
	        }

	        if (!dateMatch) {
	            // If no matching date found, insert a new record for the current date
	        	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
	            newAdminAnalytics.setAdminId(adminId);
	            newAdminAnalytics.setCanLeads(1);
	            candidateAnalyticsRepositroy.save(newAdminAnalytics);
	        } else {
	            // If matching date found, update the existing records
	        	candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
	        }
	    } else {
	        // If there are no existing records for the adminId, insert a new record for the current date
	    	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
	        newAdminAnalytics.setAdminId(adminId);
	        newAdminAnalytics.setCanLeads(1);
	        candidateAnalyticsRepositroy.save(newAdminAnalytics);
	    }
	}
	
	//Qualified CanLead Counts
	public void QualifiedCanLeadcount(Long adminId, LocalDate currentDate) {
		    List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		    if (!candidateAnalyticsList.isEmpty()) {
		        boolean dateMatch = false;

		        for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
		            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
		            if (currentDate.isEqual(createdOnDate)) {
		                dateMatch = true;
		                adminAnalytics.setQualifiedCanLeads(
		                        adminAnalytics.getQualifiedCanLeads() != null
		                                ? adminAnalytics.getQualifiedCanLeads() + 1
		                                : 1
		                );
		            }
		        }

		        if (!dateMatch) {
		            // If no matching date found, insert a new record for the current date
		        	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
		            newAdminAnalytics.setAdminId(adminId);
		            newAdminAnalytics.setQualifiedCanLeads(1);
		            candidateAnalyticsRepositroy.save(newAdminAnalytics);
		        } else {
		            // If matching date found, update the existing records
		        	candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
		        }
		    } else {
		        // If there are no existing records for the adminId, insert a new record for the current date
		    	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
		        newAdminAnalytics.setAdminId(adminId);
		        newAdminAnalytics.setQualifiedCanLeads(1);
		        candidateAnalyticsRepositroy.save(newAdminAnalytics);
		    }
		}
	
	//TotalLeads Count
	public void TotalLeadscount(Long adminId, LocalDate currentDate) {
		
	List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

			    if (!candidateAnalyticsList.isEmpty()) {
			        boolean dateMatch = false;

			        for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
			            LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
			            if (currentDate.isEqual(createdOnDate)) {
			                dateMatch = true;
			                adminAnalytics.setTotalLeads(
			                        adminAnalytics.getTotalLeads() != null
			                                ? adminAnalytics.getTotalLeads() + 1
			                                : 1
			                );
			            }
			        }

			        if (!dateMatch) {
			            // If no matching date found, insert a new record for the current date
			        	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			            newAdminAnalytics.setAdminId(adminId);
			            newAdminAnalytics.setTotalLeads(1);
			            candidateAnalyticsRepositroy.save(newAdminAnalytics);
			        } else {
			            // If matching date found, update the existing records
			        	candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			        }
			    } else {
			        // If there are no existing records for the adminId, insert a new record for the current date
			    	CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			        newAdminAnalytics.setAdminId(adminId);
			        newAdminAnalytics.setTotalLeads(1);
			        candidateAnalyticsRepositroy.save(newAdminAnalytics);
			    }
			}
	
	//TotalQualifiedLeads Count
	public void TotalQualifiedLeadsCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setTotalQualifiedLeads(
							adminAnalytics.getTotalQualifiedLeads() != null
									? adminAnalytics.getTotalQualifiedLeads() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setTotalQualifiedLeads(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setTotalQualifiedLeads(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}
	//CanRegistrationCounts
	public void canRegistrationCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setCanRegistration(
							adminAnalytics.getCanRegistration() != null
									? adminAnalytics.getCanRegistration() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setCanRegistration(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setCanRegistration(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}

	//InterviewScheduled Counts
	public void InterviewScheduledCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setInterviewScheduled(
							adminAnalytics.getInterviewScheduled() != null
									? adminAnalytics.getInterviewScheduled() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setInterviewScheduled(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setInterviewScheduled(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}

	//interviewAttended Counts
	public void interviewAttendedCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setInterviewAttended(
							adminAnalytics.getInterviewAttended() != null
									? adminAnalytics.getInterviewAttended() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setInterviewAttended(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setInterviewAttended(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}	
	
	//CompanySelected Counts
	public void companySelectedCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setCompanySelected(
							adminAnalytics.getCompanySelected() != null
									? adminAnalytics.getCompanySelected() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setCompanySelected(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setCompanySelected(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}
	
	//OfferAccepted Counts
	public void OfferAcceptedCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setOfferAccepted(
							adminAnalytics.getOfferAccepted() != null
									? adminAnalytics.getOfferAccepted() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setOfferAccepted(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setOfferAccepted(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}
	
	//Joined Counts
	public void joinedCounts(Long adminId, LocalDate currentDate) {
		List<CandidateAnalyticsModel> candidateAnalyticsList = candidateAnalyticsRepositroy.findByAdminId(adminId);

		if (!candidateAnalyticsList.isEmpty()) {
			boolean dateMatch = false;

			for (CandidateAnalyticsModel adminAnalytics : candidateAnalyticsList) {
				LocalDate createdOnDate = adminAnalytics.getCreatedOn().toLocalDateTime().toLocalDate();
				if (currentDate.isEqual(createdOnDate)) {
					dateMatch = true;
					adminAnalytics.setJoined(
							adminAnalytics.getJoined() != null
									? adminAnalytics.getJoined() + 1
									: 1
					);
				}
			}

			if (!dateMatch) {
				// If no matching date found, insert a new record for the current date
				CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
				newAdminAnalytics.setAdminId(adminId);
				newAdminAnalytics.setJoined(1);
				candidateAnalyticsRepositroy.save(newAdminAnalytics);
			} else {
				// If matching date found, update the existing records
				candidateAnalyticsRepositroy.saveAll(candidateAnalyticsList);
			}
		} else {
			// If there are no existing records for the adminId, insert a new record for the current date
			CandidateAnalyticsModel newAdminAnalytics = new CandidateAnalyticsModel();
			newAdminAnalytics.setAdminId(adminId);
			newAdminAnalytics.setJoined(1);
			candidateAnalyticsRepositroy.save(newAdminAnalytics);
		}
	}

}
