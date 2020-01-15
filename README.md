## How to use the Java GSheet application

- Create a Google Oauth2 `client_id` and `client_secret` using the Google Developer console : `https://console.developers.google.com/apis/credentials`
- Select from the `Create credentials` list, the entry `Oauth Client ID`
- Specify as `Application type`, `Others`
- Name it : `snowdrop`
- Save the `client_id` and `client_secret` within the file `~/credentials/client_secrets.json`. **REMARK**: You can also download the json file from the google console and rename it
  to `~/credentials/client_secrets.json`!
```json
{
  "installed": {
    "client_id": "ADD_ME_HERE",
    "client_secret": "ADD_ME_HERE"
  }
}
```

## Use the Java Application

- Build the java application using the command `mvn package`
- Launch it locally
```bash
java -jar ./target/maven-gsheet-1.0-SNAPSHOT-jar-with-dependencies.jar
```    

## Useful links

- [GoogleLibs](https://developers.google.com/api-client-library/java/google-api-java-client/setup) for Java
- How to setup [Oauth2](https://developers.google.com/api-client-library/java/google-api-java-client/oauth2)