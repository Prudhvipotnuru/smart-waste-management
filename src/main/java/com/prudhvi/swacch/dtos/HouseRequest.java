package com.prudhvi.swacch.dtos;

import lombok.Data;

@Data
public class HouseRequest {
	private String houseNumber;
    private String ownerName;
    private String address;
    private String ward;
}
