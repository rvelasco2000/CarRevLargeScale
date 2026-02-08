package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.CarAnalyticsDAO;
import it.unipi.CarRev.dto.CarAnalyticsBetweenDatesResponseDTO;
import it.unipi.CarRev.dto.CarAnalyticsTopCarDTO;
import it.unipi.CarRev.model.CarAnalytics;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CarAnalyticsBetweenDatesService {

    private final CarAnalyticsDAO carAnalyticsDAO;

    public CarAnalyticsBetweenDatesService(CarAnalyticsDAO carAnalyticsDAO) {
        this.carAnalyticsDAO = carAnalyticsDAO;
    }

    public CarAnalyticsBetweenDatesResponseDTO getTopCarsBetween(String startDate, String endDate) {
        Optional<CarAnalytics> startOpt = carAnalyticsDAO.findByData(startDate);
        Optional<CarAnalytics> endOpt = carAnalyticsDAO.findByData(endDate);

        if (startOpt.isEmpty() || endOpt.isEmpty()) {
            return null;
        }

        Map<String, CarAnalytics.CarInfo> startMap = mapByCarId(startOpt.get());
        Map<String, CarAnalytics.CarInfo> endMap = mapByCarId(endOpt.get());

        CarAnalyticsTopCarDTO mostViewed = null;
        CarAnalyticsTopCarDTO mostReviewed = null;

        for (Map.Entry<String, CarAnalytics.CarInfo> entry : endMap.entrySet()) {
            String carId = entry.getKey();
            CarAnalytics.CarInfo endInfo = entry.getValue();
            CarAnalytics.CarInfo startInfo = startMap.get(carId);

            long startViews = startInfo == null || startInfo.getViews() == null ? 0L : startInfo.getViews();
            long endViews = endInfo.getViews() == null ? 0L : endInfo.getViews();
            long viewsDiff = endViews - startViews;

            int startReviews = startInfo == null || startInfo.getNumReviews() == null ? 0 : startInfo.getNumReviews();
            int endReviews = endInfo.getNumReviews() == null ? 0 : endInfo.getNumReviews();
            int reviewsDiff = endReviews - startReviews;

            CarAnalyticsTopCarDTO candidate = new CarAnalyticsTopCarDTO(
                carId,
                endInfo.getCarName(),
                endInfo.getCarBrand(),
                endInfo.getCarModel(),
                viewsDiff,
                reviewsDiff
            );

            if (mostViewed == null || candidate.getViewsCount() > mostViewed.getViewsCount()) {
                mostViewed = candidate;
            }
            if (mostReviewed == null || candidate.getReviewsCount() > mostReviewed.getReviewsCount()) {
                mostReviewed = candidate;
            }
        }

        return new CarAnalyticsBetweenDatesResponseDTO(startDate, endDate, mostViewed, mostReviewed);
    }

    private Map<String, CarAnalytics.CarInfo> mapByCarId(CarAnalytics analytics) {
        Map<String, CarAnalytics.CarInfo> map = new HashMap<>();
        if (analytics.getCarsInfo() == null) {
            return map;
        }
        for (CarAnalytics.CarInfo info : analytics.getCarsInfo()) {
            if (info.getCarId() != null) {
                map.put(info.getCarId(), info);
            }
        }
        return map;
    }
}
