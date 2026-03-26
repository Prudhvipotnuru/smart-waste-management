package com.prudhvi.swacch.dtos;

import com.prudhvi.swacch.model.SegregationStatus;

import lombok.Data;

@Data
public class WasteCollectionResponse {
	private Long collectorId;
	private String collectorName;
    private Long houseId;
    private String houseNumber;
    private SegregationStatus segregationStatus;
    private String photoPath;
    private Double latitude;
    private Double longitude;
}
