package com.demo.springbatch.demobatch.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Sale {

    @Id
    private Long id;
    private String productName;
    private BigDecimal amount;
    private LocalDate saleDate;
    private String customerName;
    private String region;
}