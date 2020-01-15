package me.snowdrop.google;

import org.apache.maven.model.Model;

import java.net.URL;

public class Component {

    public Model parentModel;
    public String parentPomContent;
    public URL parentRepoURL;

    public Model model;
    public String pomContent;
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

    public String getParentPomContent() {
        return parentPomContent;
    }

    public void setParentPomContent(String parentPomContent) {
        this.parentPomContent = parentPomContent;
    }

    public URL getParentRepoURL() {
        return parentRepoURL;
    }

    public void setParentRepoURL(URL parentRepoURL) {
        this.parentRepoURL = parentRepoURL;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getPomContent() {
        return pomContent;
    }

    public void setPomContent(String pomContent) {
        this.pomContent = pomContent;
    }

    public URL getRepoURL() {
        return repoURL;
    }

    public void setRepoURL(URL repoURL) {
        this.repoURL = repoURL;
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

    public String getToSearch() {
        return toSearch;
    }

    public void setToSearch(String toSearch) {
        this.toSearch = toSearch;
    }

}
