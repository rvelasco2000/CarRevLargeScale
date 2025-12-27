package it.unipi.CarRev.interceptor;

import it.unipi.CarRev.service.Impl.BotDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class BotInterceptor implements HandlerInterceptor {

    @Autowired
    private BotDetectionService botDetectionService;

    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws IOException {
            String idUser = request.getRemoteAddr();

            if (!botDetectionService.checkForBot(idUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Too many request, you have been blocked for 24H");
                return false;
            }
        return true;
    }

}
