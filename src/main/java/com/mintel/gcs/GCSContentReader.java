package com.mintel.gcs;

import java.nio.channels.ReadableByteChannel;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobGetOption;

/**
 * Google cloud storage contend reader implementation.
 * 
 * @author Ana Gouveia
 * @author Matteo Mazzola
 * @author Sam Cheshire
 * @author Rob Mackay
 */
public class GCSContentReader extends AbstractContentReader
{
    private static final Log LOG = LogFactory.getLog(GCSContentStore.class);

    private static final BlobGetOption METADATA_FIELDS = BlobGetOption.fields(Storage.BlobField.UPDATED, Storage.BlobField.SIZE);
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
     * A google cloud object with only the metadata we need
     */
    private Blob metadata = null;

    /**
     * Initialises a GCS content reader
     * 
     * @param path The simple path to the content inside the contentstore
     * @param contentUrl The full content URL
     * @param bucket The bucket where the content is stored
     */
    protected GCSContentReader(String path, String contentUrl, Bucket bucket)
    {
        super(contentUrl);
        this.path = path;
        this.contentUrl = contentUrl;
        this.bucket = bucket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists()
    {
        Blob metadata = getMetadata();
        return metadata != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLastModified()
    {
        Blob metadata = getMetadata();
        return metadata == null ? 0L : metadata.getUpdateTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize()
    {
        Blob metadata = getMetadata();
        return metadata == null ? 0L : metadata.getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new GCSContentReader(path, contentUrl, bucket);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        if (!exists())
        {
            throw new ContentIOException("File doesn't exist");
        }
        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Reading object using path: " + path);
            }
            return bucket.get(path).reader();
        }
        catch (Exception e)
        {
            throw new ContentIOException("Could not read", e);
        }
    }

    /**
     * Gets a blob with just the required metadata (size and last modified date)
     *
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
