package org.apache.tapestry5.internal.jpa;

import java.util.List;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class PersistenceContentHandler implements ContentHandler
{
    private static final String NAMESPACE_URI = "http://java.sun.com/xml/ns/persistence";
    private static final String ELEMENT_PERSISTENCE_UNIT = "persistence-unit";
    private static final String ELEMENT_PROVIDER = "provider";
    private static final String ELEMENT_JTA_DATA_SOURCE = "jta-data-source";
    private static final String ELEMENT_NON_JTA_DATA_SOURCE = "non-jta-data-source";
    private static final String ELEMENT_MAPPING_FILE = "mapping-file";
    private static final String ELEMENT_JAR_FILE = "jar-file";
    private static final String ELEMENT_CLASS = "class";
    private static final String ELEMENT_EXCLUDE_UNLISTED_CLASSES = "exclude-unlisted-classes";
    private static final String ELEMENT_CACHING = "shared-cache-mode";
    private static final String ELEMENT_VALIDATION_MODE = "validation-mode";
    private static final String ELEMENT_PROPERTY = "property";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_VERSION = "value";
    private static final String ATTRIBUTE_TRANSACTION_TYPE = "transaction-type";

    private final List<TapestryPersistenceUnitInfo> persistenceUnits = CollectionFactory.newList();
    private TapestryPersistenceUnitInfo persistenceUnitInfo;
    private StringBuilder characters;

    public List<TapestryPersistenceUnitInfo> getPersistenceUnits()
    {
        return persistenceUnits;
    }

    public void setDocumentLocator(final Locator locator)
    {
    }

    public void startDocument() throws SAXException
    {
    }

    public void endDocument() throws SAXException
    {
    }

    public void startPrefixMapping(final String prefix, final String uri) throws SAXException
    {
    }

    public void endPrefixMapping(final String prefix) throws SAXException
    {
    }

    public void startElement(final String namespaceURI, final String localName, final String qName,
            final Attributes atts) throws SAXException
    {
        if (NAMESPACE_URI.equals(namespaceURI))
        {
            if (ELEMENT_PERSISTENCE_UNIT.equals(localName))
            {
                persistenceUnitInfo = new PersistenceUnitInfoImpl();
                persistenceUnitInfo.setPersistenceUnitName(atts.getValue(ATTRIBUTE_NAME));
                persistenceUnitInfo
                        .setPersistenceXMLSchemaVersion(atts.getValue(ATTRIBUTE_VERSION));

                final String transactionType = atts.getValue(ATTRIBUTE_TRANSACTION_TYPE);

                if (transactionType != null)
                {
                    persistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType
                            .valueOf(transactionType));
                }
            }
            else if (ELEMENT_PROPERTY.equals(localName))
            {
                final String name = atts.getValue(ATTRIBUTE_NAME);
                final String value = atts.getValue(ATTRIBUTE_VALUE);
                persistenceUnitInfo.getProperties().setProperty(name, value);
            }
        }
    }

    public void endElement(final String namespaceURI, final String localName, final String qName)
            throws SAXException
    {

        final String string = characters.toString().trim();
        characters = null;

        if (NAMESPACE_URI.equals(namespaceURI))
        {
            if (ELEMENT_PROVIDER.equals(localName))
            {
                persistenceUnitInfo.setPersistenceProviderClassName(string);
            }
            else if (ELEMENT_CLASS.equals(localName))
            {
                persistenceUnitInfo.addManagedClassName(string);
            }
            else if (ELEMENT_CACHING.equals(localName))
            {
                persistenceUnitInfo.setSharedCacheMode(toEnum(SharedCacheMode.class, string));
            }
            else if (ELEMENT_VALIDATION_MODE.equals(localName))
            {
                persistenceUnitInfo.setValidationMode(toEnum(ValidationMode.class, string));
            }
            else if (ELEMENT_PERSISTENCE_UNIT.equals(localName))
            {
                if (persistenceUnitInfo != null)
                {
                    persistenceUnits.add(persistenceUnitInfo);
                    persistenceUnitInfo = null;
                }
            }
        }
    }

    public void characters(final char[] ch, final int start, final int length) throws SAXException
    {
        final String s = new String(ch, start, length);

        if (characters == null)
        {
            characters = new StringBuilder(s);
        }
        else
        {
            characters.append(s);
        }

    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException
    {
    }

    public void processingInstruction(final String target, final String data) throws SAXException
    {
    }

    public void skippedEntity(final String name) throws SAXException
    {
    }

    private <T extends Enum<T>> T toEnum(final Class<T> enumType, final String value)
    {
        return Enum.valueOf(enumType, value);
    }
}
