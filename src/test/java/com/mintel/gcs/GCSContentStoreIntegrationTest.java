package com.mintel.gcs;

import org.alfresco.model.ContentModel;
import org.alfresco.rad.test.AbstractAlfrescoIT;
import org.alfresco.rad.test.AlfrescoTestRunner;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


@RunWith(value = AlfrescoTestRunner.class)
public class GCSContentStoreIntegrationTest extends AbstractAlfrescoIT {

    private GCSContentStore store;

    @Before
    public void setupContentStore(){
        this.store = (GCSContentStore) getApplicationContext().getBean("gcsContentStore");
    }

    @Test
    public void readWriteTest(){
        NodeRef parentRef = this.getCompanyHomeNodeRef();
        FileInfo node = this.getServiceRegistry().getFileFolderService().create(
                parentRef,
                "readWriteTest.txt"+System.currentTimeMillis(),
                ContentModel.TYPE_CONTENT
        );
        NodeRef nodeRef = node.getNodeRef();
        String textContent = "node content";
        addFileContent(node.getNodeRef(), textContent);

        assertEquals(textContent,readTextContent(node.getNodeRef()));

        if (nodeRef != null) {
            getServiceRegistry().getNodeService().deleteNode(nodeRef);
        }
    }

    @Test
    public void deleteNodeTest() {
        NodeRef nodeRef = this.createTestNode("testDeleteNode");
        ContentData contentData = (ContentData) this.getServiceRegistry().getNodeService().getProperty(nodeRef, ContentModel.PROP_CONTENT);
        String contentUrl = contentData.getContentUrl();

        assertTrue(store.getReader(contentUrl).exists());

        store.delete(contentUrl);

        assertFalse(store.getReader(contentUrl).exists());
    }

    @Test
    public void existsTest() {
        assertFalse("Made up contenturl shouldn't exist", store.getReader("store://2099/9/17/3/58/182ad6bb-ec25-4012-8bcf-f370b2452d3e.bin").exists());
    }

    private NodeRef createTestNode(String content){
        NodeRef parentRef = this.getCompanyHomeNodeRef();
        FileInfo node = this.getServiceRegistry().getFileFolderService().create(
                parentRef,
                "readWriteTest.txt"+System.currentTimeMillis(),
                ContentModel.TYPE_CONTENT
        );
        String textContent = "node content";
        addFileContent(node.getNodeRef(), textContent);
        return node.getNodeRef();
    }

    /**
     * Add some text content to a file node
     *
     * @param nodeRef the node reference for the file that should have some text content added to it
     * @param fileContent the text content
     */
    private void addFileContent(NodeRef nodeRef, String fileContent) {
        boolean updateContentPropertyAutomatically = true;
        ContentWriter writer = getServiceRegistry().getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT,
                updateContentPropertyAutomatically);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(fileContent);
    }

    /**
     * Create a new node, such as a file or a folder, with passed in type and properties
     *
     * @param name the name of the file or folder
     * @param type the content model type
     * @param properties the properties from the content model
     * @return the Node Reference for the newly created node
     */
    private NodeRef createNode(String name, QName type, Map<QName, Serializable> properties) {
        NodeRef parentFolderNodeRef = getCompanyHomeNodeRef();
        QName associationType = ContentModel.ASSOC_CONTAINS;
        QName associationQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(name));
        properties.put(ContentModel.PROP_NAME, name);
        ChildAssociationRef parentChildAssocRef = getServiceRegistry().getNodeService().createNode(
                parentFolderNodeRef, associationType, associationQName, type, properties);

        return parentChildAssocRef.getChildRef();
    }

    /**
     * Get the node reference for the /Company Home top folder in Alfresco.
     * Use the standard node locator service.
     *
     * @return the node reference for /Company Home
     */
    private NodeRef getCompanyHomeNodeRef() {
        return getServiceRegistry().getNodeLocatorService().getNode(CompanyHomeNodeLocator.NAME, null, null);
    }

    /**
     * Read text content for passed in file Node Reference
     *
     * @param nodeRef the node reference for a file containing text
     * @return the text content
     */
    private String readTextContent(NodeRef nodeRef) {
        ContentReader reader = getServiceRegistry().getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader == null) {
            return ""; // Maybe it was a folder after all
        }

        InputStream is = reader.getContentInputStream();
        try {
            return IOUtils.toString(is, "UTF-8");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

}