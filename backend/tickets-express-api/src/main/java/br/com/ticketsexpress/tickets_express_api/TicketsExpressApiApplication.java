package br.com.ticketsexpress.tickets_express_api;

import br.com.ticketsexpress.tickets_express_api.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = AppProperties.class)
@EnableScheduling
public class TicketsExpressApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketsExpressApiApplication.class, args);
    }
}
