package it.unipi.CarRev.config;

import it.unipi.CarRev.interceptor.BotInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/*this class intercept all of the endpoint requests and check for bots*/
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BotInterceptor botInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(botInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns( /*exclude all the pattern of open-api for running it correctly*/
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/favicon.ico",
                        "/error"
                );

    }

}
