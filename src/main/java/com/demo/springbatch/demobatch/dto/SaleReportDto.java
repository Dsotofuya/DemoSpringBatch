package com.demo.springbatch.demobatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleReportDto {

    private String productName;
    private String customerName;
    private BigDecimal saleAmount;
    private String region;
    private String salesPerson;
    private BigDecimal commission;
}