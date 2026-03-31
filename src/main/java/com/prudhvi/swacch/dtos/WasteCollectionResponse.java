package com.prudhvi.swacch.dtos;

import java.time.LocalDateTime;

import com.prudhvi.swacch.model.SegregationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WasteCollectionResponse {
    private SegregationStatus segregationStatus;
    private String photoPath;
    private Double latitude;
    private Double longitude;
    private LocalDateTime collectedAt;
    private Long collectorId;
	private String collectorName;
    private Long houseId;
    private String houseNumber;
}
