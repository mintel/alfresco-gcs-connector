package com.mintel.gcs;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;

public class GCSContentStoreTest {
    @Test
    public void test() throws Exception {
        /*
         * Init
         */
        GCSContentStore store = new GCSContentStore("***REMOVED***", "***REMOVED***", "");

        /*
         * WRITING
         */

        int numberOfFiles = 100;

        Set<String> files = new HashSet<String>();
        for (int i = 0; i < numberOfFiles; i++)
        {
            files.add(store.getPath(GCSContentStore.createNewUrl()));
        }
        System.out.println("Starting write....");
        long startTime = System.currentTimeMillis();

        files.parallelStream().forEach((filename) ->
        {
            ContentReader reader = store.getReader("gs://parallel3/" + filename);
            ContentWriter writer = store.getWriterInternal(reader, "gs://parallel3/" + filename);
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
            ContentReader reader = store.getReader("gs://parallel3/" + filename);
            /*System.out.println("Exists: " + reader.exists());
            System.out.println("Last modified: " + reader.getLastModified());
            System.out.println("Size: " + reader.getSize());*/
            System.out.println("Content: " + reader.getContentString());
        });
        timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Execution time in milliseconds  : " + timeElapsed);
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms");



        System.out.println("Starting delete");

        startTime = System.currentTimeMillis();
        files.parallelStream().forEach((filename) ->
        {
            store.delete("gs://parallel3/" + filename);
            ContentReader reader = store.getReader("gs://parallel3/" + filename);
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
