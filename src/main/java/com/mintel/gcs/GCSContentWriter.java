package com.mintel.gcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;

import com.google.cloud.storage.Bucket;

/**
 * Google cloud storage contend writer implementation.
 * 
 * @author Ana Gouveia
 * @author Matteo Mazzola
 * @author Sam Cheshire
 * @author Rob Mackay
 */
public class GCSContentWriter extends AbstractContentWriter
{
    /**
     * The path of the content within the store/bucket. By using this if data is migrated to a new bucket it will still work without changing the database values.
     */
    private String path;
    /**
     * The full content URL of the node
     */
    private String contentUrl;
    /**
     * The bucket where the content should be
     */
    private Bucket bucket;
    /**
     * Temporary uuid for caching the content before writing it to storage 
     */
    private String uuid;
    /**
     * The content size
     */
    private long size;
    /**
     * The temporary file written locally before writing it to storage 
     */
    private File tempFile;

    /**
     * Initialises a GCS content writer
     * 
     * @param path The simple path to the content inside the contentstore
     * @param contentUrl The full content URL
     * @param existingContentReader A reader of a previous version of this content
     * @param bucket The bucket where the content should be
     */
    protected GCSContentWriter(String path, String contentUrl, ContentReader existingContentReader, Bucket bucket)
    {
        super(contentUrl, existingContentReader);
        this.path = path;
        this.contentUrl = contentUrl;
        this.bucket = bucket;
        this.uuid = GUID.generate();
        addListener(new GCSContentStreamListener(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        if(this.getTempFile() != null)
        {
            FileContentReader reader = new FileContentReader(this.getTempFile(), this.getContentUrl());
            reader.setMimetype(this.getMimetype());
            reader.setEncoding(this.getEncoding());
            return reader;
        }
        return new GCSContentReader(path, contentUrl, bucket);
    }

    /**
     * {@inheritDoc}
     */
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
            throw new ContentIOException("Could not open write channel, sorry", e);
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
