package gov.va.escreening.variableresolver;

import gov.va.escreening.dto.ae.Answer;
import gov.va.escreening.entity.AssessmentVariable;
import gov.va.escreening.entity.Measure;
import gov.va.escreening.entity.SurveyMeasureResponse;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public interface MeasureAnswerAssessmentVariableResolver {
	AssessmentVariableDto resolveAssessmentVariable(AssessmentVariable assessmentVariable, 
			Integer veteranAssessmentId, Map<Integer, AssessmentVariable> measureAnswerHash);
	
	AssessmentVariableDto resolveAssessmentVariable(AssessmentVariable measureAnswerAssessmentVariable, SurveyMeasureResponse response, 
			Map<Integer, AssessmentVariable> measureAnswerHash);
	
	String resolveCalculationValue(AssessmentVariable assessmentVariable, Integer veteranAssessmentId, 
			SurveyMeasureResponse response);
	
	String resolveCalculationValue(AssessmentVariable assessmentVariable, Integer veteranAssessmentId, NullValueHandler smrNullHandler);

	String resolveCalculationValue(AssessmentVariable answerVariable,
			Pair<Measure, gov.va.escreening.dto.ae.Measure> answer);

	/**
	 * @param answer
	 * @param index if index is null then null is returned
	 * @return
	 */
	String resolveCalculationValue(Pair<Measure, gov.va.escreening.dto.ae.Measure> answer, Integer index);
}
