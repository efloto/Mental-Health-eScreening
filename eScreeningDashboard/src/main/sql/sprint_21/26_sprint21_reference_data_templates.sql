/* ********************************************** */
/* template   UPDATES */
/* ********************************************** */




/* Homelessness Clinical Reminder */

UPDATE template SET template_id = 8, 
	template_type_id = 3, 
	name = 'Homelessness CR CPRS Note Entry', 
	description = 'Homelessness Clinical Reminder Module CPRS Note Entry', 
	template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
HOUSING: 
${MODULE_TITLE_END}
${MODULE_START}

  <#assign housing_section>

	<#-- var763: ${var763}<br><br>     var2000: ${var2000!""}<br><br> var2001: ${var2001!""}<br><br>  var2002: ${var2002!""}<br><br>  -->  
  	<#if (var2000.children)?? && ((var2000.children)?size > 0)  >
		
		
		<#if (var2000.children[0].key == "var763") >  <#-- declined -->
				
				The Veteran declined to discuss their current housing situation.
		
		<#elseif (var2000.children[0].key == "var761")
				&& (var2002.children)?? && (var2008.children)?? 
					&& ((var2002.children)?size > 0) && ((var2008.children)?size > 0) >   <#-- no -->
	
				The Veteran ${getSelectOneDisplayText(var2000)} been living in stable housing for the last 2 months. ${LINE_BREAK}
				The Veteran has been living ${getSelectOneDisplayText(var2002)}.${LINE_BREAK}
				The Veteran ${getSelectOneDisplayText(var2008)} like a referral to talk more about his/her housing concerns.${NBSP}
				
		
		<#elseif (var2000.children[0].key == "var762")
				&& (var2001.children)?? && ((var2001.children)?size > 0) >   <#-- yes -->
				
				<#assign allComplete = false>
				<#assign showQ = false>

				<#if var2001.children[0].key == "var772">
					<#if (var2002.children)?? &&  ((var2002.children)?size > 0)
							&& (var2008.children)?? &&  ((var2008.children)?size > 0)>
						<#assign showQ = true>
						<#assign allComplete = true>
					</#if>
				<#elseif var2001.children[0].key == "var771">
					<#assign allComplete = true>
				<#else>
					<#assign allComplete = true>
				</#if>

				<#if allComplete>
					The Veteran ${getSelectOneDisplayText(var2000)} been living in stable housing for the last 2 months. ${LINE_BREAK}
					<#if showQ>
						The Veteran has been living ${getSelectOneDisplayText(var2002)}.${LINE_BREAK}
					</#if>
					The Veteran ${getSelectOneDisplayText(var2001)} concerned about housing in the future.${LINE_BREAK}
					<#if showQ>
						The Veteran ${getSelectOneDisplayText(var2008)} like a referral to talk more about his/her housing concerns.
					</#if>
				<#else>
					${getNotCompletedText()}.
				</#if>
		<#else>
				${getNotCompletedText()}.
		</#if>


    <#else>
		${getNotCompletedText()}
	</#if> 

  </#assign>
  <#if !(housing_section = "") >
     ${housing_section}
  <#else>
     ${noParagraphData}
  </#if>
${MODULE_END}
'
where template_id = 8;








/*  SCORING MATRIX  */

UPDATE template SET template_id = 100, 
	template_type_id = 4, 
	name = 'OOO Battery CPRS Note Scoring Matrix', 
	description = 'OOO Battery CPRS Note Scoring Matrix', 
	template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
SCORING MATRIX: 
${MODULE_TITLE_END}
${MODULE_START}
	

