package com.ubi.platform.dto;

/**
 * Representation of a categorical distribution count and calculated percentage.
 */
public class CategoryCountDto {

    private String category;
    private Long count;
    private Double percentage;

    public CategoryCountDto() {
    }

    public CategoryCountDto(String category, Long count, Double percentage) {
        this.category = category;
        this.count = count != null ? count : 0L;
        this.percentage = percentage != null ? percentage : 0.0;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
