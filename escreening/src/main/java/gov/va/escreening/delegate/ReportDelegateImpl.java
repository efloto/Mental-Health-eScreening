package gov.va.escreening.delegate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gov.va.escreening.controller.dashboard.ReportsController;
import gov.va.escreening.domain.ClinicDto;
import gov.va.escreening.domain.ProgramDto;
import gov.va.escreening.domain.SurveyDto;
import gov.va.escreening.domain.VeteranDto;
import gov.va.escreening.dto.report.*;
import gov.va.escreening.repository.UserProgramRepository;
import gov.va.escreening.security.EscreenUser;
import gov.va.escreening.service.*;
import gov.va.escreening.util.ReportsUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.FileResolver;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by munnoo on 3/16/15.
 */
@Service("reportDelegate")
public class ReportDelegateImpl implements ReportDelegate {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String PERCENT_FORMAT="%02d";
    private static final String ZERO_PERCENT="00%";
    @Resource(type = AssessmentDelegate.class)
    private AssessmentDelegate assessmentDelegate;

    @Resource(type = SurveyService.class)
    private SurveyService surveyService;

    @Resource(type = AssessmentVariableService.class)
    AssessmentVariableService avs;

    @Resource(type = ClinicService.class)
    private ClinicService clinicService;

    @Resource(type = VeteranAssessmentSurveyScoreService.class)
    private VeteranAssessmentSurveyScoreService scoreService;

    @Resource(type = VeteranAssessmentSurveyService.class)
    private VeteranAssessmentSurveyService vasSrv;

    @Resource(type = SurveyScoreIntervalService.class)
    private SurveyScoreIntervalService intervalService;

    @Resource(type = VeteranService.class)
    private VeteranService veteranService;

    @Resource(type = VeteranAssessmentService.class)
    private VeteranAssessmentService veteranAssessmentService;

    @Resource(type = UserProgramRepository.class)
    private UserProgramRepository upr;

    @Resource(type = ReportFunctionCommon.class)
    private ReportFunctionCommon reportsHelper;

    @Resource(name = "repFuncMap")
    Map<String, ReportFunction> repFuncMap;

    @Resource(name = "modulesForScoringMap")
    Map<String, String> modulesForScoringMap;

    private FileResolver fileResolver = new FileResolver() {

        @Override
        public File resolveFile(String fileName) {
            URI uri;
            try {
                uri = new URI(ReportsController.class.getResource(fileName).getPath());
                return new File(uri.getPath());
            } catch (URISyntaxException e) {
                System.out.println(fileName);
                logger.error("Fail to load image file for jasper report " + fileName);
                return null;
            }
        }
    };

