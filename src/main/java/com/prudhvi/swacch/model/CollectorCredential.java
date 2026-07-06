package com.prudhvi.swacch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "collector_temp_credentials")
@Data
@NoArgsConstructor
public class CollectorCredential {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String name;
	private String email;
	private String phone;
	private String password;
	private Long jobExecutionId;	
	
	public CollectorCredential(String name, String email, String phone, String password, Long jobExecutionId) {
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.password = password;
		this.jobExecutionId = jobExecutionId;
	}
}
