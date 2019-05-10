package main.java.com.mintel.gcs;

import java.nio.channels.WritableByteChannel;

import org.alfresco.repo.content.AbstractContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;

public class GCSContentWriter extends AbstractContentWriter
{

    protected GCSContentWriter(String contentUrl, ContentReader existingContentReader)
    {
        super(contentUrl, existingContentReader);
        // TODO Auto-generated constructor stub
    }

    @Override
    public long getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected WritableByteChannel getDirectWritableChannel() throws ContentIOException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
