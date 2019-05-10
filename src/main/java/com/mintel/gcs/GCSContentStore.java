package com.mintel.gcs;

import org.alfresco.repo.content.AbstractContentStore;
import org.alfresco.service.cmr.repository.ContentReader;

public class GCSContentStore extends AbstractContentStore
{

    @Override
    public boolean isWriteSupported()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ContentReader getReader(String contentUrl)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
