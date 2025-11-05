package com.demo.springbatch.demobatch.controller;

import com.demo.springbatch.demobatch.serivice.implementation.JobLauncherService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/demo/jobs")
public class ReportController {

    private final JobLauncherService jobLauncherService;

    public ReportController(JobLauncherService jobLauncherService) {
        this.jobLauncherService = jobLauncherService;
    }

    @PostMapping("/launch/sales-report")
    public ResponseEntity<String> launchSalesReportJob(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            jobLauncherService.launchJob(date);
            return ResponseEntity.ok("Job 'generateSaleReportJob' ejecutado - fecha: " + date);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al ejecutar job: " + e.getMessage());
        }
    }
}