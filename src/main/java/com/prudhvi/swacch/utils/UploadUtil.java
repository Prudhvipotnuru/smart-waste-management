package com.prudhvi.swacch.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public class UploadUtil {

	public static final Logger logger = LoggerFactory.getLogger(UploadUtil.class);

	public static ResponseEntity<Map<String, String>> uploadProcess(MultipartFile file, JobOperator jobOperator,
			Job job, long headerLength) {
		File batchCsvFile=null;
		try {
			String fileName = file.getOriginalFilename();
			boolean isCsv = fileName != null && fileName.endsWith(".csv");
			boolean isExcel = fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"));

			if (!isCsv && !isExcel) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("status", "FAILED", "error", "Only CSV or Excel (.xlsx/.xls) files allowed"));
			}

			String uploadDir = System.getProperty("user.dir") + "/uploads";
			File dir = new File(uploadDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			// Determine final CSV path for the batch job
			if (isCsv) {
				batchCsvFile = new File(dir, UUID.randomUUID() + "_" + fileName);
				file.transferTo(batchCsvFile);
				logger.debug("Saved CSV to: " + batchCsvFile.getAbsolutePath());
			} else {
				// Convert Excel → CSV
				batchCsvFile = new File(dir, UUID.randomUUID() + "_converted.csv");
				convertExcelToCsv(file, batchCsvFile);
				logger.debug("Converted Excel to CSV: " + batchCsvFile.getAbsolutePath());
			}

			// Trigger Spring Batch job with the CSV path
			long now = System.currentTimeMillis();
			JobParameters params = new JobParametersBuilder()
					.addLong("jobExecutionId", now)
					.addString("filePath", batchCsvFile.getAbsolutePath())
					.addLong("time", now)
					.toJobParameters();

			JobExecution jobExecution = jobOperator.start(job, params);
			logger.debug(jobExecution + " Job status = " + jobExecution.getStatus());

			File errorFile = new File(uploadDir + "/error_records.csv");
			if (errorFile.exists() && errorFile.length() > headerLength) {
				return ResponseEntity.ok(
						Map.of(
								"status", "COMPLETED_WITH_ERRORS",
								"downloadUrl", "/download-errors",
								"jobExecution", jobExecution.getStatus().name(),
								"jobExecutionId",
								String.valueOf(jobExecution.getJobParameters().getLong("jobExecutionId"))));
			}

			return ResponseEntity.ok(
					Map.of("status", "SUCCESS",
							"jobExecution", jobExecution.getStatus().name(),
							"jobExecutionId",
							String.valueOf(jobExecution.getJobParameters().getLong("jobExecutionId"))));

		} catch (Exception e) {
			logger.error("Upload process failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "FAILED", "error", e.getMessage()!=null?e.getMessage():e.getClass().getSimpleName()));
		} finally {
	        if (batchCsvFile != null && batchCsvFile.exists()) {
	            boolean deleted = batchCsvFile.delete();
	            if (!deleted) {
	                logger.warn("Failed to delete temp file: " + batchCsvFile.getAbsolutePath());
	            } else {
	                logger.debug("Deleted temp file: " + batchCsvFile.getAbsolutePath());
	            }
	        }
	    }
	}

	/**
	 * Converts an Excel file (.xlsx or .xls) to CSV using Apache POI.
	 * Reads the first sheet only. Commas within cell values are replaced with
	 * spaces.
	 */
	private static void convertExcelToCsv(MultipartFile excelFile, File csvOutput) throws Exception {
		try (Workbook workbook = WorkbookFactory.create(excelFile.getInputStream());
				PrintWriter writer = new PrintWriter(new FileWriter(csvOutput,StandardCharsets.UTF_8))) {

			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			for (Row row : sheet) {
				int lastCell = row.getLastCellNum();
				if(row==null || row.getLastCellNum()<=0) continue;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < lastCell; i++) {
					Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					// Replace commas inside cell values to avoid CSV corruption
					String value = formatter.formatCellValue(cell).replace(",", " ");
					sb.append(value);
					if (i < lastCell - 1)
						sb.append(",");
				}
				writer.println(sb);
			}
		}
	}
}