    @Override
    public Map<String, Object> getAvgScoresVetByClinicGraphReport(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, String> svgObject = (Map<String, String>) requestData.get("svgData");
        Map<String, Object> userReqData = (Map<String, Object>) requestData.get("userReqData");
        List cClinicList = (List) userReqData.get(ReportsUtil.CLINIC_ID_LIST);
        List sSurveyList = (List) userReqData.get(ReportsUtil.SURVEY_ID_LIST);

        Map<String, Object> parameterMap = Maps.newHashMap();
        attachDates(parameterMap, userReqData);

        List<ClinicVeteranDTO> resultList = Lists.newArrayList();

        for (Object c : cClinicList) {
            Integer clinicId = (Integer) c;

            ClinicVeteranDTO cvDTO = new ClinicVeteranDTO();

            cvDTO.setClinicName(clinicService.getClinicNameById(clinicId));
            cvDTO.setVeteranModuleGraphReportDTOs(new ArrayList<VeteranModuleGraphReportDTO>());

            List<Integer> veterans = clinicService.getAllVeteranIds(clinicId);
            VeteranModuleGraphReportDTO veteranModuleGraphReportDTO = null;

            for (Integer vId : veterans) {
                veteranModuleGraphReportDTO = new VeteranModuleGraphReportDTO();
                veteranModuleGraphReportDTO.setModuleGraphs(new ArrayList<ModuleGraphReportDTO>());
                cvDTO.getVeteranModuleGraphReportDTOs().add(veteranModuleGraphReportDTO);

                VeteranDto vDto = veteranService.getByVeteranId(vId);

                veteranModuleGraphReportDTO.setLastNameAndSSN(vDto.getLastName() + ", " + vDto.getSsnLastFour());
                final List<ModuleGraphReportDTO> moduleGraphs = veteranModuleGraphReportDTO.getModuleGraphs();

                for (Object o : sSurveyList) {
                    Integer surveyId = (Integer) o;
                    repFuncMap.get("avgStatGraph").createReport(new Object[]{userReqData, vId, surveyId, clinicId, moduleGraphs, svgObject});
                }
            }
            if (veteranModuleGraphReportDTO != null && !veteranModuleGraphReportDTO.getModuleGraphs().isEmpty()) {
                resultList.add(cvDTO);
            }

        }

        JRDataSource dataSource = new JREmptyDataSource();

        if (resultList != null && !resultList.isEmpty()){
            dataSource = new JRBeanCollectionDataSource(resultList);
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);
        parameterMap.put("noData", resultList.isEmpty());
        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    public List<SurveyDto> getSurveyList() {

        List<String> surveyNames = Lists.newArrayList();
        surveyNames.addAll(modulesForScoringMap.keySet());

        return surveyService.getSurveyListByNames(surveyNames);
    }

    @Override
    public List<ClinicDto> getClinicDtoList() {
        return clinicService.getClinicDtoList();
    }

    @Override
    public Map<String, Object> getAveScoresByClinicGraphOrNumeric(Map<String, Object> requestData, EscreenUser escreenUser, boolean includeCount, boolean isGraphOnly) {
        Map<String, String> svgObject = (Map<String, String>) requestData.get("svgData");
        Map<String, Object> userReqData = (Map<String, Object>) requestData.get("userReqData");

        Map<String, Object> parameterMap = Maps.newHashMap();

        List cClinicList = (List) userReqData.get(ReportsUtil.CLINIC_ID_LIST);
        List sSurveyList = (List) userReqData.get(ReportsUtil.SURVEY_ID_LIST);

        attachDates(parameterMap, userReqData);

        List<Integer> surveyIds = Lists.newArrayList();

        for (Object s : sSurveyList) {
            surveyIds.add((Integer) s);
        }

        List<ClinicDTO> clinics = Lists.newArrayList();

        for (Object c : cClinicList) {
            Integer clinicId = (Integer) c;
            ClinicDTO dto = new ClinicDTO();
            dto.setClinicName(clinicService.getClinicNameById(clinicId));
            dto.setGraphReport(new ArrayList<ModuleGraphReportDTO>());
            final List<ModuleGraphReportDTO> graphReport = dto.getGraphReport();
            for (Object s : sSurveyList) {
                Integer surveyId = (Integer) s;
                repFuncMap.get("avgStatGrpGraphOrNumber").createReport(new Object[]{userReqData, surveyId, clinicId, svgObject, graphReport, includeCount, isGraphOnly});
            }
            clinics.add(dto);
        }

        JRDataSource dataSource = new JRBeanCollectionDataSource(clinics);

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);
        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    public Map<String, Object> getIndividualStaticsGraphicPDF(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, Object> userReqData = (Map<String, Object>) requestData.get("userReqData");

        Map<String, Object> parameterMap = Maps.newHashMap();

        String lastName = (String) userReqData.get(ReportsUtil.LASTNAME);
        String last4SSN = (String) userReqData.get(ReportsUtil.SSN_LAST_FOUR);
        List surveyIds = (List) userReqData.get(ReportsUtil.SURVEY_ID_LIST);

        attachDates(parameterMap, userReqData);
        parameterMap.put("lastNameSSN", lastName + ", " + last4SSN);

        VeteranDto veteran = new VeteranDto();
        veteran.setLastName(lastName);
        veteran.setSsnLastFour(last4SSN);

        List<VeteranDto> veterans = assessmentDelegate.findVeterans(veteran);

        Integer veteranId = -1;

        if (veterans != null && !veterans.isEmpty()) {
            veteranId = veterans.get(0).getVeteranId();
        }

        List<ModuleGraphReportDTO> resultList = Lists.newArrayList();

        for (int i = 0; i < surveyIds.size(); i++) {
            Integer surveyId = (Integer) surveyIds.get(i);
            repFuncMap.get("indivStatGraphPlusNumber").createReport(new Object[]{userReqData, veteranId, surveyId, resultList, requestData.get("svgData")});
        }

        JRDataSource dataSource = new JREmptyDataSource();

        if (resultList != null && !resultList.isEmpty()){
            dataSource = new JRBeanCollectionDataSource(resultList);
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);
        parameterMap.put("noData", resultList.isEmpty());
        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    public List<Map<String, Object>> createIndivChartableDataForAvgScoresForPatientsByClinic(Map<String, Object> requestData) {
        List cList = (List) requestData.get(ReportsUtil.CLINIC_ID_LIST);
        List sList = (List) requestData.get(ReportsUtil.SURVEY_ID_LIST);

        List<Map<String, Object>> chartableDataList = Lists.newArrayList();
        for (Object oClinicId : cList) {
            Integer clinicId = (Integer) oClinicId;
            List<Integer> veterans = clinicService.getAllVeteranIds(clinicId);
            for (Integer vId : veterans) {
                for (Object oSurveyId : sList) {
                    Integer surveyId = (Integer) oSurveyId;
                    repFuncMap.get("avgStatIndivChart").createReport(new Object[]{requestData, vId, surveyId, clinicId, chartableDataList});
                }
            }
        }
        return chartableDataList;
    }


    @Override
    public List<Map<String, Object>> createGrpChartableDataForAvgScoresForPatientsByClinic(Map<String, Object> requestData) {
        List<Map<String, Object>> chartableDataList = Lists.newArrayList();

        logger.trace("Generating the clinic graph data ");

        List cList = (List) requestData.get(ReportsUtil.CLINIC_ID_LIST);
        List sList = (List) requestData.get(ReportsUtil.SURVEY_ID_LIST);

        for (Object oClinicId : cList) {

            Integer clinicId = (Integer) oClinicId;

            for (Object oSurveyId : sList) {
                Integer surveyId = (Integer) oSurveyId;
                repFuncMap.get("avgStatGrpChart").createReport(new Object[]{requestData, surveyId, clinicId, chartableDataList});
            }
        }
        return chartableDataList;
    }

    @Override
    public List<Map<String, Object>> createChartableDataForIndividualStats(Map<String, Object> requestData) {
        List<Map<String, Object>> chartableDataList = Lists.newArrayList();

        logger.trace("Generating the individual statistics reports");

        String lastName = (String) requestData.get(ReportsUtil.LASTNAME);
        String last4SSN = (String) requestData.get(ReportsUtil.SSN_LAST_FOUR);

        VeteranDto veteran = new VeteranDto();
        veteran.setLastName(lastName);
        veteran.setSsnLastFour(last4SSN);

        List<VeteranDto> veterans = assessmentDelegate.findVeterans(veteran);

        if (veterans == null || veterans.isEmpty()) {
            return chartableDataList;
        }

        Integer veteranId = veterans.get(0).getVeteranId();

        for (Object strSurveyId : (List) requestData.get(ReportsUtil.SURVEY_ID_LIST)) {
            Integer surveyId = (Integer) strSurveyId;
            repFuncMap.get("indivStatChart").createReport(new Object[]{requestData, veteranId, surveyId, chartableDataList});
        }

        return chartableDataList;

    }


    @Override
    public Map<String, Object> genIndividualStatisticsNumeric(Map<String, Object> requestData, EscreenUser escreenUser) {
        String lastName = (String) requestData.get(ReportsUtil.LASTNAME);
        String last4SSN = (String) requestData.get(ReportsUtil.SSN_LAST_FOUR);

        Map<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("lastNameSSN", lastName + ", " + last4SSN);
        parameterMap.put("noData", true);
        attachDates(parameterMap, requestData);

        VeteranDto veteran = new VeteranDto();
        veteran.setLastName(lastName);
        veteran.setSsnLastFour(last4SSN);

        List<VeteranDto> veterans = assessmentDelegate.findVeterans(veteran);

        JRDataSource dataSource = null;

        if (veterans == null || veterans.isEmpty()) {
            dataSource = new JREmptyDataSource();
        } else {
            List<TableReportDTO> resultList = Lists.newArrayList();

            for (Object strSurveyId : (List) requestData.get(ReportsUtil.SURVEY_ID_LIST)) {
                Integer surveyId = (Integer) strSurveyId;
                Integer veteranId = veterans.get(0).getVeteranId();
                repFuncMap.get("indivStatNumeric").createReport(new Object[]{requestData, veteranId, surveyId, resultList});
            }

            if (resultList.isEmpty()) {
                dataSource = new JREmptyDataSource();
            } else {
                dataSource = new JRBeanCollectionDataSource(resultList);
            }
            parameterMap.put("noData", resultList.isEmpty());
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);

        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    public Map<String, Object> genClinicStatisticReportsPart1eScreeningBatteriesReport(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, Object> parameterMap = Maps.newHashMap();
        attachDates(parameterMap, requestData);
        attachClinics(parameterMap, requestData);

        parameterMap.put("datasource", new JREmptyDataSource());
        parameterMap.put("noData", true);

        final DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        final LocalDate fromDate = dtf.parseLocalDate(requestData.get(ReportsUtil.FROMDATE).toString());
        final LocalDate toDate = dtf.parseLocalDate(requestData.get(ReportsUtil.TODATE).toString());

        List<Integer> clinicIds = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);
        String strFromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String strToDate = (String) requestData.get(ReportsUtil.TODATE);

        int totals = 0;
        Map<LocalDate, Integer> dateTotalMap = Maps.newHashMap();
        if (requestData.get("eachDay") != null && ((Boolean) requestData.get("eachDay"))) {
            parameterMap.put("showByDay", true);
            List<Report593ByDayDTO> data = veteranAssessmentService.getBatteriesByDay(strFromDate, strToDate, clinicIds);
            parameterMap.put("byDay", data);

            for (Report593ByDayDTO dto : data) {
                totals += Integer.parseInt(dto.getTotal().trim());
            }

            parameterMap.put("grandTotal", "" + totals);
            if (totals > 0) {
                parameterMap.put("noData", false);
            }
        } else {
            parameterMap.put("showByDay", false);
            parameterMap.put("byDay", Lists.newArrayList());
        }

        int total2 = 0;

        if (requestData.get("timeOfDayWeek") != null && ((Boolean) requestData.get("timeOfDayWeek"))) {
            parameterMap.put("showByTime", true);
            List<Report593ByTimeDTO> data = veteranAssessmentService.getBatteriesByTime(strFromDate, strToDate, clinicIds);

            for (Report593ByTimeDTO dto : data) {
                total2 += Integer.parseInt(dto.getTotal().trim());
            }

            parameterMap.put("byTime", data);
            parameterMap.put("grandTotal", "" + total2);

            if (total2 > 0) {
                parameterMap.put("noData", false);
            }


        } else {
            parameterMap.put("showByTime", false);
            parameterMap.put("byTime", Lists.newArrayList());
        }


        List<KeyValueDTO> ks = Lists.newArrayList();


        KeyValueDTO keyValueDTO;

        boolean bCheckAll = false;


        if (requestData.get("numberOfUniqueVeteran") != null
                && (Boolean) requestData.get("numberOfUniqueVeteran")) {
            keyValueDTO = new KeyValueDTO();
            keyValueDTO.setKey("Number of Unique Veterans");

            keyValueDTO.setValue(veteranAssessmentService.getUniqueVeterns(clinicIds, strFromDate, strToDate));
            if (!keyValueDTO.getValue().equals("0")) {
                parameterMap.put("noData", false);
            }
            ks.add(keyValueDTO);
            bCheckAll = true;
        }

        if (requestData.get("numberOfeScreeningBatteries") != null
                && (Boolean) requestData.get("numberOfeScreeningBatteries")) {
            keyValueDTO = new KeyValueDTO();
            keyValueDTO.setValue(
                    veteranAssessmentService.getNumOfBatteries(clinicIds, strFromDate, strToDate)
            );
            if (!keyValueDTO.getValue().equals("0")) {
                parameterMap.put("noData", false);
            }
            keyValueDTO.setKey("Number of eScreening batteries completed");
            ks.add(keyValueDTO);
            bCheckAll = true;
        }

        if (requestData.get("averageTimePerAssessment") != null
                && (Boolean) requestData.get("averageTimePerAssessment")) {
            keyValueDTO = new KeyValueDTO();
            String value = veteranAssessmentService.getAverageTimePerAssessment(clinicIds, strFromDate, strToDate);
            if (!value.equals("0")) {
                parameterMap.put("noData", false);
            }
            keyValueDTO.setValue(
                    value + " min"
            );
            keyValueDTO.setKey("Average time per assessment");
            ks.add(keyValueDTO);

            bCheckAll = true;
        }

        if (requestData.get("numberOfAssessmentsPerClinician") != null &&
                (Boolean) requestData.get("numberOfAssessmentsPerClinician")) {
            keyValueDTO = new KeyValueDTO();
            keyValueDTO.setValue(veteranAssessmentService.calculateAvgAssessmentsPerClinician(clinicIds, strFromDate, strToDate));
            keyValueDTO.setKey("Average number of assessments per clinician in each clinic");

            if (keyValueDTO.getValue() != null && !keyValueDTO.getValue().equals("0")) {
                parameterMap.put("noData", false);
            }
            ks.add(keyValueDTO);
            bCheckAll = true;
        }

        if (requestData.get("numberAndPercentVeteransWithMultiple") != null
                && (Boolean) requestData.get("numberAndPercentVeteransWithMultiple")) {
            keyValueDTO = new KeyValueDTO();
            keyValueDTO.setValue(veteranAssessmentService.getVeteranWithMultiple(clinicIds, strFromDate, strToDate) + "%");
            keyValueDTO.setKey("Number and percent of veterans with multiple eScreening batteries");
            if (!keyValueDTO.getValue().equals("0%")) {
                parameterMap.put("noData", false);
            }
            ks.add(keyValueDTO);
            bCheckAll = true;
        }

        parameterMap.put("checkAll", ks);
        parameterMap.put("showCheckAll", bCheckAll);

        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    public Map<String, Object> genClinicStatisticReportsPartIVAverageTimePerModuleReport(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, Object> parameterMap = Maps.newHashMap();

        attachDates(parameterMap, requestData);
        attachClinics(parameterMap, requestData);

        Map<Integer, Map<String, String>> surveyAvgTimeMap = vasSrv.calculateAvgTimePerSurvey(requestData);

        JRDataSource dataSource = null;

        List<SurveyTimeDTO> dtos = Lists.newArrayList();

        for (SurveyDto survey : surveyService.getSurveyList()) {
            Map<String, String> surveyAvgTime = surveyAvgTimeMap.get(survey.getSurveyId());
            if (surveyAvgTime != null) {
                SurveyTimeDTO surveyTimeDTO = new SurveyTimeDTO();
                surveyTimeDTO.setModuleName(survey.getName());
                surveyTimeDTO.setModuleTime(surveyAvgTime.get("MODULE_TOTAL_TIME"));
                surveyTimeDTO.setModuleAvgMin(surveyAvgTime.get("MODULE_AVG_MIN"));
                surveyTimeDTO.setModuleAvgSec(surveyAvgTime.get("MODULE_AVG_SEC"));
                dtos.add(surveyTimeDTO);
            }
        }

        if (dtos.isEmpty()) {
            dataSource = new JREmptyDataSource();
            parameterMap.put("noData", true);
        } else {
            dataSource = new JRBeanCollectionDataSource(dtos);
            parameterMap.put("noData", false);
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);

        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    public Map<String, Object> genClinicStatisticReportsPartVDemographicsReport(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, Object> parameterMap = Maps.newHashMap();

        parameterMap.put("noData", true);
        attachDates(parameterMap, requestData);
        attachClinics(parameterMap, requestData);
        attachGender(parameterMap, requestData);
        attachEthnicity(parameterMap, requestData);
        attachAge(parameterMap, requestData);

        attachEducation(parameterMap, requestData);
        attachEmploymentStatus(parameterMap, requestData);
        attachMilitaryBranch(parameterMap, requestData);
        attachTobaccoUsage(parameterMap, requestData);
        attachDeployments(parameterMap, requestData);

        parameterMap.put("datasource", new JREmptyDataSource());
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);

        logger.warn(parameterMap.toString());

        return parameterMap;
    }

    @Override
    public Map<String, Object> genClinicStatisticReportsPartIIIList20MostSkippedQuestionsReport(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, Object> parameterMap = Maps.newHashMap();
        attachDates(parameterMap, requestData);
        attachClinics(parameterMap, requestData);
        JRDataSource dataSource = null;


        List<Integer> clinicIds = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);
        String fromDate = requestData.get(ReportsUtil.FROMDATE).toString();
        String toDate = requestData.get(ReportsUtil.TODATE).toString();

        List<Report595DTO> dtos = veteranAssessmentService.getTopSkippedQuestions(clinicIds, fromDate, toDate);

        if (dtos == null || dtos.isEmpty()) {
            parameterMap.put("noData", true);
            dataSource = new JREmptyDataSource();
        } else {
            parameterMap.put("noData", false);
            dataSource = new JRBeanCollectionDataSource(dtos);
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);

        logger.warn(parameterMap.toString());

        return parameterMap;
    }

    @Override
    public Map<String, Object> genClinicStatisticReportsPartIIMostCommonTypesOfAlertsPercentagesReport(Map<String, Object> requestData, EscreenUser escreenUser) {
        Map<String, Object> parameterMap = Maps.newHashMap();

        attachDates(parameterMap, requestData);
        attachClinics(parameterMap, requestData);

        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> clinicIds = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        int totalAssessment = veteranAssessmentService.findAssessmentCount(fromDate, toDate, clinicIds);

        List<Report594DTO> dtos = veteranAssessmentService.findAlertsCount(fromDate, toDate, clinicIds);

        JRDataSource dataSource = null;

        if (totalAssessment == 0 || dtos == null || dtos.size() == 0) {
            parameterMap.put("noData", true);
            dataSource = new JREmptyDataSource();
        } else {
            parameterMap.put("noData", false);
            for (Report594DTO report594DTO : dtos) {

                int numerator = Integer.parseInt(report594DTO.getModuleCount());
                report594DTO.setModuleCount(String.format("%s/%s", numerator, totalAssessment));
                report594DTO.setModulePercent(String.format("%5.2f%%", numerator * 100.0f / totalAssessment));
            }
            dataSource = new JRBeanCollectionDataSource(dtos);
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);
        logger.warn(parameterMap.toString());

        return parameterMap;
    }


    /**
     * for 599 report
     *
     * @param requestData
     * @param escreenUser
     * @return
     */
    @Override
    public Map<String, Object> genClinicStatisticReportsPartVIPositiveScreensReport
    (Map<String, Object> requestData, EscreenUser escreenUser) {

        Map<String, Object> parameterMap = Maps.newHashMap();
        attachDates(parameterMap, requestData);
        attachClinics(parameterMap, requestData);

        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> clinicIds = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        JRDataSource dataSource = null;

        List<Report599DTO> dtos = scoreService.getClinicStatisticReportsPartVIPositiveScreensReport(fromDate, toDate, clinicIds);

        if (dtos == null || dtos.isEmpty()) {
            dataSource = new JREmptyDataSource();
            parameterMap.put("noData", true);
        } else {
            parameterMap.put("noData", false);
            dataSource = new JRBeanCollectionDataSource(dtos);
        }

        parameterMap.put("datasource", dataSource);
        parameterMap.put("REPORT_FILE_RESOLVER", fileResolver);
        logger.warn(parameterMap.toString());
        return parameterMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClinicDto> getClinicDtoList(EscreenUser escreenUser) {
        final List<ClinicDto> clinicDtoList = getClinicDtoList();

        final List<ClinicDto> allowedClinic = Lists.newArrayList();

        Integer userId = escreenUser == null ? 0 : escreenUser.getUserId();

        if (userId == 0) {
            logger.warn(String.format("User Id is 0, therefore no clinics will be returned. Please check the user id of the ecreen user logged in"));
        }

        // use this user Id and go an get try to get UserProgram using this id and each programId from clinic.
        // If found then that is a intersection and that clinic is allowed
        for (ClinicDto clinicDto : clinicDtoList) {
            for(ProgramDto pdto:clinicDto.getProgramDtos()) {
                if (upr.hasUserAndProgram(userId, pdto.getProgramId())) {
                    allowedClinic.add(clinicDto);
                }
            }
        }

        return allowedClinic;
    }

    private void attachDates(Map<String, Object> dataCollection, Map<String, Object> requestData) {
        String fromDate = requestData.get(ReportsUtil.FROMDATE).toString();
        String toDate = requestData.get(ReportsUtil.TODATE).toString();
        dataCollection.put("fromToDate", String.format("From %s -- %s", fromDate, toDate));
    }

    private void attachClinics(Map<String, Object> dataCollection, Map<String, Object> requestData) {
        StringBuilder sb = new StringBuilder();
        for (Integer clicnicId : (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST)) {
            sb.append(clinicService.getClinicNameById(clicnicId)).append(", ");
        }
        if (sb.length() > 2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        dataCollection.put("clinicNames", sb.toString());
    }

    private void attachDeployments(Map<String, Object> dataCollection, Map<String, Object> requestData) {
        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> cList = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Number> result = veteranAssessmentService.getNumOfDeploymentStatistics(cList, fromDate, toDate);

        if (result == null || result.size() != 3) {
            dataCollection.put("numberofdeployments", "");
        } else {
            dataCollection.put("numberofdeployments",
                    String.format("Mean number of deployments = %2.1f and minimum Value = %d and Maximum Value = %d",
                            result.get(0).floatValue(),
                            result.get(1).intValue(),
                            result.get(2).intValue()
                    ));
            dataCollection.put("noData", false);
        }
    }

    private void attachTobaccoUsage(Map<String, Object> dataCollection, Map<String, Object> requestData) {
        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> cList = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Integer> result = veteranAssessmentService.getTobaccoCount(cList, fromDate, toDate);

        int missing = veteranAssessmentService.getTobaccoMissingCount(cList, fromDate, toDate);

        int total = 0;
        if (result != null && result.size() > 0) {
            for (Integer i : result) {
                total += i;
            }

            total += missing;
            if (total != 0) {
                dataCollection.put("tobacco_never_percentage", String.format(PERCENT_FORMAT, result.get(0) * 100 / total) + "%");
                dataCollection.put("tobacco_never_count", result.get(0) + "/" + total);
                dataCollection.put("tobacco_no_percentage", String.format(PERCENT_FORMAT, result.get(1) * 100 / total) + "%");
                dataCollection.put("tobacco_no_count", result.get(1) + "/" + total);
                dataCollection.put("tobacco_yes_percentage", String.format(PERCENT_FORMAT, result.get(2) * 100 / total) + "%");
                dataCollection.put("tobacco_yes_count", result.get(2) + "/" + total);
                dataCollection.put("tobacco_miss_percentage", String.format(PERCENT_FORMAT, missing * 100 / total) + "%");
                dataCollection.put("tobacco_miss_count", missing + "/" + total);
                dataCollection.put("noData", false);
            }
        }

        if (total == 0) {
            dataCollection.put("tobacco_never_percentage", ZERO_PERCENT);
            dataCollection.put("tobacco_never_count", "0/0");
            dataCollection.put("tobacco_no_percentage", ZERO_PERCENT);
            dataCollection.put("tobacco_no_count", "0/0");
            dataCollection.put("tobacco_yes_percentage", ZERO_PERCENT);
            dataCollection.put("tobacco_miss_percentage", ZERO_PERCENT);
            dataCollection.put("tobacco_yes_count", "0/0");
            dataCollection.put("tobacco_miss_count", "0/0");
        }
    }

    private void attachMilitaryBranch(Map<String, Object> dataCollection, Map<String, Object> requestData) {
        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> cList = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Integer> result = veteranAssessmentService.getBranchCount(cList, fromDate, toDate);

        int missing = veteranAssessmentService.getMissingBranchCount(cList, fromDate, toDate);

        int total = 0;
        if (result != null && result.size() > 0) {
            for (Integer i : result) {
                total += i;
            }
            total += missing;
            if (total != 0) {
                dataCollection.put("army_percentage", String.format(PERCENT_FORMAT, result.get(0) * 100 / total)+"%");
                dataCollection.put("army_count", result.get(0) + "/" + total);
                dataCollection.put("airforce_percentage", String.format(PERCENT_FORMAT, result.get(1) * 100 / total)+"%");
                dataCollection.put("airforce_count", result.get(1) + "/" + total);
                dataCollection.put("coast_percentage", String.format(PERCENT_FORMAT, result.get(2) * 100 / total)+"%");
                dataCollection.put("coast_count", result.get(2) + "/" + total);
                dataCollection.put("marines_percentage", String.format(PERCENT_FORMAT, result.get(3) * 100 / total)+"%");
                dataCollection.put("marines_count", result.get(3) + "/" + total);
                dataCollection.put("nationalguard_percentage", String.format(PERCENT_FORMAT, result.get(4) * 100 / total)+"%");
                dataCollection.put("nationalguard_count", result.get(4) + "/" + total);
                dataCollection.put("navy_percentage", String.format(PERCENT_FORMAT, result.get(5) * 100 / total)+"%");
                dataCollection.put("navy_count", result.get(5) + "/" + total);
                dataCollection.put("missingmilitary_percentage", String.format(PERCENT_FORMAT, missing * 100 / total)+"%");
                dataCollection.put("missingmilitary_count", missing + "/" + total);
                dataCollection.put("noData", false);
            }
        }

        if (total == 0) {
            dataCollection.put("army_percentage", ZERO_PERCENT);
            dataCollection.put("army_count", "0/0");
            dataCollection.put("airforce_percentage", ZERO_PERCENT);
            dataCollection.put("airforce_count", "0/0");
            dataCollection.put("coast_percentage", ZERO_PERCENT);
            dataCollection.put("coast_count", "0/0");
            dataCollection.put("marines_percentage", ZERO_PERCENT);
            dataCollection.put("marines_count", "0/" + total);
            dataCollection.put("nationalguard_percentage", ZERO_PERCENT);
            dataCollection.put("nationalguard_count", "0/0");
            dataCollection.put("navy_percentage", ZERO_PERCENT);
            dataCollection.put("navy_count", "0/" + total);
            dataCollection.put("missingmilitary_percentage", ZERO_PERCENT);
            dataCollection.put("missingmilitary_count", "0/" + total);
        }
    }

    private void attachEmploymentStatus(Map<String, Object> dataCollection, Map<String, Object> requestData) {

        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> cList = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Integer> result = veteranAssessmentService.getEmploymentCount(cList, fromDate, toDate);

        int missing = veteranAssessmentService.getMissingEmploymentCount(cList, fromDate, toDate);

        int total = 0;
        if (result != null && result.size() > 0) {
            for (Integer i : result) {
                total += i;
            }

            total += missing;

            if (total != 0) {
                dataCollection.put("fulltime_percentage", String.format(PERCENT_FORMAT, result.get(0) * 100 / total) + "%");
                dataCollection.put("fulltime_count", result.get(0) + "/" + total);
                dataCollection.put("parttime_percentage", String.format(PERCENT_FORMAT, result.get(1) * 100 / total) + "%");
                dataCollection.put("parttime_count", result.get(1) + "/" + total);
                dataCollection.put("seasonal_percentage", String.format(PERCENT_FORMAT, result.get(2) * 100 / total) + "%");
                dataCollection.put("seasonal_count", result.get(2) + "/" + total);
                dataCollection.put("daylabor_percentage", String.format(PERCENT_FORMAT, result.get(3) * 100 / total) + "%");
                dataCollection.put("daylabor_count", result.get(3) + "/" + total);
                dataCollection.put("unemployed_percentage", String.format(PERCENT_FORMAT, result.get(4) * 100 / total) + "%");
                dataCollection.put("unemployed_count", result.get(4) + "/" + total);
                dataCollection.put("noData", false);

                dataCollection.put("missingemp_percentage", String.format(PERCENT_FORMAT, missing * 100 / total) + "%");
                dataCollection.put("missingemp_count", missing + "/" + total);
            }
        }

        if (total == 0) {
            dataCollection.put("fulltime_percentage", ZERO_PERCENT);
            dataCollection.put("fulltime_count", "0/0");
            dataCollection.put("parttime_percentage", ZERO_PERCENT);
            dataCollection.put("parttime_count", "0/0");
            dataCollection.put("seasonal_percentage", ZERO_PERCENT);
            dataCollection.put("seasonal_count", "0/0");
            dataCollection.put("daylabor_percentage", ZERO_PERCENT);
            dataCollection.put("daylabor_count", "0/0");
            dataCollection.put("unemployed_percentage", ZERO_PERCENT);
            dataCollection.put("unemployed_count", "0/0");
            dataCollection.put("missingemp_percentage", ZERO_PERCENT);
            dataCollection.put("missingemp_count", "0/0");
        }
    }

    private void attachEducation(Map<String, Object> dataCollection, Map<String, Object> requestData) {

        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> cList = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Integer> result = veteranAssessmentService.getEducationCount(cList, fromDate, toDate);

        Integer missingCount = veteranAssessmentService.getMissingEducationCount(cList, fromDate, toDate);

        int total = 0;
        if (result != null && result.size() > 0) {
            for (Integer i : result) {
                total += i;
            }

            total += missingCount;

            if (total != 0) {
                dataCollection.put("highschool_percentage", String.format(PERCENT_FORMAT, result.get(0) * 100 / total) + "%");
                dataCollection.put("highschool_count", result.get(0) + "/" + total);
                dataCollection.put("ged_percentage", String.format(PERCENT_FORMAT, result.get(1) * 100 / total) + "%");
                dataCollection.put("ged_count", result.get(1) + "/" + total);
                dataCollection.put("highschooldip_percentage", String.format(PERCENT_FORMAT, result.get(2) * 100 / total) + "%");
                dataCollection.put("highschooldip_count", result.get(2) + "/" + total);
                dataCollection.put("somecollege_percentage", String.format(PERCENT_FORMAT, result.get(3) * 100 / total) + "%");
                dataCollection.put("somecollege_count", result.get(3) + "/" + total);
                dataCollection.put("associate_percentage", String.format(PERCENT_FORMAT, result.get(4) * 100 / total) + "%");
                dataCollection.put("associate_count", result.get(4) + "/" + total);
                dataCollection.put("college_percentage", String.format(PERCENT_FORMAT, result.get(5) * 100 / total) + "%");
                dataCollection.put("college_count", result.get(5) + "/" + total);
                dataCollection.put("master_percentage", String.format(PERCENT_FORMAT, result.get(6) * 100 / total) + "%");
                dataCollection.put("master_count", result.get(6) + "/" + total);
                dataCollection.put("dr_percentage", String.format(PERCENT_FORMAT, result.get(7) * 100 / total) + "%");
                dataCollection.put("dr_count", result.get(7) + "/" + total);
                dataCollection.put("missingedu_percentage", String.format(PERCENT_FORMAT, missingCount * 100 / total) + "%");
                dataCollection.put("missingedu_count", missingCount + "/" + total);
                dataCollection.put("noData", false);
            }
        }

        if (total == 0) {

            dataCollection.put("highschool_percentage", ZERO_PERCENT);
            dataCollection.put("highschool_count", "0/0");
            dataCollection.put("ged_percentage", ZERO_PERCENT);
            dataCollection.put("ged_count", "0/0");
            dataCollection.put("highschooldip_percentage", ZERO_PERCENT);
            dataCollection.put("highschooldip_count", "0/0");
            dataCollection.put("ged_percentage", ZERO_PERCENT);
            dataCollection.put("ged_count", "0/0");
            dataCollection.put("somecollege_percentage", ZERO_PERCENT);
            dataCollection.put("somecollege_count", "0/0");
            dataCollection.put("associate_percentage", ZERO_PERCENT);
            dataCollection.put("associate_count", "0/0");
            dataCollection.put("college_percentage", ZERO_PERCENT);
            dataCollection.put("college_count", "0/0");
            dataCollection.put("master_percentage", ZERO_PERCENT);
            dataCollection.put("master_count", "0/0");
            dataCollection.put("dr_percentage", ZERO_PERCENT);
            dataCollection.put("dr_count", "0/0");
            dataCollection.put("missingedu_percentage", ZERO_PERCENT);
            dataCollection.put("missingedu_count", "0/0");
        }
    }

    private void attachAge(Map<String, Object> dataCollection, Map<String, Object> requestData) {
        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List<Integer> cList = (List<Integer>) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Number> result = veteranAssessmentService.getAgeStatistics(cList, fromDate, toDate);

        if (result == null || result.isEmpty() || result.size() != 3) {
            dataCollection.put("age", "");
        } else {

            dataCollection.put("age",
                    String.format("Mean Age %3.1f years Minimum Value = %d and Maximum value = %d",
                            result.get(0).floatValue(), result.get(1).intValue(), result.get(2).intValue()));
            dataCollection.put("noData", false);
        }
    }

    private void attachEthnicity(Map<String, Object> dataCollection, Map<String, Object> requestData) {

        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List cList = (List) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Integer> result = veteranAssessmentService.getEthnicityCount(cList, fromDate, toDate);
        int missing = veteranAssessmentService.getMissingEthnicityCount(cList, fromDate, toDate);

        int total = 0;
        if (result != null && result.size() > 0) {
            total = result.get(0) + result.get(1) + result.get(2) + missing;

            if (total != 0) {
                dataCollection.put("hispanic_percentage", String.format(PERCENT_FORMAT, result.get(0) * 100 / total) + "%");
                dataCollection.put("hispanic_count", result.get(0) + "/" + total);
                dataCollection.put("non_hispanic_percentage", String.format(PERCENT_FORMAT, result.get(1) * 100 / total) + "%");
                dataCollection.put("non_hispanic_count", result.get(1) + "/" + total);
                dataCollection.put("missingethnicity_percentage", String.format(PERCENT_FORMAT, missing * 100 / total) + "%");
                dataCollection.put("missingethnicity_count", missing + "/" + total);
                dataCollection.put("noData", false);
            }
        }
        if (total == 0) {
            dataCollection.put("hispanic_percentage", ZERO_PERCENT);
            dataCollection.put("hispanic_count", "0/0");
            dataCollection.put("non_hispanic_percentage", ZERO_PERCENT);
            dataCollection.put("non_hispanic_count", "0/0");
            dataCollection.put("missingethnicity_percentage", ZERO_PERCENT);
            dataCollection.put("missingethnicity_count", "0/0");
        }

        total = 0;
        result = veteranAssessmentService.getRaceCount(cList, fromDate, toDate);

        if (result != null && result.size() > 0) {
            for (Integer i : result) {
                total += i;
            }

            if (total != 0) {
                dataCollection.put("white_percentage", String.format(PERCENT_FORMAT, result.get(0) * 100 / total) + "%");
                dataCollection.put("white_count", result.get(0) + "/" + total);
                dataCollection.put("black_percentage", String.format(PERCENT_FORMAT, result.get(1) * 100 / total) + "%");
                dataCollection.put("black_count", result.get(1) + "/" + total);
                dataCollection.put("indian_percentage", String.format(PERCENT_FORMAT, result.get(2) * 100 / total) + "%");
                dataCollection.put("indian_count", result.get(2) + "/" + total);
                dataCollection.put("asian_percentage", String.format(PERCENT_FORMAT, result.get(3) * 100 / total) + "%");
                dataCollection.put("asian_count", result.get(3) + "/" + total);
                dataCollection.put("hawaiian_percentage", String.format(PERCENT_FORMAT, result.get(4) * 100 / total) + "%");
                dataCollection.put("hawaiian_count", result.get(4) + "/" + total);
                dataCollection.put("otherrace_percentage", String.format(PERCENT_FORMAT, result.get(5) * 100 / total) + "%");
                dataCollection.put("otherrace_count", result.get(5) + "/" + total);
                dataCollection.put("norace_percentage", String.format(PERCENT_FORMAT, result.get(6) * 100 / total) + "%");
                dataCollection.put("norace_count", result.get(6) + "/" + total);
            }
        }

        if (total == 0) {

            dataCollection.put("white_percentage", ZERO_PERCENT);
            dataCollection.put("white_count", "0/0");
            dataCollection.put("black_percentage", ZERO_PERCENT);
            dataCollection.put("black_count", "0/0");
            dataCollection.put("indian_percentage", ZERO_PERCENT);
            dataCollection.put("indian_count", "0/0");
            dataCollection.put("asian_percentage", ZERO_PERCENT);
            dataCollection.put("asian_count", "0/0");
            dataCollection.put("hawaiian_percentage", ZERO_PERCENT);
            dataCollection.put("hawaiian_count", "0/0");
            dataCollection.put("otherrace_percentage", ZERO_PERCENT);
            dataCollection.put("otherrace_count", "0/0");
            dataCollection.put("norace_percentage", ZERO_PERCENT);
            dataCollection.put("norace_count", "0/0");
        }
    }

    private void attachGender(Map<String, Object> dataCollection, Map<String, Object> requestData) {

        String fromDate = (String) requestData.get(ReportsUtil.FROMDATE);
        String toDate = (String) requestData.get(ReportsUtil.TODATE);
        List cList = (List) requestData.get(ReportsUtil.CLINIC_ID_LIST);

        List<Integer> result = veteranAssessmentService.getGenderCount(cList, fromDate, toDate);

        if (result != null) {

            int female = result.get(1);
            int male = result.get(0);
            int total = female + male;

            if (total > 0) {
                dataCollection.put("female_percentage", String.format(PERCENT_FORMAT, female * 100 / total) + "%");
                dataCollection.put("female_count", female + "/" + total);
                dataCollection.put("male_percentage", String.format(PERCENT_FORMAT, male * 100 / total) + "%");
                dataCollection.put("male_count", male + "/" + total);
                dataCollection.put("noData", false);
                return;
            }
        }
        dataCollection.put("female_percentage", ZERO_PERCENT);
        dataCollection.put("female_count", "0/0");
        dataCollection.put("male_percentage", ZERO_PERCENT);
        dataCollection.put("male_count", "0/0");


    }
}
