package com.mintel.gcs;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobGetOption;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        InputStream is = Main.class.getClassLoader().getResourceAsStream("key.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(is);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId("***REMOVED***").build().getService();
        System.out.println(storage.get("***REMOVED***"));

        Bucket bucket = storage.get("***REMOVED***");
        System.out.println(bucket);
        BlobGetOption BLOB_FIELDS = BlobGetOption.fields(
            Storage.BlobField.UPDATED,
            Storage.BlobField.SIZE
        );
        for (Blob b : bucket.list().getValues())
        {
            System.out.println(b.getName());
        }
        Blob metadata = bucket.get("hi/sam.txt", BLOB_FIELDS);
        System.out.println(metadata);
        System.out.println(metadata.exists());
        System.out.println(metadata.getUpdateTime());
        System.out.println(metadata.getSize());
        is = Channels.newInputStream(metadata.reader());
        int a;
        while (true)
        {
            a = is.read();
            if (a != -1)
            {
                System.out.print((char) a);
            }
            else
            {
                break;
            }
        }
        is.close();
    }

}
