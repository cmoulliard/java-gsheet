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

    public static void main(String... args) throws IOException, GeneralSecurityException, XmlPullParserException {

        if (args.length == 0) {
            System.out.println("Proper Usage is: java -jar ./maven-gsheet-1.0-SNAPSHOT-jar-with-dependencies.jar sheet_id");
            System.exit(0);
        } else {
            GSHEET_ID = args[0];
        }

        // Collect information such as maven repositories from the json configuration file
        init();

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
        configuration = mapper.readValue(configURL,Configuration.class);
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
                    String componentToSearch = (String) row.get(3);
                    if (!componentToSearch.contentEquals("??")) {
                        System.out.printf("Component : %s\n", componentToSearch);

                        // Fetch POM file using GAV defined within the G Sheet
                        // and maven repository upstream
                        Component component = parseMavenPOM(configuration.mavenCentralRepo, (String) row.get(0), (String) row.get(1), (String) row.get(2));
                        System.out.printf("Maven URL : %s\n", component.repoURL);

                        // Check if the dependency contains the component to search and get version
                        String componentVersion = getComponentVersion(component.model, componentToSearch);
                        System.out.printf("Version : %s\n", componentVersion);

                        // Update the cell of the Component URL (upstream)
                        String cellURLPosition = configuration.getCellUpstreamURL() + (i + 1);
                        String hyperlink = "=HYPERLINK(\"" + component.getRepoURL().toString() + "\",\"" + componentVersion + "\")";
                        updateCells(service, cellURLPosition, "USER_ENTERED", hyperlink);

                        // Do the job for Downstream Repository
                        //component = parseMavenPOM(configuration.mavenRedHatRepo, (String) row.get(0), (String) row.get(1), (String) row.get());
                        //System.out.printf("Maven URL : %s\n", component.repoURL);

                        System.out.println("========================================");
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

    static String getComponentVersion(Model model, String componentToSearch) throws IOException, XmlPullParserException {
        List<Dependency> dependencies = model.getDependencies();
        String result;

        // Test if we have dependencies, otherwise use the dependencies delcared within the dependencyManagement section
        if (dependencies.size() > 0) {
            for (Dependency dep : dependencies) {
                result = searchVersion(model, dep, componentToSearch);
                if (result != "") {
                    return result;
                }
            }
        } else {
            for (Dependency dep : model.getDependencyManagement().getDependencies()) {
                result = searchVersion(model, dep, componentToSearch);
                if (result != "") {
                    return result;
                }
            }
        }
        return "";
    }

    static String searchVersion(Model model, Dependency dependency, String componentToSearch) throws IOException, XmlPullParserException {
        if (dependency.getArtifactId().contains(componentToSearch)) {
            // If the version is null, then we will search the version of the component
            // using the dependencies defined within the parent pom
            if (dependency.getVersion() == null) {
                Parent parent = model.getParent();
                Component component = parseMavenPOM(configuration.mavenCentralRepo, parent.getGroupId().replaceAll("\\.", "/"), parent.getArtifactId(), parent.getVersion());
                return getComponentVersion(component.model, componentToSearch);
            }

            if (dependency.getVersion().startsWith("${project.version")) {
                // Check if there is a version defined for the project, otherwise pickup the version of the parent
                if (model.getVersion() == null) {
                    return model.getParent().getVersion();
                } else {
                    return model.getVersion();
                }
            }

            // We will check if we have a version or ${}"
            if (dependency.getVersion().startsWith("${")) {
                Properties props = model.getProperties();
                Set<Map.Entry<Object, Object>> entries = props.entrySet();
                for (Map.Entry<Object, Object> entry : entries) {
                    String key = (String) entry.getKey();
                    if (key.contains(componentToSearch)) {
                        String val = (String) entry.getValue();
                        // If the key is not a version such as a string, message, then we continue
                        if (val.contains(" ")) {
                            continue;
                        }
                        return val;
                    }
                }
                // If there are no properties, then we will check if the parent contains it
                Parent parent = model.getParent();
                Component component = parseMavenPOM(configuration.getMavenCentralRepo(), parent.getGroupId().replaceAll("\\.", "/"), parent.getArtifactId(), parent.getVersion());
                // TODO : To be improved
                props = component.getModel().getProperties();
                entries = props.entrySet();
                for (Map.Entry<Object, Object> entry : entries) {
                    String key = (String) entry.getKey();
                    if (key.contains(componentToSearch)) {
                        String val = (String) entry.getValue();
                        // If the key is not a version such as a string, message, then we continue
                        if (val.contains(" ")) {
                            continue;
                        }
                        return val;
                    }
                }

            }
            return dependency.getVersion();
        }
        return "";
    }

    static Component parseMavenPOM(String mavenRepo, String groupID, String artifactID, String version) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        String REPO = mavenRepo;
        URL url = new URL(REPO
                + groupID
                + "/"
                + artifactID
                + "/"
                + version
                + "/"
                + artifactID + "-" + version + ".pom");

        String content = fetchContent(url);
        Component component = new Component();
        component.setModel(reader.read(new StringReader(content)));
        component.setRepoURL(url);
        return component;
    }
}