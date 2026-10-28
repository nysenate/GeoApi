package gov.nysenate.sage.client.response.base;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public record ListResponse<T>(
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        ImmutableList<T> items) {
    public ListResponse(Collection<T> items) {
        this(ImmutableList.copyOf(items));
    }

    public int getSize() {
        return items.size();
    }
}
