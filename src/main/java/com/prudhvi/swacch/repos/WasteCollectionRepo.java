package com.prudhvi.swacch.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.prudhvi.swacch.model.SegregationStatus;
import com.prudhvi.swacch.model.WasteCollection;

public interface WasteCollectionRepo extends JpaRepository<WasteCollection, Long>{

	List<WasteCollection> findByHouseId(Long id);

	List<WasteCollection> findByCollectorId(Long id);

	Long countBySegregationStatus(SegregationStatus status);

	Page<WasteCollection> findByCollectorIdAndCollectedAtBetween(Long id, PageRequest pageable, LocalDateTime atStartOfDay,
			LocalDateTime atStartOfDay2);

	Page<WasteCollection> findByCollectorId(Long id, PageRequest pageable);

	Page<WasteCollection> findByCollectorIdAndSegregationStatus(Long id, PageRequest pageable, SegregationStatus status);

	Page<WasteCollection> findByCollectorIdAndSegregationStatusAndCollectedAtBetween(Long id, PageRequest pageable,
			SegregationStatus valueOf, LocalDateTime atStartOfDay, LocalDateTime atStartOfDay2);

}
