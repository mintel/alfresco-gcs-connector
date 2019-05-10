package com.mintel.gcs;

import java.nio.channels.ReadableByteChannel;

import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;

public class GCSContentReader extends AbstractContentReader
{

    protected GCSContentReader(String contentUrl)
    {
        super(contentUrl);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean exists()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getLastModified()
    {
        // TODO Auto-generated method stub
        return 0;
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
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
