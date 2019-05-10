package com.mintel.gcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;

import com.google.api.client.util.ByteStreams;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.common.net.MediaType;

public class GCSContentStreamListener implements ContentStreamListener
{
    private Bucket bucket;
    private GCSContentWriter writer;
    
    public GCSContentStreamListener(GCSContentWriter writer)
    {
        this.writer = writer;
        this.bucket = writer.getBucket();
    }

    @Override
    public void contentStreamClosed() throws ContentIOException
    {
        File file = writer.getTempFile();

        long size = file.length();

        writer.setSize(size);

        BlobId blobId = BlobId.of(bucket.getName(), writer.getPath());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(MediaType.OCTET_STREAM.toString()).build();
        
        OutputStream writer = null;
        InputStream reader = null;
        try
        {
            writer = Channels.newOutputStream(bucket.getStorage().writer(blobInfo));
            reader = new FileInputStream(file);
            
            ByteStreams.copy(reader, writer);
        }
        catch (Exception e)
        {
            throw new ContentIOException("could not write", e);
        }
        finally
        {
            try 
            {
                if (writer != null)
                {
                    writer.close();
                }
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
                throw new ContentIOException("could not close stream", e);
            }
        }
    }

}
