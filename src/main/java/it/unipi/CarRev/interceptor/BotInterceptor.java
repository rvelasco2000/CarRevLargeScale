package it.unipi.CarRev.interceptor;

import it.unipi.CarRev.service.Impl.BotDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * class that allow us to intercept all the endpoints request and check
 * if the user is a legitimate or suspicious one
 */
@Component
public class BotInterceptor implements HandlerInterceptor {

    @Autowired
    private BotDetectionService botDetectionService;

    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws IOException {

        String idUser=getIdUser(request);
        if (!botDetectionService.checkForBot(idUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Too many request, you have been blocked for 24H");
            return false;
        }
        return true;
    }
    private static String getIdUser(HttpServletRequest request){
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        String idUser;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
            idUser=request.getHeader("X-Forwarded-For");
            if(idUser==null||idUser.isEmpty())
            {
                idUser = request.getRemoteAddr();
            }
            return idUser;
    }

}
