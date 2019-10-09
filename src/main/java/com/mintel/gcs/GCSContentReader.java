package com.mintel.gcs;

import java.nio.channels.ReadableByteChannel;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobGetOption;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GCSContentReader extends AbstractContentReader
{
    private static final Log LOG = LogFactory.getLog(GCSContentStore.class);

    private static BlobGetOption METADATA_FIELDS = BlobGetOption.fields(
        Storage.BlobField.UPDATED,
        Storage.BlobField.SIZE
    );
    
    private String path;
    private String contentUrl;
    private Bucket bucket;
    
    private Blob metadata = null;

    protected GCSContentReader(String path, String contentUrl, Bucket bucket)
    {
        super(contentUrl);
        this.path = path;
        this.contentUrl = contentUrl;
        this.bucket = bucket;
    }

    @Override
    public boolean exists()
    {
        Blob metadata = getMetadata();
        return metadata != null && metadata.exists();
    }

    @Override
    public long getLastModified()
    {
        Blob metadata = getMetadata();
        return metadata == null ? 0L : metadata.getUpdateTime();
    }

    @Override
    public long getSize()
    {
        Blob metadata = getMetadata();
        return metadata == null ? 0L : metadata.getSize();
    }

    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new GCSContentReader(path, contentUrl, bucket);
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        if (!exists())
        {
            throw new ContentIOException("file doesn't exist");
        }
        try
        {
            if(LOG.isDebugEnabled())
                LOG.debug("Reading object using path: " + path);
            return bucket.get(path).reader();
        }
        catch (Exception e)
        {
            throw new ContentIOException("could not read", e);
        }
    }
    
    /**
     * gets blob with just required metadata (size and last modified date)
     * @return Blob containing only these fields
     */
    private Blob getMetadata()
    {
        if (metadata == null)
        {
            metadata = bucket.get(path, METADATA_FIELDS);
        }
        return metadata;
    }

}
