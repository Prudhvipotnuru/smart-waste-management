package com.prudhvi.swacch.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.prudhvi.swacch.dtos.DashBoardResponse;
import com.prudhvi.swacch.dtos.WasteCollectionRequest;
import com.prudhvi.swacch.dtos.WasteCollectionResponse;
import com.prudhvi.swacch.model.House;
import com.prudhvi.swacch.model.SegregationStatus;
import com.prudhvi.swacch.model.User;
import com.prudhvi.swacch.model.WasteCollection;
import com.prudhvi.swacch.repos.HouseRepo;
import com.prudhvi.swacch.repos.UserRepo;
import com.prudhvi.swacch.repos.WasteCollectionRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class WasteService {

	private WasteCollectionRepo wrepo;
	private UserRepo urepo;
	private HouseRepo hrepo;
	private PixelBinService pixelBin;

	WasteService(WasteCollectionRepo repo, UserRepo urepo, HouseRepo hrepo, PixelBinService pixelBin) {
		this.wrepo = repo;
		this.urepo = urepo;
		this.hrepo = hrepo;
		this.pixelBin = pixelBin;
	}

	private List<WasteCollectionResponse> processWasteList(List<WasteCollection> list) {
		List<WasteCollectionResponse> response = new ArrayList<WasteCollectionResponse>();
		for (WasteCollection waste : list) {
			WasteCollectionResponse resp = processWasteResponse(waste);
			response.add(resp);
		}
		return response;
	}

	private WasteCollectionResponse processWasteResponse(WasteCollection waste) {
		WasteCollectionResponse response = new WasteCollectionResponse();
		response.setCollectedAt(waste.getCollectedAt());
		response.setCollectorId(waste.getCollector().getId());
		response.setCollectorName(waste.getCollector().getName());
		response.setHouseId(waste.getHouse().getId());
		response.setHouseNumber(waste.getHouse().getHouseNumber());
		response.setLatitude(waste.getLatitude());
		response.setLongitude(waste.getLongitude());
		response.setSegregationStatus(waste.getSegregationStatus());
		response.setPhotoPath(waste.getPhotoPath());
		return response;
	}

	@PreAuthorize("hasRole('ROLE_COLLECTOR')")
	@Transactional
	public WasteCollectionResponse save(WasteCollectionRequest request, Authentication auth) {
		WasteCollection waste = new WasteCollection();
		waste.setSegregationStatus(request.getSegregationStatus());
		waste.setLatitude(request.getLatitude());
		waste.setLongitude(request.getLongitude());

		User user = urepo.findByName(auth.getName()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
		waste.setCollector(user);

		House house = hrepo.findByQrCodeValue(request.getHouseqr())
				.orElseThrow(() -> new EntityNotFoundException("House not found"));

		waste.setHouse(house);

		// Optional: upload photo to PixelBin if provided
		if (request.getPhotoPath() != null && !request.getPhotoPath().isEmpty()) {
			waste.setPhotoPath(uploadPhoto(request.getPhotoPath(), house.getId()));
		}

		WasteCollection resp = wrepo.save(waste);
		return processWasteResponse(resp);
	}

	/**
	 * Uploads the base64 photo submitted by the collector to PixelBin and
	 * returns the public CDN url that is stored in the database (instead of
	 * a local /uploads/photos path on the server's file system).
	 */
	private String uploadPhoto(String base64Photo, Long houseId) {
		String contentType = "image/png";
		String base64Data = base64Photo;

		// Parse optional "data:<mime>;base64," prefix sent by the collector app
		if (base64Photo.startsWith("data:")) {
			int comma = base64Photo.indexOf(',');
			String meta = comma >= 0 ? base64Photo.substring(0, comma) : "";
			int semicolon = meta.indexOf(';');
			if (meta.length() > 5) {
				contentType = meta.substring(5, semicolon < 0 ? meta.length() : semicolon);
			}
			if (comma >= 0) {
				base64Data = base64Photo.substring(comma + 1);
			}
		}

		byte[] data = Base64.getDecoder().decode(base64Data);

		String extension = "png";
		int slash = contentType.indexOf('/');
		if (slash >= 0 && contentType.length() > slash + 1) {
			extension = contentType.substring(slash + 1);
		}
		String filename = "house_" + houseId + "_" + System.currentTimeMillis() + "." + extension;

		return pixelBin.upload(data, filename, contentType);
	}

	public List<WasteCollectionResponse> getAll() {
		List<WasteCollection> list = wrepo.findAll();
		return processWasteList(list);
	}

	public List<WasteCollectionResponse> getWasteByHouseId(Long id) {
		List<WasteCollection> list = wrepo.findByHouseId(id);
		return processWasteList(list);
	}

	public List<WasteCollectionResponse> getWasteByCollectorId(Long id) {
		List<WasteCollection> list = wrepo.findByCollectorId(id);
		return processWasteList(list);
	}

	public DashBoardResponse dashboard() {
		DashBoardResponse response = new DashBoardResponse();
		response.setHouseCount(hrepo.count());
		response.setWasteCount(wrepo.count());
		response.setSegregatedCount(wrepo.countBySegregationStatus(SegregationStatus.SEGREGATED));
		response.setNonSegregatedCount(wrepo.countBySegregationStatus(SegregationStatus.NOT_SEGREGATED));
		return response;
	}

	public Page<WasteCollectionResponse> getWasteByCollectorIdAndCurrentDate(Authentication auth, int page,
			int size,
			// @RequestParam(defaultValue = "collectedAt,desc") String sort, // e.g.
			// "houseNumber,asc"
			String status // SEGREGATED / NOT_SEGREGATED
			, String date) {
		Long userId = urepo.findIdByName(auth.getName());
		if (userId == null) {
			throw new EntityNotFoundException("User Not Found");
		}
		PageRequest pageable = PageRequest.of(page, size);
		Page<WasteCollection> wasteCollections;
		boolean hasDate = date != null && !date.isBlank();
		if (status != null && !status.isBlank()) {
			SegregationStatus segStatus;
			try {
				segStatus = SegregationStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid segregation status", e);
			}
			if (!hasDate) {
				wasteCollections = wrepo.findByCollectorIdAndSegregationStatus(userId, pageable, segStatus);
			} else {
				wasteCollections = wrepo.findByCollectorIdAndSegregationStatusAndCollectedAtBetween(userId, pageable,
						segStatus,
						LocalDate.parse(date).atStartOfDay(), LocalDate.parse(date).plusDays(1).atStartOfDay());
			}
		} else {
			if (hasDate) {
				wasteCollections = wrepo.findByCollectorIdAndCollectedAtBetween(userId, pageable,
						LocalDate.parse(date).atStartOfDay(), LocalDate.parse(date).plusDays(1).atStartOfDay());
			} else {
				wasteCollections = wrepo.findByCollectorId(userId, pageable);
			}
		}
		return wasteCollections.map(this::processWasteResponse);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Page<WasteCollectionResponse> getAllCollections(Authentication auth, int page, int size, String status,
			String date, Long collectorId) {
		PageRequest pageable = PageRequest.of(page, size);
		Page<WasteCollection> wasteCollections;
		boolean hasDate = date != null && !date.isBlank();
		if (status != null && !status.isBlank()) {
			SegregationStatus segStatus;
			try {
				segStatus = SegregationStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid segregation status", e);
			}

			if (hasDate) {
				LocalDateTime start = LocalDate.parse(date).atStartOfDay();
				LocalDateTime end = LocalDate.parse(date).plusDays(1).atStartOfDay();
				if (collectorId != null) {
					wasteCollections = wrepo.findByCollectorIdAndSegregationStatusAndCollectedAtBetween(collectorId,
							pageable, segStatus, start, end);
					} else {
						wasteCollections = wrepo.findBySegregationStatusAndCollectedAtBetween(pageable, segStatus, start,
								end);
					}
			} else {
				if (collectorId != null) {
					wasteCollections = wrepo.findByCollectorIdAndSegregationStatus(collectorId, pageable, segStatus);
				} else {
					wasteCollections = wrepo.findBySegregationStatus(pageable, segStatus);
				}
			}
		} else {
			if (hasDate) {
				LocalDateTime start = LocalDate.parse(date).atStartOfDay();
				LocalDateTime end = LocalDate.parse(date).plusDays(1).atStartOfDay();
				if (collectorId != null) {
					wasteCollections = wrepo.findByCollectorIdAndCollectedAtBetween(collectorId, pageable, start, end);
				} else {
					wasteCollections = wrepo.findByCollectedAtBetween(pageable, start, end);
				}
			} else {
				if (collectorId != null) {
					wasteCollections = wrepo.findByCollectorId(collectorId, pageable);
				} else {
					wasteCollections = wrepo.findAll(pageable);
				}
			}
		}
		return wasteCollections.map(this::processWasteResponse);
	}

}