<#assign matrix_section>
	
	<#macro resetRow >
		<#assign screen = "">
		<#assign status = "">
		<#assign score = "">
		<#assign cutoff = "">
	</#macro>

		
		<#assign empty = "--">
		<#assign rows = []>

		<#-- AUDIT C -->
		<#assign screen = "AUDIT C">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		<#assign rows = []>
	
		<#if var1229??>
			<#assign score = getFormulaDisplayText(var1229)>
			<#if score != "notset">
				<#assign cutoff = "3-women/4-men">
				<#assign score = score?number>
				
				<#if (score >= 0) && (score <= 2)>
					<#assign status = "Negative">
				<#elseif (score >= 4) && (score <= 998)>
					<#assign status = "Positive">
				<#elseif (score == 3 )>
					<#assign status = "Positive for women/Negative for men">
				</#if>
			<#else>
				<#assign score = empty> 
			</#if>
			<#if (score?string == empty) || (status == empty)>
				<#assign status = empty>
				<#assign score = empty>
			</#if>
		</#if>
	
		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>



		<#-- TBI -->
		<#assign screen = "BTBIS (TBI)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if (var2047.children)?? && ((var2047.children)?size > 0)>
			
			<#if isSelectedAnswer(var2047,var3442)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif isSelectedAnswer(var2047,var3441)> 
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status ="Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>



		<#-- DAST 10 -->
		<#assign screen = "DAST-10 (Substance Abuse)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>

		<#if var1010?? >
			<#assign score = getFormulaDisplayText(var1010)>
			<#if (score?length > 0) &&  score != "notset">
				<#assign cutoff = "3">
				<#if ((score?number) <= 2)> 
					<#assign status ="Negative">
				<#else> 
					<#assign status ="Positive">
				</#if> 
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- GAD 7 -->
		<#assign screen = "GAD-7 (Anxiety)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1749?? >
			<#assign score = getFormulaDisplayText(var1749)>
			<#if score != "notset">
					<#assign cutoff = "10">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- HOUSING - Homelessness -->
		<#assign screen = "Homelessness">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var2008?? >
			<#if isSelectedAnswer(var2008, var792)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif isSelectedAnswer(var2008, var791) >
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- ISI -->
		<#assign screen = "ISI (Insomnia)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var2189?? >
			<#assign score = getFormulaDisplayText(var2189)>
			<#if score != "notset">
					<#assign cutoff = "15">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- MST -->
		<#assign screen = "MST">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1640?? && var1642?? >
			<#if isSelectedAnswer(var1640, var1642)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif isSelectedAnswer(var1640, var1641)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- PCLC -->
		<#assign screen = "PCL-C (PTSD)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1929?? >
			<#assign score = getFormulaDisplayText(var1929)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "50">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- PTSD -->
		<#assign screen = "PC-PTSD">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1989?? >
			<#assign score = getFormulaDisplayText(var1989)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "3">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>
		


	
		<#-- PHQ 9 DEPRESSION -->
		<#assign screen = "PHQ-9 (Depression)">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if var1599?? >
			<#assign score = getFormulaDisplayText(var1599)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "10">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- Prior MH DX/TX - Prior Mental Health Treatment -->
		<#assign screen = "Prior MH DX/TX">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		<#assign acum = 0>
		<#assign Q1complete =false>
		<#assign Q2complete =false>
		<#assign Q3complete =false>
		<#assign Q4complete =false>
		<#-- var1520: ${var1520!""}<br><br>  var1530: ${var1530!""}<br><br>  var200: ${var200!""}<br><br>  var210: ${var210!""}<br><br>  -->
		<#if (var1520.children)?? && ((var1520.children)?size > 0)>
			<#assign Q1complete = true>
			<#list var1520.children as c>
				<#if ((c.key == "var1522")  || (c.key == "var1523")  || (c.key == "var1524")) && (c.value == "true")>
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>

		<#if (var1530.children)?? && ((var1530.children)?size > 0)>
			<#assign Q2complete = true>
			<#list var1530.children as c>
				<#if ((c.key == "var1532")  || (c.key == "var1533") 
						|| (c.key == "var1534") || (c.key == "var1535") 
						|| (c.key == "var1536")) && (c.value == "true") >
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>
		

		<#if (var200.children)?? && ((var200.children)?size > 0)>
			<#assign Q3complete = true>
			<#list var200.children as c>
				<#if ((c.key == "var202") && (c.value == "true"))  >
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>

		<#if (var210.children)?? && ((var210.children)?size > 0)>
			<#assign Q4complete = true>
			<#list var210.children as c>
				<#if ((c.key == "var214") && (c.value == "true"))  >
					<#assign acum = acum + 1>
					<#break>
				</#if>
			</#list>
		</#if>

		<#if Q1complete && Q2complete && Q3complete && Q3complete>
			<#assign score = "N/A">
			<#assign cutoff = "N/A">
			<#if (acum >= 3)>
				<#assign status = "Positive">
			<#elseif (acum > 0) && (acum <= 2)>
				<#assign status ="Negative">
			</#if>
		</#if>
			
		


		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>
	



		<#-- TOBACCO -->
		<#assign screen = "Tobacco Use">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>
		
		<#if (var600.children)?? && ((var600.children)?size > 0)>
			<#if isSelectedAnswer(var600, var603)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Positive">
			<#elseif isSelectedAnswer(var600, var601) || isSelectedAnswer(var600, var602)>
				<#assign score = "N/A">
				<#assign cutoff = "N/A">
				<#assign status = "Negative">
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>




		<#-- VAS PAIN - BASIC PAIN -->
		<#assign screen = "VAS PAIN">
		<#assign status = empty>
		<#assign score = empty>
		<#assign cutoff = empty>

		<#if (var2300)?? >
			<#assign score = getSelectOneDisplayText(var2300)>
			<#if score != "notset" && score != "notfound">
					<#assign cutoff = "4">
					<#if (score?number >= cutoff?number)>
						<#assign status = "Positive">
					<#else> 
						<#assign status ="Negative">
					</#if>
			<#else>
				<#assign score = empty>
			</#if>
		</#if>

		<#assign rows = rows + [[screen, status, score, cutoff]]>	
		<@resetRow/>



		${MATRIX_TABLE_START}
			${MATRIX_TR_START}
				${MATRIX_TH_START}Screen${MATRIX_TH_END}
				${MATRIX_TH_START}Result${MATRIX_TH_END}
				${MATRIX_TH_START}Raw Score${MATRIX_TH_END}
				${MATRIX_TH_START}Cut-off Score${MATRIX_TH_END}
			${MATRIX_TR_END}
			${MATRIX_TR_START}
				<#list rows as row>
					${MATRIX_TR_START}
					<#list row as col>
						${MATRIX_TD_START}${col}${MATRIX_TD_END}
					</#list>
					${MATRIX_TR_END}
				</#list>
			${MATRIX_TR_END}
		${MATRIX_TABLE_END}


