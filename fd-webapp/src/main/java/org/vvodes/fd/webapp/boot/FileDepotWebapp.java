package org.vvodes.fd.webapp.boot;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CorsFilter;
import org.vvodes.fd.def.intf.IFileRepository;
import org.vvodes.fd.storage.LocalDiskFileRepository;

@SpringBootApplication(scanBasePackages = { "org.vvodes.fd.webapp.rest" })
//@EnableSwagger2
public class FileDepotWebapp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(FileDepotWebapp.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FileDepotWebapp.class);
    }

    @Bean
    public IFileRepository getFileRepository() {
        return new LocalDiskFileRepository();
    }

    @Bean
    public ResourceConfig getResourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(false, "org.vvodes.fd.webapp.rest");
        return resourceConfig;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        CorsFilter corsFilter = new CorsFilter(new ProfileCorsConfigSource());
        filterRegistrationBean.setFilter(corsFilter);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}
