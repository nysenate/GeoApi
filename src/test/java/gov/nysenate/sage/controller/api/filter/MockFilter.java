package gov.nysenate.sage.controller.api.filter;

import gov.nysenate.sage.BaseTests;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/**
 * In order to test filter operation a MockFilter implementation is supplied here to
 * simulate any methods that are called in code. The mock objects are created using the
 * Mockito library. The documentation will be useful for constructing mock method
 * implementations for this class.
 * (Documentation subject to change)
 * <a href="http://docs.mockito.googlecode.com/hg/org/mockito/Mockito.html">...</a>
 */
public class MockFilter extends BaseTests
{
    protected FilterConfig mockFilterConfig;
    protected FilterChain mockFilterChain;
    protected HttpServletRequest mockServletRequest;
    protected HttpServletResponse mockServletResponse;

    StringWriter sWriter = new StringWriter();
    PrintWriter pWriter = new PrintWriter(sWriter);
    protected HashMap<String,Object> attributes = new HashMap<>();

    public MockFilter()
    {
        this.setUp();
    }

    public void setUp()
    {
        mockFilterConfig = mock(FilterConfig.class);
        mockFilterChain = mock(FilterChain.class);
        mockServletRequest = mock(HttpServletRequest.class);
        mockServletResponse = mock(HttpServletResponse.class);

        try
        {
            /** Mock getWriter() method for ServletResponse */
            when(mockServletResponse.getWriter()).thenReturn(this.pWriter);

            /** Mock setAttribute() method for ServletRequest */
            doAnswer(invocationOnMock -> {
                Object[] args = invocationOnMock.getArguments();
                attributes.put((String) args[0], args[1]);
                return null;
            }).when(mockServletRequest).setAttribute(isA(String.class), isA(Object.class));

            /** Mock getAttribute(String) method for ServletRequest */
            when(mockServletRequest.getAttribute(isA(String.class))).thenAnswer((Answer) invocationOnMock -> attributes.get(invocationOnMock.getArguments()[0]));
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

    /** Mock output method for ServletResponse */
    public String getMockFilterResponseOutput()
    {
        return sWriter.getBuffer().toString();
    }

    public FilterConfig getMockFilterConfig() {
        return mockFilterConfig;
    }

    public FilterChain getMockFilterChain() {
        return mockFilterChain;
    }

    public HttpServletRequest getMockServletRequest() {
        return mockServletRequest;
    }

    public HttpServletResponse getMockServletResponse() {
        return mockServletResponse;
    }
}
