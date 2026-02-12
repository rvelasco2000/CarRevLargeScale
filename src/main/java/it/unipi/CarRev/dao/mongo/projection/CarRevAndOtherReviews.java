package it.unipi.CarRev.dao.mongo.projection;

import org.bson.Document;

import java.util.List;

public interface CarRevAndOtherReviews {
    List<Document> getTopTenReview();
    List<Document> getOtherReview();
}