</#assign>
	
	<#if !(matrix_section = "") >
     	${matrix_section}
  	<#else>
     	${noParagraphData}
  </#if> 
${MODULE_END}'
 WHERE template_id = 100;







/* TEST */
 UPDATE template SET template_id = 101, 
	template_type_id = 8, 
	name = 'Test', 
	description = 'Test', 
	template_file = 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
TEST TYPE:
${MODULE_TITLE_END}
${MODULE_START}
  TEST TYPE
 ${MODULE_END}
'
	WHERE template_id = 101;


	
	
	
 /* VETERAN SUMMARY -  TEMPLATES*/	
	
/* VETERAN SUMMARY HEADER INSERT */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (200, 6, 'Veteran Summary Header', 'Veteran Summary Header',
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}

${MATRIX_TABLE_START}
	${MATRIX_TR_START}
		${MATRIX_TD_START}eScreening Summary ${MATRIX_TD_END}
		${TABLE_TD_SPACER1_START}${NBSP}${NBSP}${TABLE_TD_END}
		${TABLE_TD_RT_START}${IMG_LOGO_VA_HC}${TABLE_TD_END}
		${TABLE_TD_RT_START}${IMG_CESMITH_BLK_BRDR}${TABLE_TD_END}
	${MATRIX_TR_END}
${MATRIX_TABLE_END}

