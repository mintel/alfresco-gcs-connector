package com.mintel.gcs;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the general connection to Google Cloud Storage without relying on Alfresco
 * <p>
 * @author Ana Gouveia
 * @author Matteo Mazzola
 * @author Sam Cheshire
 * @author Rob Mackay
 */
public class GCSContentStoreTest
{
    public static final String BUCKET_PATH = "gs://parallel/";
    public static String BUCKET_NAME;
    public static final String KEY = "key.json";
    public static final String KEY_PATH = "alfresco/extension/google-cloud-storage/";

    @Before
    public void setup()
    {
        BUCKET_NAME = System.getenv("BUCKET_NAME");
        if (BUCKET_NAME == null)
        {
            throw new NullPointerException("Bucketname can not be null.");
        }
    }

    /**
     * General test that doesn't require a running Alfresco.
     * Uses the GCSContentstore directly.
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception
    {
        /**
         * Init
         */
        GCSContentStore store = new GCSContentStore(KEY_PATH, KEY, BUCKET_NAME, "");

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
        AtomicInteger numberOfFilesWritten = new AtomicInteger();
        files.parallelStream().forEach((filename) ->
        {
            ContentReader reader = store.getReader(BUCKET_PATH + filename);
            ContentWriter writer = store.getWriterInternal(reader, BUCKET_PATH + filename);
            WritableByteChannel channel = writer.getWritableChannel();
            try
            {
                channel.write(ByteBuffer.wrap(filename.getBytes()));
                channel.close();
                numberOfFilesWritten.addAndGet(1);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
        long timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Written " + numberOfFiles + " documents in " + timeElapsed + " milliseconds : ");
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms\n");
        assertEquals(numberOfFiles, numberOfFilesWritten.get());

        /**
         * Read
         */
        System.out.println("Starting read....");
        startTime = System.currentTimeMillis();
        AtomicInteger numberOfFilesDeleted = new AtomicInteger();
        Stream<String> results = files.parallelStream().map((filename) ->
        {
            ContentReader reader = store.getReader(BUCKET_PATH + filename);
            assertTrue(reader.exists());
            String content = reader.getContentString();
            System.out.println("Content: " + content);
            assertNotNull(content);
            assertEquals(filename, content);
            numberOfFilesDeleted.addAndGet(1);
            return content;
        });
        assertEquals(numberOfFiles, results.count());
        timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Read " + numberOfFiles + " documents in " + timeElapsed + " milliseconds : ");
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms\n");
        assertEquals(numberOfFiles, numberOfFilesDeleted.get());

        /**
         * Delete
         */
        System.out.println("Starting delete");
        startTime = System.currentTimeMillis();
        files.parallelStream().forEach((filename) ->
        {
            store.delete(BUCKET_PATH + filename);
            ContentReader reader = store.getReader(BUCKET_PATH + filename);
        });
        timeElapsed = System.currentTimeMillis() - startTime;
        System.out.println("Deleted " + numberOfFiles + " documents in " + timeElapsed + " milliseconds : ");
        System.out.println("\tTime elapsed per file: " + (timeElapsed / files.size()) + "ms\n");
    }

    /**
     * Checks if we can handle different legacy contenturls.
     *
     * @throws Exception
     */
    @Test
    public void contentUrlTest() throws Exception
    {
        GCSContentStore store = new GCSContentStore(KEY_PATH, KEY, BUCKET_NAME, "contentstore");

        assertEquals("contentstore/2013/12/17/16/57/5f3ee607-0d69-409b-9bdd-320c04a72706.bin", store.getPath("store://2013/12/17/16/57/5f3ee607-0d69-409b-9bdd-320c04a72706.bin"));

        assertEquals("contentstore/-system-/2018/10/5/1/21/ff9fefb3-6665-4c04-9b21-7641a9d1258e.bin", store.getPath("s3://-system-/2018/10/5/1/21/ff9fefb3-6665-4c04-9b21-7641a9d1258e.bin"));
    }

}