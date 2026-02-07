package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.ReviewDAO;
import it.unipi.CarRev.dto.ReportedReviewResponseDTO;
import it.unipi.CarRev.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetReportedReviewsServiceImplementation{
    private final ReviewDAO reviewDAO;
    public GetReportedReviewsServiceImplementation(ReviewDAO reviewDAO){
        this.reviewDAO=reviewDAO;
    }

    public Page<ReportedReviewResponseDTO> getReportedReviews(Integer numOfPage){
        PageRequest pageRequest=PageRequest.of(numOfPage,10, Sort.by("report").descending());
        Page<Review> page=reviewDAO.findAll(pageRequest);
        return page.map(this::getDTOfromReview);
    }

    private ReportedReviewResponseDTO getDTOfromReview(Review review){
        ReportedReviewResponseDTO reportedReviewResponseDTO=new ReportedReviewResponseDTO();
        reportedReviewResponseDTO.setReviewId(review.getId());
        reportedReviewResponseDTO.setCarName(review.getCarName());
        reportedReviewResponseDTO.setNOfReports(review.getReport());
        reportedReviewResponseDTO.setText(review.getText());
        return reportedReviewResponseDTO;
    }
}