${MODULE_END}
');	
INSERT INTO battery_template (battery_id, template_id) VALUES (5, 200);	
	
	/* Veteran Summary - Footer */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (220, 7, 'Veteran Summary Footer', 'Veteran Summary Footer',
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
VETERAN SUMMARY FOOTER
${MODULE_TITLE_END}
${MODULE_START}
${MATRIX_TABLE_START}
	${MATRIX_TR_START}
		${TABLE_TD_CTR_START}For online information about support services and benefits, visit the VA Center of Excellence resource site:${TABLE_TD_END}
	${MATRIX_TR_END}
	${TABLE_TR_CTR_START}
		${TABLE_TD_CTR_START}http://escreening.cesamh.org ${TABLE_TD_END}
	${TABLE_TR_END}
	${TABLE_TR_CTR_START}
		${TABLE_TD_CTR_START}${IMG_VA_VET_SMRY}${TABLE_TD_END}
	${TABLE_TR_END}
	${MATRIX_TR_START}
		${TABLE_TD_LFT_START}For confidential help and support any time, call the Veteran\'s Suicide Prevention/Crisis Hotline at 
(800) 273-8255. The Hotline is never closed; someone is always there to take your call, even on holidays and in the middle of the night. 
${TABLE_TD_END}
	${MATRIX_TR_END}
${MATRIX_TABLE_END}

${MODULE_END}
');
INSERT INTO battery_template (battery_id, template_id) VALUES (5, 220);
	



-- /* VETERAN SUMMARY - Advance Directive  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (300, 8, 'Veteran Summary Advance Directive Entry', 'Veteran Summary Advance Directive Entry',
'
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if score == 0>
	<#assign scoreText = "Complete">
	<#assign isComplete = true>
<#elseif score == 1>
	<#assign scoreText = "Declined">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Advance Directive ${LINE_BREAK}
	This is a legal paper that tells your wishes for treatment if you become too sick to talk, and if needed, can help your doctors and family to make decisions about your care. 
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendations: Call VA Social Work Service at (858) 552-8585 ext. 3500, and ask for help in creating and filing an advance directive. 
</#if>
${MODULE_END} ');
INSERT INTO survey_template (survey_id, template_id) VALUES (9, 300);


-- /* VETERAN SUMMARY - Homelessness  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (301, 8, 'Veteran Summary Homelessness Entry', 'Veteran Summary Homelessness Entry',
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if score == 0>
	<#assign scoreText = "unstable housing">
	<#assign isComplete = true>
<#elseif score == 1>
	<#assign scoreText = "stable housing">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Homelessness ${LINE_BREAK}
	This is when you do not have a safe or stable place you can return to every night. The VA is committed to ending Veteran homelessness by the end of 2015. 
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendation: Call the VA\'s free National Call Center for Homeless Veterans at (877)-424-3838 and ask for help. Someone is always there to take your call.
</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (7, 301);


-- /* VETERAN SUMMARY - Alcohol Use  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (302, 8, 'Veteran Summary Alcohol Use Entry', 'Veteran Summary Alcohol Use Entry', 
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if (score >= 0) && (score <= 2)>
	<#assign scoreText = "negative screen">
	<#assign isComplete = true>
<#elseif (score == 3) >
	<#assign scoreText = "at risk">
	<#assign isComplete = true>
<#elseif (score >= 4) && (score <= 12)>
	<#assign scoreText = "at risk">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Alcohol Use ${LINE_BREAK}
	Drinking too much, too often, or both, causes serious problems. Abuse can have negative effects on school, work, and relationships, and can cause liver disease and cirrhosis, congestive heart failure, seizures, falls, hypertension, and other serious health risks.
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendation: If female, limit yourself to one drink a day; if male, limit yourself to 2 drinks a day. If this is difficult, ask your clinician for help with managing your drinking.  
</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (6, 302);


-- /* VETERAN SUMMARY - Insomnia  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (303, 8, 'Veteran Summary Insomnia Entry', 'Veteran Summary Insomnia Entry', 
'
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if (score >= 0) && (score <= 7)>
	<#assign scoreText = "negative screen">
	<#assign isComplete = true>
<#elseif (score >= 8) && (score <= 14)>
	<#assign scoreText = "at risk">
	<#assign isComplete = true>
<#elseif (score >= 4) && (score <= 12)>
	<#assign scoreText = "at risk">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Insomnia ${LINE_BREAK}
	Insomnia is having trouble sleeping that lasts longer than a few weeks. Some causes are: medical (like depression or pain), lifestyle factors (such as too much caffeine), or even stress. 
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendation: Describe your sleeping problems to your clinician, or learn more about insomnia at the  CESAMH site at: http://escreening.cesamh.org 

</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (36, 303);



-- /* VETERAN SUMMARY - Environmental Exposure  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (304, 8, 'Veteran Summary Environmental Exposure Entry', 'Veteran Summary Environmental Exposure Entry', '
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if (score == 0)>
	<#assign scoreText = "none reported">
	<#assign isComplete = true>
<#elseif (score == 1) >
	<#assign scoreText = "at risk">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Environmental Exposure ${LINE_BREAK}
	This is when you have been exposed to a hazard that may have potential health risks.
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendation: Call Dale Willoughby at the Environmental Registry Program and discuss your exposure: (858) 642-3995, weekdays 7:30am-4:00pm. 


</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (14, 304);



-- /* VETERAN SUMMARY - Military Sexual Trauma (MST)  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (305, 8, 'Veteran Summary Military Sexual Trauma (MST) Entry', 'Veteran Summary Military Sexual Trauma (MST) Entry', 
'
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if (score == 0)>
	<#assign scoreText = "negative screen">
	<#assign isComplete = true>
<#elseif (score == 1)>
	<#assign scoreText = "postive screen">
	<#assign isComplete = true>
<#elseif (score == 2)>
	<#assign scoreText = "declined to answer">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Military Sexual Trauma (MST) ${LINE_BREAK}
	MST is sexual assault or repeated, threatening sexual harassment that occurred while the Veteran was in the military. MST can happen any time or anywhere, to men and women. MST can affect your physical and mental health, even years later.
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendation: Ask your clinician for help managing your MST. 

</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (32, 305);



-- /* VETERAN SUMMARY - Tobacco Use  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (306, 8, 'Veteran Summary Tobacco Use Entry', 'Veteran Summary Tobacco Use Entry', 
' 
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if (score >= 0) && (score <= 2)>
	<#assign scoreText = "negative screen">
	<#assign isComplete = true>
<#elseif (score == 2)>
	<#assign scoreText = "current user">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Tobacco Use ${LINE_BREAK}
	The use of tobacco causes harm to nearly every organ in the body. Quitting greatly lowers your risk of death from cancers, heart disease, stroke, and emphysema. There are many options, such as in-person and telephone counseling, nicotine replacement, and prescription medications.
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendations: Prepare a plan to reduce or quit the use of tobacco. Get support from family and friends, and ask your clinician for help if needed.  

</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (25, 306);





-- /* VETERAN SUMMARY - Traumatic Brain Injury (TBI)  */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (307, 8, 'Veteran Summary Traumatic Brain Injury (TBI) Entry', 'Veteran Summary Traumatic Brain Injury (TBI) Entry',
'<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}

${MODULE_TITLE_END}
${MODULE_START}
<#assign isComplete = false> <#-- fix when get right variables -->
<#assign score = 0>
<#assign scoreText = "">
<#if (score >= 0) && (score <= 3)>
	<#assign scoreText = "negative screen">
	<#assign isComplete = true>
<#elseif (score >= 4 ) >
	<#assign scoreText = "at risk">
	<#assign isComplete = true>
</#if>

<#if isComplete>
	
	Traumatic Brain Injury (TBI) ${LINE_BREAK}
	A TBI is physical damage to your brain, caused by a blow to the head. Common causes are falls, fights, sports, and car accidents. A blast or shot can also cause TBI.
	${LINE_BREAK}
	${LINE_BREAK}
	Results: ${scoreText}	${LINE_BREAK}
	Recommendation: Ask your clinician about treatment for any symptoms that are bothering you. 

</#if>
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (29, 307);



-- /* VETERAN SUMMARY - My Depression Score */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (308, 8, 'Veteran Summary PHQ 9 Depression Entry', 'Veteran Summary PHQ 9 Depression Entry', 
'
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
Depression
${MODULE_TITLE_END}
${MODULE_START}
Depression is when you feel sad and hopeless for much of the time. It affects your body and thoughts, and interferes with daily life. There are effective treatments and resources for dealing with depression.${LINE_BREAK}
Recommendation: Ask your clinician for further evaluation and treatment options. 
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (30, 308);



-- /* VETERAN SUMMARY -  My Pain Score  (Basic Pain) */
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (309, 8, 'Veteran Summary Basic Pain Score Entry', 'Veteran Summary Basic Pain Score Entry', 
'
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
Pain 
${MODULE_TITLE_END}
${MODULE_START}
Pain can slow healing and stop you from being active. Untreated pain can harm your sleep, outlook, and ability to do things. ${LINE_BREAK}
Recommendation: Tell your clinician if medications aren\'t reducing your pain, or if the pain suddenly increases or changes, and ask for help with managing your pain. 
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (20, 309);



-- /* VETERAN SUMMARY -  My PTSD Score*/
INSERT INTO template(template_id, template_type_id, name, description, template_file) VALUES (310, 8, 'Veteran Summary PTSD Entry', 'Veteran Summary PTSD Entry',
'
<#include "clinicalnotefunctions"> 
<#-- Template start -->
${MODULE_TITLE_START}
PTSD 
${MODULE_TITLE_END}
${MODULE_START}
PTSD is when remembering a traumatic event keeps you from living a normal life. It\'s also called shell shock or combat stress. Common symptoms include recurring memories or nightmares of the event, sleeplessness, and feeling angry, irritable, or numb. ${LINE_BREAK}
Recommendation: Ask your clinician for further evaluation and treatment options. 
${MODULE_END}
');
INSERT INTO survey_template (survey_id, template_id) VALUES (35, 310);