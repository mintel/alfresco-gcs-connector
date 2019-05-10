package com.mintel.gcs;

import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        InputStream is = Main.class.getClassLoader().getResourceAsStream("key.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(is);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId("***REMOVED***").build().getService();
        System.out.print(storage.get("***REMOVED***"));
    }

}
