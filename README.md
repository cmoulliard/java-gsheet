# Java GSheet application

This java application allows to update a Google Sheet document using an `Oauth Client Id` and will perform the following steps:
- Read a range of cells containing the GAV definition of the POM to be used and the component name to search for
- Grab the maven url and search about the version of the component using the dependencies of the pom or parent and/or properties
- Save the version of the component like also the URL of the POM file containing it

Example 

Before to run the java application

![Before](gsheet_before.png)

After the execution of the application

![After](gsheet_after.png)


## Setup the project

- Create a Google Oauth2 `client_id` and `client_secret` using the Google Developer console : `https://console.developers.google.com/apis/credentials`
- Select from the `Create credentials` list, the entry `Oauth Client ID`
- Specify as `Application type`, `Others`
- Name it : `snowdrop`
- Save the `client_id` and `client_secret` within the file `~/credentials/client_secrets.json`.
  ```json
  {
    "installed": {
      "client_id": "ADD_ME_HERE",
      "client_secret": "ADD_ME_HERE"
    }
  }
  ```
  
  **REMARK** : You can also download the json file from the google console and rename it
  to `~/credentials/client_secrets.json`!

  **IMPORTANT** : If you get an `401` unauthorized error, this is most probably due to the fact that the Library 
  of `Gsheets` or `GDocs` has not been enable from the Google developer console. They can be enabled/disabled from this url by example : https://console.developers.google.com/apis/library/docs.googleapis.com   

## Build and launch the application

- Build the java application using the command `mvn clean package`
- Launch it locally
  ```bash
  java -jar ./target/maven-gsheet-1.0-SNAPSHOT-jar-with-dependencies.jar SHEET_ID
  
  example
  java -jar ./target/maven-gsheet-1.0-SNAPSHOT-jar-with-dependencies.jar 1YcNuI_lzruhhS4P1mIGnklSnLqfVK6SWQu1BRTP8jY4
  ```  
**NOTE** : The first time you will launch it, then you wil be redirected to the Oauth URL from where you can grant access
to your Google account.
Select your account and next click on the `allow` button. A new file will be created under the `./credentials` folder called
`StoredCredential`.

 
## Useful links

- [GoogleLibs](https://developers.google.com/api-client-library/java/google-api-java-client/setup) for Java
- How to setup [Oauth2](https://developers.google.com/api-client-library/java/google-api-java-client/oauth2)