package com.prudhvi.swacch.dtos;

import lombok.Data;

@Data
public class DashBoardResponse {
	private Long houseCount;
	private Long wasteCount;
	private Long segregatedCount;
	private Long nonSegregatedCount;
}
