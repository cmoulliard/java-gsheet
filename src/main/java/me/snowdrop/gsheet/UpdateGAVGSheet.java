package me.snowdrop.gsheet;

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
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateGAVGSheet {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CLIENT_SECRET_FILE_NAME = "client_secrets.json";

    // Directory to store user credentials for this application.
    private static final java.io.File CREDENTIALS_FOLDER //
            = new java.io.File(System.getProperty("user.home"), "credentials");
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    private static String GSHEET_ID = "1YcNuI_lzruhhS4P1mIGnklSnLqfVK6SWQu1BRTP8jY4";
    private static String INPUT_RANGE = "A1:A10";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

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

    public static void main(String... args) throws IOException, GeneralSecurityException, XmlPullParserException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Create a Service to read, update cells
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                //.setApplicationName(APPLICATION_NAME)
                .build();


        // Read Cells
        readCells(service);

        // Fetch POM content and print GAV
        parseMavenPOM("org/springframework/boot","spring-boot","2.1.0.RELEASE");

        // Update Cells
        updateCells(service);
    }

    static void readCells(Sheets service) throws IOException {
        // Read Cells
        ValueRange response = service.spreadsheets().values()
                .get(GSHEET_ID, INPUT_RANGE)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Name, Major");
            for (List row : values) {
                // Print cell of the column A.
                System.out.printf("%s\n", row.get(0));
            }
        }
    }

    static void updateCells(Sheets service) throws IOException {
        List<List<Object>> writeData = new ArrayList<>();
        List<Object> dataRow = new ArrayList<>();
        dataRow.add("B1");
        writeData.add(dataRow);
        writeData.add(dataRow);
        writeData.add(dataRow);

        final String outputRange = "B:B";

        ValueRange body = new ValueRange()
                .setValues(writeData);
        UpdateValuesResponse result =
                service.spreadsheets().values()
                        .update(GSHEET_ID, outputRange, body)
                        .setValueInputOption("RAW")
                        .execute();
        System.out.printf("Cells updated.");
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

    static void parseMavenPOM(String groupID, String artifactID, String version) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        String REPO = "http://repo1.maven.org/maven2/";
        URL url = new URL( REPO
                 + groupID
                 + "/"
                 + artifactID
                 + "/"
                 + version
                 + "/"
                 + artifactID + "-" + version + ".pom");

        String content = fetchContent(url);
        Model model = reader.read(new StringReader(content));
        System.out.println(model.getId());
        System.out.println(model.getGroupId());
        System.out.println(model.getArtifactId());
        System.out.println(model.getVersion());

        List<Dependency> dependencies = model.getDependencies();
        for(Dependency dep: dependencies) {
            if (dep.getGroupId().contains("tomcat")) {
                System.out.println("====================");
                System.out.println(dep.getGroupId());
                System.out.println(dep.getArtifactId());
                System.out.println(dep.getVersion());
                System.out.println("====================");
            }
        }
    }
}