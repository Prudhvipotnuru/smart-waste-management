package com.prudhvi.swacch.dtos;

import com.prudhvi.swacch.model.SegregationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WasteCollectionRequest {
    private SegregationStatus segregationStatus;
    private String photoPath;
    private Double latitude;
    private Double longitude;
    private Long collectorId;
    private Long houseId;
}
