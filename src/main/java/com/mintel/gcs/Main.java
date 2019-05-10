package com.mintel.gcs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        /*
         * Init
         */
        InputStream is = Main.class.getClassLoader().getResourceAsStream("key.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(is);
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId("***REMOVED***").build().getService();
        Bucket bucket = storage.get("***REMOVED***");
        GCSContentStore store = new GCSContentStore("key.json", "***REMOVED***", "");
        System.out.println(bucket);

        /*
         * WRITING
         */

        int numberOfFiles = 10000;

        Set<String> files = new HashSet<String>();
        for (int i = 0; i < numberOfFiles; i++)
        {
            files.add(store.getPath(GCSContentStore.createNewUrl()));
        }
        System.out.println("Starting write....");
        long startTime = System.currentTimeMillis();

        files.parallelStream().forEach((filename) ->
        {
            ContentReader reader = store.getReader("gs://parallel/" + filename);
            ContentWriter writer = store.getWriterInternal(reader, "gs://parallel/" + filename);
            WritableByteChannel channel = writer.getWritableChannel();
            try
            {
                channel.write(ByteBuffer.wrap(filename.getBytes()));
                channel.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        long timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Execution time in milliseconds  : " + timeElapsed);
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms");

        startTime = System.currentTimeMillis();

        files.parallelStream().forEach((filename) ->
        {
            ContentReader reader = store.getReader("gs://parallel/" + filename);
            /*System.out.println("Exists: " + reader.exists());
            System.out.println("Last modified: " + reader.getLastModified());
            System.out.println("Size: " + reader.getSize());*/
            System.out.println("Content: " + reader.getContentString());
        });

        timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Execution time in milliseconds  : " + timeElapsed);
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms");

        /* ContentReader reader = store.getReader("gs://hi/sam.txt");
        ContentWriter writer = store.getWriterInternal(reader, "gs://hi/sam.txt");
        WritableByteChannel channel = writer.getWritableChannel();
        
        String emoji = "☊☋☌☍☎☏☐☑☒☓☔☕☖☗☘☙☚☛☜☝☞☟☠☡☢☣☤☥☦☧☨☩☪☫☬☭☮☯☰☱☲☳☴☵☶☷☸☹☺☻☼☽☾☿♀♁♂♃♄♅♆♇♈♉♊♋♌♍♎♏♐♑♒♓♔♕♖♗♘♙♚♛♜♝♞♟♠♡♢♣♤♥♦♧♨♩♪♫♬♭♮♯≰≱≲≳≴≵≶≷≸≹≺≻≼≽≾≿⊀⊁⊂⊃⊄⊅⊆⊇⊈⊉⊊⊋⊌⊍⊎⊏⊐⊑⊒⊓⊔⊕⊖⊗⊘⊙⊚⊛⊜⊝⊞⊟⊠⊡⊢⊣⊤⊥⊦⊧⊨⊩⊪⊫⊬⊭⊮⊯⊰⊱⊲⊳⊴⊵⊶⊷⊸⊹";
        
        channel.write(ByteBuffer.wrap(emoji.getBytes()));
        channel.close();
        
        
         * READING
         
        System.out.println("Exists: " + reader.exists());
        System.out.println("Last modified: " + reader.getLastModified());
        System.out.println("Size: " + reader.getSize());
        System.out.println("Content: " + reader.getContentString());*/
    }

}
