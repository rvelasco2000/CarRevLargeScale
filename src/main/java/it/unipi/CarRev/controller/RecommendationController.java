package it.unipi.CarRev.controller;

import it.unipi.CarRev.service.Neo4jRecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final Neo4jRecommendationService recommendationService;

    public RecommendationController(Neo4jRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }


    @GetMapping("/me")
    public List<Map<String, Object>> recommendForMe(Authentication authentication) {
        String username = authentication.getName(); // preso dal JWT
        return recommendationService.recommendForUser(username);
    }
}
