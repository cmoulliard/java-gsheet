package me.snowdrop.google;

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
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GDocApplication {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Directory to store user credentials for this application.
    private static final File CREDENTIALS_FOLDER //
            = new File(System.getProperty("user.home"), "credentials");
    private static final String CLIENT_SECRET_FILE_NAME = "client_secrets.json";

    // Scope to access the Google documents
    private static final List<String> SCOPES = Collections.singletonList(DocsScopes.DOCUMENTS);

    // Google Sheet ID
    private static String GDOC_ID;

    public static void main(String... args) throws IOException, GeneralSecurityException, XmlPullParserException {

        if (args.length == 0) {
            System.out.println("Proper Usage is: java -jar ./maven-gsheet-1.0-SNAPSHOT-jar-with-dependencies.jar doc_id");
            System.exit(0);
        } else {
            GDOC_ID = args[0];
        }

        // Create the Google Doc service able to communicate with the Sheet
        Docs service = createService();

        Document response = service
                .documents()
                .get(GDOC_ID)
                .execute();
        String title = response.getTitle();

        System.out.printf("The title of the doc is: %s\n", title);
    }

    static Docs createService() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Create a Service to read, update cells
        Docs service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                //.setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        File clientSecretFilePath = new File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

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

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
}