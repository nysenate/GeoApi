package gov.nysenate.sage.model;

import gov.nysenate.sage.util.SerializedFormat;

/**
 * Defines a common interface for all model objects that are used in API Services.
 */
public interface ApiModel
{
    /**
     * The purpose of the toMap() method is to provide a map of primitive objects that the object mappers
     * can consistently digest to output correct JSON, XML, etc.
     *
     * toMap() should build and return a LinkedHashMap containing the desired values to be serialized.
     * The order of insertion into the map should be preserved by using a LinkedHashMap.
     *
     * @return LinkedHashMap map of fields and values
     */
    public SerializedFormat toSerializedFormat();
}
