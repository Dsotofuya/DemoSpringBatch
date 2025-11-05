package com.demo.springbatch.demobatch.jpa.repository;

import com.demo.springbatch.demobatch.jpa.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
