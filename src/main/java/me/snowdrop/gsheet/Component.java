package me.snowdrop.gsheet;

import org.apache.maven.model.Model;

import java.net.URL;

public class Component {

    public Model model;
    public URL repoURL;

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
