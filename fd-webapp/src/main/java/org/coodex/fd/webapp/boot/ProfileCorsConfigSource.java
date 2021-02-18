package org.coodex.fd.webapp.boot;

import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

public class ProfileCorsConfigSource implements CorsConfigurationSource {
    private static Logger log = LoggerFactory.getLogger(ProfileCorsConfigSource.class);

    private static Profile profile = Profile.get("cors_setting.properties");

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        printRequest(request, profile.getBool("debug", false));
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(profile.getBool("allow-credentials", true));
        corsConfiguration.setMaxAge(profile.getLong("max-age", 3600));
        String[] list = profile.getStrList("allowed-headers");
        if (list != null) {
            for (String s : list) {
                corsConfiguration.addAllowedHeader(s);
            }
        }
        list = profile.getStrList("allowed-methods");
        if (list != null) {
            for (String s : list) {
                corsConfiguration.addAllowedMethod(s.toUpperCase());
            }
        }
        list = profile.getStrList("exposed-headers");
        if (list != null) {
            for (String s : list) {
                corsConfiguration.addExposedHeader(s);
            }
        }
        String[] allowedOrigins = profile.getStrList("allowed-origins", ",", new String[]{"*"});
        String origin = request.getHeader("Origin");
        for (String s : allowedOrigins) {
            if (s.equals("*") || s.equalsIgnoreCase(origin)) {
                corsConfiguration.addAllowedOrigin(origin);
                break;
            }
        }
        return corsConfiguration;
    }

    private void printRequest(HttpServletRequest request, boolean debug) {
        if (debug) {
            log.debug("Request URI: {}", request.getRequestURI());
            log.debug("Request method: {}", request.getMethod());
            ServletServerHttpRequest serverRequest = new ServletServerHttpRequest(request);
            HttpHeaders httpHeaders = serverRequest.getHeaders();
            for (Iterator<String> it = httpHeaders.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                log.debug("Header: {} = {}", key, listToStr(httpHeaders.get(key)));
            }
            log.debug("CORS filter end.");
        }
    }

    private String listToStr(List<?> list) {
        StringBuilder sb = new StringBuilder(4);
        boolean isFirst = true;
        for (Object o : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(",");
            }
            if (o instanceof String) {
                sb.append(o);
            } else {
                sb.append(o.toString());
            }
        }
        return sb.toString();
    }
}
