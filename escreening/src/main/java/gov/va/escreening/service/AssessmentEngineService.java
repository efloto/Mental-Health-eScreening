package gov.va.escreening.service;

import gov.va.escreening.domain.AssessmentStatusEnum;
import gov.va.escreening.dto.ae.AssessmentRequest;
import gov.va.escreening.dto.ae.AssessmentResponse;
import gov.va.escreening.entity.SurveyPage;

import java.util.List;
import java.util.Map;

public interface AssessmentEngineService {

	/**
	 * Process user data in survey and returns the next page with the next set
	 * of questions and an updated progress section.
	 * 
	 * @param assessmentRequest
	 * @return
	 */
	AssessmentResponse processPage(AssessmentRequest assessmentRequest, List<SurveyPage> surveyPageList);

	/**
	 * Saves the veteran's answers found in the request. Any Rules dealing with
	 * questions found on the Survey page are run and any measure visibility
	 * changes are updated.
	 * 
	 * @param assessmentRequest
	 * @return a map (from measure ID to visibility) containing measures found
	 *         on the survey page found in the request.
	 *         
	 *  This function does a lot of DB IO unecessarily. Remove it and use the inmemory version
	 *  instead.
	 */
//	Map<Integer, Boolean> getUpdatedVisibility(
//			AssessmentRequest assessmentRequest);

	boolean transitionAssessmentStatusTo(Integer veteranAssessmentId,
			AssessmentStatusEnum requestedState);

	Map<Integer, Boolean> getUpdatedVisibilityInMemory(
			AssessmentRequest assessmentRequest);
}
