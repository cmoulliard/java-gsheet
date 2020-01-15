package me.snowdrop.google;

public class Configuration {

    public String mavenCentralRepo;
    public String mavenRedHatRepo;
    public String gavRange;

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
