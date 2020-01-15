## How to use the Java GSheet application

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

## Use the Google GSheet Application to update the data

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