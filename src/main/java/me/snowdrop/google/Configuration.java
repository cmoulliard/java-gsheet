package me.snowdrop.google;

public class Configuration {

    public String mavenCentralRepo;
    public String mavenRedHatRepo;
    public String gavRange;
    public String cellUpstreamVersion;
    public String cellUpstreamURL;
    public String cellDownstreamVersion;
    public String cellDownstreamURL;

    public String getCellUpstreamURL() {
        return cellUpstreamURL;
    }

    public void setCellUpstreamURL(String cellUpstreamURL) {
        this.cellUpstreamURL = cellUpstreamURL;
    }

    public String getCellDownstreamVersion() {
        return cellDownstreamVersion;
    }

    public void setCellDownstreamVersion(String cellDownstreamVersion) {
        this.cellDownstreamVersion = cellDownstreamVersion;
    }

    public String getCellDownstreamURL() {
        return cellDownstreamURL;
    }

    public void setCellDownstreamURL(String cellDownstreamURL) {
        this.cellDownstreamURL = cellDownstreamURL;
    }

    public String getCellUpstreamVersion() {
        return cellUpstreamVersion;
    }

    public void setCellUpstreamVersion(String cellUpstreamVersion) {
        this.cellUpstreamVersion = cellUpstreamVersion;
    }

    public String getGavRange() {
        return gavRange;
    }

    public void setGavRange(String gavRange) {
        this.gavRange = gavRange;
    }

    public String getMavenCentralRepo() {
        return mavenCentralRepo;
    }

    public void setMavenCentralRepo(String mavenCentralRepo) {
        this.mavenCentralRepo = mavenCentralRepo;
    }

    public String getMavenRedHatRepo() {
        return mavenRedHatRepo;
    }

    public void setMavenRedHatRepo(String mavenRedHatRepo) {
        this.mavenRedHatRepo = mavenRedHatRepo;
    }
}
