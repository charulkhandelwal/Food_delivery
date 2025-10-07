package com.example.food_delivery.Model;

public class DataModel {
    private boolean aadhaarDone;
    private String aadhaarFrontUri;
    private String aadhaarBackUri;

    private boolean panDone;
    private String panFrontUri;
    private String panBackUri;

    private boolean licenseDone;
    private String licenseFrontUri;
    private String licenseBackUri;

    private boolean rcDone;
    private String rcFrontUri;
    private String rcBackUri;

    // Aadhaar
    public boolean isAadhaarDone() { return aadhaarDone; }
    public void setAadhaarDone(boolean aadhaarDone) { this.aadhaarDone = aadhaarDone; }

    public String getAadhaarFrontUri() { return aadhaarFrontUri; }
    public void setAadhaarFrontUri(String aadhaarFrontUri) { this.aadhaarFrontUri = aadhaarFrontUri; }

    public String getAadhaarBackUri() { return aadhaarBackUri; }
    public void setAadhaarBackUri(String aadhaarBackUri) { this.aadhaarBackUri = aadhaarBackUri; }

    // PAN
    public boolean isPanDone() { return panDone; }
    public void setPanDone(boolean panDone) { this.panDone = panDone; }

    public String getPanFrontUri() { return panFrontUri; }
    public void setPanFrontUri(String panFrontUri) { this.panFrontUri = panFrontUri; }

    public String getPanBackUri() { return panBackUri; }
    public void setPanBackUri(String panBackUri) { this.panBackUri = panBackUri; }

    // License
    public boolean isLicenseDone() { return licenseDone; }
    public void setLicenseDone(boolean licenseDone) { this.licenseDone = licenseDone; }

    public String getLicenseFrontUri() { return licenseFrontUri; }
    public void setLicenseFrontUri(String licenseFrontUri) { this.licenseFrontUri = licenseFrontUri; }

    public String getLicenseBackUri() { return licenseBackUri; }
    public void setLicenseBackUri(String licenseBackUri) { this.licenseBackUri = licenseBackUri; }

    // RC
    public boolean isRcDone() { return rcDone; }
    public void setRcDone(boolean rcDone) { this.rcDone = rcDone; }

    public String getRcFrontUri() { return rcFrontUri; }
    public void setRcFrontUri(String rcFrontUri) { this.rcFrontUri = rcFrontUri; }

    public String getRcBackUri() { return rcBackUri; }
    public void setRcBackUri(String rcBackUri) { this.rcBackUri = rcBackUri; }
}
