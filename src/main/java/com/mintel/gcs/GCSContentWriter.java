package com.mintel.gcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;

import com.google.cloud.storage.Bucket;

public class GCSContentWriter extends AbstractContentWriter
{
    private String path;
    private String contentUrl;
    private Bucket bucket;
    private String uuid;
    private long size;
    private File tempFile;

    protected GCSContentWriter(String path, String contentUrl, ContentReader existingContentReader, Bucket bucket)
    {
        super(contentUrl, existingContentReader);
        this.path = path;
        this.contentUrl = contentUrl;
        this.bucket = bucket;
        this.uuid = GUID.generate();
        addListener(new GCSContentStreamListener(this));
    }

    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new GCSContentReader(path, contentUrl, bucket);
    }

    @Override
    protected WritableByteChannel getDirectWritableChannel() throws ContentIOException
    {
        try
        {
            // create temporary file to store data before writing to storage
            tempFile = TempFileProvider.createTempFile(uuid, ".bin");
            OutputStream os = new FileOutputStream(tempFile);
            return Channels.newChannel(os);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("could not open write channel, sorry", e);
        }

    }
    
    /*
     * Getters and setters
     */
    

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public Bucket getBucket()
    {
        return bucket;
    }

    public File getTempFile()
    {
        return tempFile;
    }

    @Override
    public long getSize()
    {
        return size;
    }
    
    public void setSize(long size)
    {
        this.size = size;
    }

}
