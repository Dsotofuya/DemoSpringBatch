package com.demo.springbatch.demobatch.serivice.implementation;

import com.demo.springbatch.demobatch.jpa.entity.Sale;
import com.demo.springbatch.demobatch.jpa.repository.SaleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final SaleRepository saleRepository;

    public DataLoader(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        saleRepository.deleteAll();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<Sale> sales = List.of(
                new Sale(1L, "Laptop Pro", new BigDecimal("1200.50"), today, "Cliente A", "North"),
                new Sale(2L, "Smartphone X", new BigDecimal("899.99"), today, "Cliente B", "South"),
                new Sale(3L, "Monitor 4K", new BigDecimal("450.00"), yesterday, "Cliente C", "East"),
                new Sale(4L, "Teclado Mecánico", new BigDecimal("150.75"), today, "Cliente D", "West"),
                new Sale(5L, "Mouse Gamer", new BigDecimal("75.20"), yesterday, "Cliente A", "North")
        );
        saleRepository.saveAll(sales);
    }
}