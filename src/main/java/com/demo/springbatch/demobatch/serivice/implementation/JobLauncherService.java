package com.demo.springbatch.demobatch.serivice.implementation;

import com.demo.springbatch.demobatch.serivice.IJobLauncherService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class JobLauncherService implements IJobLauncherService {

    private final JobLauncher jobLauncher;
    private final Job generateSaleReportJob;

    @Value("${app.reports.base-path}")
    private String reportsBasePath;

    public JobLauncherService(JobLauncher jobLauncher, @Qualifier("generateSaleReportJob") Job generateSaleReportJob) {
        this.jobLauncher = jobLauncher;
        this.generateSaleReportJob = generateSaleReportJob;
    }

    @Override
    public void launchJob(LocalDate reportDate) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        String formattedDate = reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String outputPath = reportsBasePath + "reporte-ventas-demo-" + formattedDate + ".csv";

        // Crear directorio si no existe (buena práctica)
        Path reportPath = Paths.get(reportsBasePath);
        Files.createDirectories(reportPath);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("reportDate", reportDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .addString("outputPath", outputPath)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(generateSaleReportJob, jobParameters);
    }
}