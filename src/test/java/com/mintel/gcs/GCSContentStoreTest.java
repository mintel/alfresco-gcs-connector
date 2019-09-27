package com.mintel.gcs;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class GCSContentStoreTest
{
    public static final String BUCKET_PATH = "gs://parallel/";
    public static final String BUCKET_NAME = "***REMOVED***";
    public static final String KEY = "***REMOVED***";

    @Test public void test() throws Exception
    {
        /**
         * Init
         */
        GCSContentStore store = new GCSContentStore(KEY, BUCKET_NAME, "");

        int numberOfFiles = 21;

        Set<String> files = new HashSet<String>();
        for (int i = 0; i < numberOfFiles; i++)
        {
            files.add(store.getPath(GCSContentStore.createNewUrl()));
        }


        /**
         * Write
         */
        System.out.println("Starting write....");
        long startTime = System.currentTimeMillis();
        files.parallelStream().forEach((filename) -> {
            ContentReader reader = store.getReader(BUCKET_PATH + filename);
            ContentWriter writer = store.getWriterInternal(reader, BUCKET_PATH + filename);
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
        System.out.println("Written " + numberOfFiles + " documents in " + timeElapsed + " milliseconds : ");
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms\n");


        /**
         * Read
         */
        System.out.println("Starting read....");
        startTime = System.currentTimeMillis();
        Stream<String> results = files.parallelStream().map((filename) -> {
            ContentReader reader = store.getReader(BUCKET_PATH + filename);
            assertTrue(reader.exists());
            String content = reader.getContentString();
            System.out.println("Content: " + content);
            assertNotNull(content);
            assertEquals(filename, content);
            return content;
        });
        assertEquals(numberOfFiles, results.count());
        timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Read " + numberOfFiles + " documents in " + timeElapsed + " milliseconds : ");
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms\n");


        /**
         * Write
         */
        System.out.println("Starting delete");
        startTime = System.currentTimeMillis();
        files.parallelStream().forEach((filename) -> {
            store.delete(BUCKET_PATH + filename);
            ContentReader reader = store.getReader(BUCKET_PATH + filename);
        });
        timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Deleted " + numberOfFiles + " documents in " + timeElapsed + " milliseconds : ");
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms\n");
    }

}