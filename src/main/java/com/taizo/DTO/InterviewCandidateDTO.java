package com.taizo.DTO;

import com.taizo.model.CanInterviewsModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewCandidateDTO {

	 private CanInterviewsModel canInterviewsModel;
	 private String candidateName;
}
