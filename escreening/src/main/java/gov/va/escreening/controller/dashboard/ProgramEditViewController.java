package gov.va.escreening.controller.dashboard;

import gov.va.escreening.domain.BatteryDto;
import gov.va.escreening.domain.ClinicDto;
import gov.va.escreening.dto.DropDownObject;
import gov.va.escreening.form.ProgramEditViewFormBean;
import gov.va.escreening.security.CurrentUser;
import gov.va.escreening.security.EscreenUser;
import gov.va.escreening.service.BatteryService;
import gov.va.escreening.service.ClinicService;
import gov.va.escreening.service.NoteTitleService;
import gov.va.escreening.service.ProgramService;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "/dashboard")
public class ProgramEditViewController {

	private static final Logger logger = LoggerFactory.getLogger(ProgramEditViewController.class);

	private BatteryService batteryService;
	private ClinicService clinicService;
	private NoteTitleService noteTitleService;
	private ProgramService programService;

	/**
	 * Returns the backing bean for the form.
	 * 
	 * @return
	 */
	@ModelAttribute
	public ProgramEditViewFormBean getProgramEditViewFormBean() {
		logger.trace("Creating new ProgramEditViewFormBean");
		return new ProgramEditViewFormBean();
	}

	private void populateModel(Model model) {
		Set<BatteryDto> batteryList = batteryService.getBatteryDtoSet();
		model.addAttribute("batteryList", batteryList);

		List<ClinicDto> clinicList = clinicService.getClinicDtoList();
		model.addAttribute("clinicList", clinicList);

		List<DropDownObject> noteTitleList = noteTitleService.getNoteTitleList();
		model.addAttribute("noteTitleList", noteTitleList);
	}

	/**
	 * User clicked on the cancel button. Redirect to list view page.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/programEditView", method = RequestMethod.POST, params = "cancelButton")
	public String processCancel(Model model) {

		logger.trace("In processCancel");

		return "redirect:/dashboard/programListView";
	}

	/**
	 * Saves the data by either creating a new record or updating an existing one.
	 * 
	 * @param programEditViewFormBean
	 * @param result
	 * @param model
	 * @param escreenUser
	 * @return
	 */
	@RequestMapping(value = "/programEditView", method = RequestMethod.POST, params = "saveButton")
	public String processSave(
			@Valid @ModelAttribute ProgramEditViewFormBean programEditViewFormBean,
			BindingResult result, Model model,
			@CurrentUser EscreenUser escreenUser) {

		logger.trace("In processSave");

		// If there is an error, return the same view.
		if (result.hasErrors()) {
			populateModel(model);
			return "systemTab/programEditView";
		}

		if (programEditViewFormBean.getProgramId() != null && programEditViewFormBean.getProgramId() > 0) {
			logger.trace("Edit mode");

			Integer progId = programEditViewFormBean.getProgramId();
			String progName = programEditViewFormBean.getName();
			boolean progDisabled = programEditViewFormBean.getIsDisabled();
			List<Integer> selectedBatteryIds = (List<Integer>) (programEditViewFormBean.getSelectedBatteryIdList() == null ? Lists.newArrayList() : programEditViewFormBean.getSelectedBatteryIdList());
			List<Integer> selectedClinicIds = (List<Integer>) (programEditViewFormBean.getSelectedClinicIdList() == null ? Lists.newArrayList() : programEditViewFormBean.getSelectedClinicIdList());
			List<Integer> selectedNoteTitleIds = (List<Integer>) (programEditViewFormBean.getSelectedNoteTitleIdList() == null ? Lists.newArrayList() : programEditViewFormBean.getSelectedNoteTitleIdList());

			programService.updateProgram(progId, progName, progDisabled, selectedBatteryIds, selectedClinicIds, selectedNoteTitleIds);
		} else {
			logger.trace("Add mode");
			Integer programId = programService.createProgram(programEditViewFormBean.getName(), programEditViewFormBean.getIsDisabled(), programEditViewFormBean.getSelectedClinicIdList(), programEditViewFormBean.getSelectedNoteTitleIdList());

			logger.trace("Created new Program with programId: " + programId);
		}

		return "redirect:/dashboard/programListView";
	}

	@Autowired
	public void setBatteryService(BatteryService batteryService) {
		this.batteryService = batteryService;
	}

	@Autowired
	public void setClinicService(ClinicService clinicService) {
		this.clinicService = clinicService;
	}

	@Autowired
	public void setNoteTitleService(NoteTitleService noteTitleService) {
		this.noteTitleService = noteTitleService;
	}

	@Autowired
	public void setProgramService(ProgramService programService) {
		this.programService = programService;
	}

	/**
	 * Initialize and setup page.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/programEditView", method = RequestMethod.GET)
	public String setUpPageProgramEditView(Model model,
			@ModelAttribute ProgramEditViewFormBean programEditViewFormBean,
			@RequestParam(value = "pid", required = false) Integer programId,
			@CurrentUser EscreenUser escreenUser) {

		logger.trace("In setUpPageProgramEditView");
		logger.trace("pid: " + programId);

		populateModel(model);

		if (programId != null) {
			programEditViewFormBean = programService.getProgramEditViewFormBean(programId);
			model.addAttribute("programEditViewFormBean", programEditViewFormBean);
		}

		return "systemTab/programEditView";
	}
}
