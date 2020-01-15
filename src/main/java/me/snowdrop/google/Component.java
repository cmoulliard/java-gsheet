package me.snowdrop.google;

import org.apache.maven.model.Model;

import java.net.URL;

public class Component {

    public Model model;
    public Model parentModel;
    public URL repoURL;
    public String groupId;
    public String artifactId;
    public String version;
    public String toSearch;

    public Model getParentModel() {
        return parentModel;
    }

    public void setParentModel(Model parentModel) {
        this.parentModel = parentModel;
    }

    public String getToSearch() {
        return toSearch;
    }

    public void setToSearch(String toSearch) {
        this.toSearch = toSearch;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public URL getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(URL repoURL) {
        this.repoURL = repoURL;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

}
