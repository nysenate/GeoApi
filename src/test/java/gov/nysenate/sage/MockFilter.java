package gov.nysenate.sage;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockFilter extends TestBase
{
    protected FilterConfig mockFilterConfig;
    protected FilterChain mockFilterChain;
    protected ServletRequest mockServletRequest;
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
        super.setUp();
        mockFilterConfig = mock(FilterConfig.class);
        mockFilterChain = mock(FilterChain.class);
        mockServletRequest = mock(HttpServletRequest.class);
        mockServletResponse = mock(HttpServletResponse.class);

        try
        {

            when(mockServletResponse.getWriter()).thenReturn(this.pWriter);

            doAnswer(new Answer() {
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    Object[] args = invocationOnMock.getArguments();
                    attributes.put((String) args[0], args[1]);
                    return null;
                }
            }).
            when(mockServletRequest).setAttribute(isA(String.class), isA(Object.class));

            when(mockServletRequest.getAttribute(isA(String.class))).thenAnswer(new Answer(){
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return attributes.get(invocationOnMock.getArguments()[0]);
                }
            });
        }
        catch (Exception ex)
        {
            System.err.println(ex.getMessage());
        }
    }

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

    public ServletRequest getMockServletRequest() {
        return mockServletRequest;
    }

    public ServletResponse getMockServletResponse() {
        return mockServletResponse;
    }
}
