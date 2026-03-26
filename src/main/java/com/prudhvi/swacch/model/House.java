package com.prudhvi.swacch.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "houses")
@AllArgsConstructor
@NoArgsConstructor
public class House {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false,unique=true)
    private String houseNumber;

	@Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String ward;

    @Column(nullable = false, unique = true)
    private String qrCodeValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "house",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<WasteCollection> wasteCollections=new ArrayList<WasteCollection>();
    
    @Transient
    private boolean error;
    @Transient
    private String errorDesc;
}
