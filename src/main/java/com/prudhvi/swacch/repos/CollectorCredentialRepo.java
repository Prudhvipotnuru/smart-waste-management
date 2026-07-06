package com.prudhvi.swacch.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prudhvi.swacch.model.CollectorCredential;

public interface CollectorCredentialRepo extends JpaRepository<CollectorCredential, Integer>{

	List<CollectorCredential> findByJobExecutionId(Long jobExecutionId);

}
