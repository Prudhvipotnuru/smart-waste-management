package com.prudhvi.swacch.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prudhvi.swacch.model.SegregationStatus;
import com.prudhvi.swacch.model.WasteCollection;

public interface WasteCollectionRepo extends JpaRepository<WasteCollection, Long>{

	List<WasteCollection> findByHouseId(Long id);

	List<WasteCollection> findByCollectorId(Long id);

	Long countBySegregationStatus(SegregationStatus status);

}
