package de.summerfeeling.labyemoteplayer;

import com.google.gson.annotations.Expose;

public class Configuration {
	
	@Expose
	private String language = "en";
	
	@Expose
	private boolean forcefieldEnabled = true;
	@Expose
	private double forcefieldRadius = 2.2F;
	
	@Expose
	private double viewDistance = 40;
	
	@Expose
	private boolean positionFixEnabled = true;
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public boolean isForcefieldEnabled() {
		return forcefieldEnabled;
	}
	
	public void setForcefieldEnabled(boolean forcefieldEnabled) {
		this.forcefieldEnabled = forcefieldEnabled;
	}
	
	public double getForcefieldRadius() {
		return forcefieldRadius;
	}
	
	public void setForcefieldRadius(double forcefieldRadius) {
		this.forcefieldRadius = forcefieldRadius;
	}
	
	public double getViewDistance() {
		return viewDistance;
	}
	
	public void setViewDistance(double viewDistance) {
		this.viewDistance = viewDistance;
	}
	
	public boolean isPositionFixEnabled() {
		return positionFixEnabled;
	}
	
	public void setPositionFixEnabled(boolean positionFixEnabled) {
		this.positionFixEnabled = positionFixEnabled;
	}
}
