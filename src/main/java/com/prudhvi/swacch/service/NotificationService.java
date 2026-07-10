package com.prudhvi.swacch.service;

public interface NotificationService {
	public void sendCollectorCredentials(String toEmail, String name, String tempPassword);
	
	public void sendCollectorCredentials(String toEmail, String name);
}
