package com.prudhvi.swacch.dtos;

import lombok.Data;

@Data
public class HouseResponse {
	private Long id;
    private String houseNumber;
    private String ownerName;
    private String address;
    private String ward;
    private String qrCodeValue;
}
