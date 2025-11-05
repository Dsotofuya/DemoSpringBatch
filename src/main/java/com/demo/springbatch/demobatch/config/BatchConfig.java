package com.demo.springbatch.demobatch.config;

import com.demo.springbatch.demobatch.dto.SaleReportDto;
import com.demo.springbatch.demobatch.jpa.entity.Sale;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope // Esencial para la inyección tardía de parámetros del job
    public JpaPagingItemReader<Sale> reader(@Value("#{jobParameters['reportDate']}") String reportDateStr) {
        LocalDate reportDate = LocalDate.parse(reportDateStr, DateTimeFormatter.ISO_LOCAL_DATE);

        return new JpaPagingItemReaderBuilder<Sale>()
                .name("saleReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Sale s WHERE s.saleDate = :reportDate ORDER BY s.id")
                .parameterValues(Map.of("reportDate", reportDate))
                .pageSize(100) // Tamaño del chunk de lectura
                .build();
    }

    @Bean
    public ItemProcessor<Sale, SaleReportDto> processor() {
        return sale -> {
            // Lógica de negocio: asignar vendedor y calcular comisión
            String salesPerson = switch (sale.getRegion()) {
                case "North" -> "Juan";
                case "South" -> "Jose";
                case "East" -> "Miguel";
                case "West" -> "Sara";
                default -> "N/A";
            };

            BigDecimal commissionRate = switch (sale.getRegion()) {
                case "North", "East" -> new BigDecimal("0.10");
                case "South", "West" -> new BigDecimal("0.12");
                default -> BigDecimal.ZERO;
            };

            BigDecimal commission = sale.getAmount().multiply(commissionRate);

            return new SaleReportDto(
                    sale.getProductName(),
                    sale.getCustomerName(),
                    sale.getAmount(),
                    sale.getRegion(),
                    salesPerson,
                    commission
            );
        };
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<SaleReportDto> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
        BeanWrapperFieldExtractor<SaleReportDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"productName", "customerName", "saleAmount", "region", "salesPerson", "commission"});

        DelimitedLineAggregator<SaleReportDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<SaleReportDto>()
                .name("saleReportWriter")
                .resource(new FileSystemResource(outputPath))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("Producto,Cliente,Monto,Region,Vendedor,Comision"))
                .build();
    }

    @Bean
    public Step generateSaleReportStep() {
        return new StepBuilder("generateSaleReportStep", jobRepository)
                .<Sale, SaleReportDto>chunk(10, transactionManager)
                .reader(reader(null))
                .faultTolerant()
                .skip(FlatFileParseException.class) // Ejemplo: saltar si hay un error de parseo en un reader de archivos
                .skipLimit(10)
                .retry(OptimisticLockingFailureException.class)
                .retryLimit(3)
                .processor(processor())
                .writer(writer(null))
                .build();
    }

    @Bean
    public Job generateSaleReportJob() {
        return new JobBuilder("generateSaleReportJob", jobRepository)
                .start(generateSaleReportStep())
                .build();
    }
}