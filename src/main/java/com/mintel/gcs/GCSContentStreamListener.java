package com.mintel.gcs;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentStreamListener;

public class GCSContentStreamListener implements ContentStreamListener
{
    
    public GCSContentStreamListener(GCSContentWriter writer)
    {
        //TODO
    }

    @Override
    public void contentStreamClosed() throws ContentIOException
    {
        // TODO Auto-generated method stub

    }

}
