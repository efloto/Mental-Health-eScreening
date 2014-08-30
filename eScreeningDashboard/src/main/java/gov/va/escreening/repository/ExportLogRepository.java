package gov.va.escreening.repository;

import java.util.List;

import gov.va.escreening.entity.ExportLog;

public interface ExportLogRepository extends RepositoryInterface<ExportLog> {

	List<ExportLog> findAllForDays(int noOfDays);
}