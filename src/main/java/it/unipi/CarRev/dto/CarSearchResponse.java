package it.unipi.CarRev.dto;

import java.util.List;

public class CarSearchResponse {

    private List<FrontPageCarSummaryDTO> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public CarSearchResponse() {}

    public CarSearchResponse(List<FrontPageCarSummaryDTO> items, int page, int size,
                             long totalElements, int totalPages) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<FrontPageCarSummaryDTO> getItems() { return items; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }

    public void setItems(List<FrontPageCarSummaryDTO> items) { this.items = items; }
    public void setPage(int page) { this.page = page; }
    public void setSize(int size) { this.size = size; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
