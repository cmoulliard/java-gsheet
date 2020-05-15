package me.snowdrop.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

public class GSheetApplication {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CONFIGURATION_FILE = "config.json";

    // Directory to store user credentials for this application.
    private static final java.io.File CREDENTIALS_FOLDER //
            = new java.io.File(System.getProperty("user.home"), "credentials");
    private static final String CLIENT_SECRET_FILE_NAME = "client_secrets.json";

    // Scope to access the GSheets
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    // Google Sheet ID
    private static String GSHEET_ID;

    // configuration
    private static Configuration configuration;

    private static String mavenRepo;

    private static String SpringBootDependenciesGroupId = "org.springframework.boot";
    private static String SpringBootDependenciesArtifactId = "spring-boot-dependencies";

    public static void main(String... args) throws IOException, GeneralSecurityException, XmlPullParserException {

        if (args.length == 0) {
            System.out.println("Proper Usage is: java -jar ./maven-gsheet-1.0-SNAPSHOT-jar-with-dependencies.jar sheet_id");
            System.exit(0);
        } else {
            GSHEET_ID = args[0];
        }

        // Collect information such as maven repositories from the json configuration file
        init();

        // Check if we will parse the information from the Snapshot maven repo or not
        if (configuration.isSnapshot.equals("Yes")) {
            mavenRepo = configuration.mavenSpringSnapshotRepo;
        } else {
            mavenRepo = configuration.mavenCentralRepo;
        }

        // Create the Google Sheet service able to communicate with the Sheet
        Sheets gSheet = createService();

        // Read the Cells content of the range "A:D"
        // For each row, parse the POM file using the repo + groupID, artifactID and Version
        // Next, search the component using its name to find the version
        readCells(gSheet);
    }

    static void init() throws IOException {
        URL configURL = GSheetApplication.class.getClassLoader().getResource(CONFIGURATION_FILE);
        if (configURL == null) {
            throw new IllegalArgumentException(CONFIGURATION_FILE + " file not found!");
        }
        // Parse the configuration file to setup the parameters
        ObjectMapper mapper = new ObjectMapper();
        configuration = mapper.readValue(configURL, Configuration.class);
    }

