package com.ubi.platform.dto;

/**
 * Distribution of quotes by pricing model version.
 */
public class PricingVersionCountDto {

    private String pricingVersion;
    private Long count;

    public PricingVersionCountDto() {
    }

    public PricingVersionCountDto(String pricingVersion, Long count) {
        this.pricingVersion = pricingVersion;
        this.count = count != null ? count : 0L;
    }

    public String getPricingVersion() {
        return pricingVersion;
    }

    public void setPricingVersion(String pricingVersion) {
        this.pricingVersion = pricingVersion;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
