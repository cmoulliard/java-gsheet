package me.snowdrop.google;

public class Configuration {

    public String mavenCentralRepo;
    public String mavenRedHatRepo;
    public String gavRange;
    public String cellUpPomURL;
    public String cellUpComponentURL;
    public String cellDownPomURL;
    public String cellDownComponentURL;

    public String getCellUpPomURL() {
        return cellUpPomURL;
    }

    public void setCellUpPomURL(String cellUpPomURL) {
        this.cellUpPomURL = cellUpPomURL;
    }

    public String getCellUpComponentURL() {
        return cellUpComponentURL;
    }

    public void setCellUpComponentURL(String cellUpComponentURL) {
        this.cellUpComponentURL = cellUpComponentURL;
    }

    public String getCellDownPomURL() {
        return cellDownPomURL;
    }

    public void setCellDownPomURL(String cellDownPomURL) {
        this.cellDownPomURL = cellDownPomURL;
    }

    public String getCellDownComponentURL() {
        return cellDownComponentURL;
    }

    public void setCellDownComponentURL(String cellDownComponentURL) {
        this.cellDownComponentURL = cellDownComponentURL;
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