    static Sheets createService() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Create a Service to read, update cells
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                //.setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    static void readCells(Sheets service) throws IOException, XmlPullParserException {
        // Read Cells
        ValueRange response = service.spreadsheets().values()
                .get(GSHEET_ID, configuration.gavRange)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                try {
                    Component component = new Component();
                    component.toSearch = (String) row.get(4);

                    if ((String) row.get(2) != "") {
                        if (!component.toSearch.contentEquals("??")) {
                            System.out.printf("Component : %s\n", component.toSearch);

                            // Fetch POM file content using
                            // - GAV defined within the G Sheet
                            // - maven repository where to look
                            component.setRepoURL(populateMavenRepoURL(mavenRepo, (String) row.get(0), (String) row.get(1), (String) row.get(2)));
                            component.setPomContent(fetchContent(component.repoURL));
                            component.setModel(populateModel(component.pomContent));
                            System.out.printf("Maven URL : %s\n", component.repoURL);

                            // Check if there is SB BOM dependencies
                            DependencyManagement dependencyManagement = component.getModel().getDependencyManagement();
                            if (dependencyManagement!= null && dependencyManagement.getDependencies().size() > 0) {
                                for (Dependency dep : dependencyManagement.getDependencies()) {
                                    if (dep.getGroupId().equals(SpringBootDependenciesGroupId) && dep.getArtifactId().equals(SpringBootDependenciesArtifactId)) {
                                        // Get BOM Dependencies
                                        Component bomComponent = new Component();
                                        bomComponent.setRepoURL(populateMavenRepoURL(mavenRepo, SpringBootDependenciesGroupId.replaceAll("\\.", "/"), SpringBootDependenciesArtifactId, (String) row.get(2)));
                                        bomComponent.setPomContent(fetchContent(bomComponent.repoURL));
                                        bomComponent.setModel(populateModel(bomComponent.pomContent));
                                        component.setBomModel(bomComponent.getModel());
                                    }
                                }
                            }

                            // Check if the dependency contains the component to search and get the version
                            // Scan first the pom
                            component = getComponentVersion(component, false);
                            System.out.printf("Upstream version : %s\n", component.version);

                            // Update the cell of the Component version using as text:
                            // - The version of the component as defined within the starter,
                            // - The Url of the maven repo to access the pom file of the starter packaging the component
                            String cellPosition = configuration.getCellUpPomURL() + (i + 1);
                            String hyperlink = "=HYPERLINK(\"" + component.getRepoURL().toString() + "\",\"" + (String)row.get(2) + "\")";
                            updateCells(service, cellPosition, "USER_ENTERED", hyperlink);

                            // Update the upstream cell of the Component using as text:
                            // - The version of the component,
                            // - The Url of the maven repo to access the pom file of the component, starter
                            cellPosition = configuration.getCellUpComponentURL() + (i + 1);
                            hyperlink = "=HYPERLINK(\"" + populateMavenRepoURL(mavenRepo,component.groupId.replaceAll("\\.", "/"),component.getArtifactId(),component.version) + "\",\"" + component.version + "\")";
                            updateCells(service, cellPosition, "USER_ENTERED", hyperlink);

                            // Update the downstream cell of the Component using as text:
                            // - The version of the component,
                            // - The Url of the MRRC repo to access the pom file of the component, starter
                            cellPosition = configuration.getCellDownComponentURL() + (i + 1);
                            hyperlink = "=HYPERLINK(\"" + populateMavenRepoURL(configuration.mavenRedHatRepo,component.groupId.replaceAll("\\.", "/"),component.getArtifactId(),(String)row.get(6)) + "\",\"" + (String)row.get(6) + "\")";
                            updateCells(service, cellPosition, "USER_ENTERED", hyperlink);

                            System.out.println("========================================");
                        }
                    }
                } catch (IndexOutOfBoundsException idx) {
                    System.out.printf("No information is defined for line : %s\n", i);
                }
            }
        }
    }

    static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

        if (!clientSecretFilePath.exists()) {
            throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
                    + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }

        // Load client secrets.
        InputStream in = new FileInputStream(clientSecretFilePath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                        clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(CREDENTIALS_FOLDER))
                        .setAccessType("offline")
                        .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("me");
    }

    static void updateCells(Sheets service, String cellToUpdate, String valueInputOption, String version) throws IOException {
        List<List<Object>> writeData = new ArrayList<>();
        List<Object> dataRow = new ArrayList<>();
        dataRow.add(version);
        writeData.add(dataRow);

        final String outputRange = cellToUpdate + ":" + cellToUpdate;

        ValueRange body = new ValueRange()
                .setValues(writeData);
        UpdateValuesResponse result =
                service.spreadsheets().values()
                        .update(GSHEET_ID, outputRange, body)
                        .setValueInputOption(valueInputOption)
                        .execute();
    }

    static Model populateModel(String content) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(new StringReader(content));
    }

    static String fetchContent(URL url) {
        java.io.Reader reader = null;
        try {
            reader = new java.io.InputStreamReader((java.io.InputStream) url.getContent());
            StringBuilder content = new StringBuilder();
            char[] buf = new char[1024];
            for (int n = reader.read(buf); n > -1; n = reader.read(buf))
                content.append(buf, 0, n);
            return content.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (Throwable t) {
            }
        }
    }

    static Component getComponentVersion(Component component, boolean IsParentPom) throws IOException, XmlPullParserException {
        Model model;
        List<Dependency> dependencies;

        if (component.getBomModel() != null) {
            model = component.getBomModel();
        } else if (!IsParentPom) {
            model = component.getModel();
        } else {
            model = component.getParentModel();
        }

         if (model.getDependencies().size() > 0) {
                dependencies = model.getDependencies();
        } else {
             dependencies = model.getDependencyManagement().getDependencies();
         }

        for (Dependency dep : dependencies) {
            component = searchVersion(component, dep, IsParentPom);
            if (component.getVersion() != null) {
                return component;
            }
        }

        return component;
    }

    static Component searchVersion(Component component, Dependency dependency, boolean IsParentPom) throws IOException, XmlPullParserException {
        if (dependency.getArtifactId().contains(component.toSearch)) {
            // If there is a matching, then we can set the GroupId, ArtifactId
            // We check first if the groupId exists.
            // Otherwise, we will pick up the groupId from the parent
            if (dependency.getGroupId().startsWith("${")) {
                component.setGroupId(component.model.getParent().getGroupId());
            } else {
                component.setGroupId(dependency.getGroupId());
            }
            component.setArtifactId(dependency.getArtifactId());

            // If the version is null, then we will search the version of the component
            // using the parent pom
            if (dependency.getVersion() == null) {
                Parent parent = component.getModel().getParent();
                component.setParentRepoURL(populateMavenRepoURL(mavenRepo, parent.getGroupId().replaceAll("\\.", "/"), parent.getArtifactId(), parent.getVersion()));
                component.setParentPomContent(fetchContent(component.parentRepoURL));
                component.setParentModel(populateModel(component.parentPomContent));
                return getComponentVersion(component, true);
            } else {
                // Use the pom project version
                // if the version is equal to ${project.version}
                if (dependency.getVersion().startsWith("${project.version")) {
                    // Check if there is a version defined for the project, otherwise pickup the version of the parent
                    if (component.getModel().getVersion() == null) {
                        component.setVersion(component.getModel().getParent().getVersion());
                        return component;
                    } else {
                        component.setVersion(component.getModel().getVersion());
                        return component;
                    }
                }

                // Check within the list of the properties
                // if the version is equal to ${xxxxxx}
                // where xxxxxx is a property
                if (dependency.getVersion().startsWith("${")) {
                    Properties props = component.getModel().getProperties();
                    // If there are no properties within the pom, then we will fetch them from the BOM or the parent
                    if (props.size() <= 0) {
                        if (component.getBomModel() != null) {
                            props = component.getBomModel().getProperties();
                        } else {
                            Parent parent = component.getModel().getParent();
                            component.setParentRepoURL(populateMavenRepoURL(mavenRepo, parent.getGroupId().replaceAll("\\.", "/"), parent.getArtifactId(), parent.getVersion()));
                            component.setParentPomContent(fetchContent(component.parentRepoURL));
                            component.setParentModel(populateModel(component.parentPomContent));
                            props = component.getParentModel().getProperties();
                        }
                    }
                    Set<Map.Entry<Object, Object>> entries = props.entrySet();
                    for (Map.Entry<Object, Object> entry : entries) {
                        String key = (String) entry.getKey();
                        if (key.contains(component.toSearch)) {
                            String val = (String) entry.getValue();
                            // If the key is not a version such as a string, message, then we continue
                            if (val.contains(" ")) {
                                continue;
                            }
                            component.setVersion(val);
                            return component;
                        }
                    }
                }
            }

            // Then the version is semantically equivalent to
            // ...
            component.setVersion(dependency.getVersion());
            return component;
        }
        return component;
    }

    static URL populateMavenRepoURL(String mavenRepo, String groupID, String artifactID, String version) throws IOException, XmlPullParserException {
        String REPO = mavenRepo;
        URL url = new URL(REPO
                + groupID
                + "/"
                + artifactID
                + "/"
                + version
                + "/"
                + artifactID + "-" + version + ".pom");
        return url;
    }
}