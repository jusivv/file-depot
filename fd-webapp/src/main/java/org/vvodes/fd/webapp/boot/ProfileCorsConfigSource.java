package org.vvodes.fd.webapp.boot;

import org.coodex.util.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class ProfileCorsConfigSource implements CorsConfigurationSource {
    private static Logger log = LoggerFactory.getLogger(ProfileCorsConfigSource.class);

    private static Profile profile = Profile.get("cors_setting.properties");

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(profile.getBool("allow-credentials", true));
        corsConfiguration.setMaxAge(profile.getLong("max-age", 3600));
        String[] list = profile.getStrList("allowed-headers");
        if (list != null) {
            corsConfiguration.setAllowedHeaders(Arrays.asList(list));
        }
        list = profile.getStrList("allowed-methods");
        if (list != null) {
            corsConfiguration.setAllowedMethods(Arrays.asList(list));
        }
        list = profile.getStrList("exposed-headers");
        if (list != null) {
            corsConfiguration.setAllowedMethods(Arrays.asList(list));
        }
        String[] allowedOrigins = profile.getStrList("allowed-origins", ",", new String[]{"*"});
        String origin = request.getHeader("Origin");
        for (String s : allowedOrigins) {
            if (s.equals("*") || s.equalsIgnoreCase(origin)) {
                log.debug("CORS Origin matched {} with {}", origin, s);
                corsConfiguration.setAllowedOrigins(Arrays.asList(new String[]{origin}));
                break;
            }
        }
        return corsConfiguration;
    }
}
