package gov.va.escreening.variableresolver;

import gov.va.escreening.entity.AssessmentVariable;

public interface TimeSeriesAssessmentVariableResolver {
	AssessmentVariableDto resolveAssessmentVariable(AssessmentVariable assessmentVariable, 
			Integer veteranAssessmentId);

}
