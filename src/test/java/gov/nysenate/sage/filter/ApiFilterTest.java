package gov.nysenate.sage.filter;

import gov.nysenate.sage.MockFilter;
import gov.nysenate.sage.TestBase;
import static gov.nysenate.sage.controller.api.base.RequestAttribute.*;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.auth.ApiUser;
import gov.nysenate.sage.util.Config;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test of ApiFilter using a MockFilter to simulate a filter chain environment.
 * @see MockFilter
 */
public class ApiFilterTest extends TestBase
{
    MockFilter mf;
    ApiFilter apiFilter = new ApiFilter();
    Config config;

    public static String validUri = "/GeoApi/api/testMethod/json/param?addr1=";
    public static String invalidUri = "/GeoApi/api/param?addr1=";
    public static String validBodyUri = "/GeoApi/api/testMethod/json/body/param?somestuff";

    @Before
    public void setUp()
    {
        config = ApplicationFactory.getConfig();
        mf = new MockFilter();
    }

    @Test
    public void apiFilterAuthenticatesDefaultUser_SetsApiUserInRequestIfValidUri() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("127.0.0.1");
        assertEquals("127.0.0.1", mf.getMockServletRequest().getRemoteAddr());

        /** Check that the ip filter is set in the config */
        assertEquals("(127.0.0.1)", config.getValue("user.ip_filter"));

        /** Check that the writer is initialized */
        assertNotNull(mf.getMockServletResponse().getWriter());

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        /** Verify that apiUser has been set */
        verify(mf.getMockServletRequest()).setAttribute(eq("apiUser"), isA(ApiUser.class));

        /** Check to see if the apiUser attribute was set with the ApiUser */
        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNotNull(apiUser);
        assertEquals(config.getValue("user.default"), apiUser.getApiKey());

        /** Verify that filter proceeds */
        verify(mf.getMockFilterChain(), only()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    public void apiFilterAuthenticatesValidKey_SetsApiUserInRequestIfValidUri() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);

        /** Set remote ip to something that's not loopback.
         *  Set the key to the default key in the request */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("X.X.X.X");
        when(mf.getMockServletRequest().getParameter("key")).thenReturn(config.getValue("user.default"));

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNotNull(apiUser);
        assertEquals(config.getValue("user.default"), apiUser.getApiKey());

        /** Verify that filter proceeds since uri is valid api format */
        verify(mf.getMockFilterChain(), only()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    public void apiFilterAuthenticatesInvalidKey_WritesInvalidKeyError() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        /** Set remote ip to something that's not loopback.
         *  Set the key to an invalid key */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("X.X.X.X");
        when(mf.getMockServletRequest().getParameter("key")).thenReturn(config.getValue("INVALID"));

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNull(apiUser);

        assertTrue(mf.getMockFilterResponseOutput().contains(ApiFilter.INVALID_KEY_MESSAGE));

        /** Verify that filter does NOT proceed */
        verify(mf.getMockFilterChain(), never()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    public void apiFilterAuthenticatesInvalidKey_WritesMissingKeyError() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        /** Set remote ip to something that's not loopback.
         *  Set the key to an invalid key */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("X.X.X.X");

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNull(apiUser);

        assertTrue(mf.getMockFilterResponseOutput().contains(ApiFilter.MISSING_KEY_MESSAGE));

        /** Verify that filter does NOT proceed */
        verify(mf.getMockFilterChain(), never()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    public void apiFilterParsesValidURI_SetsRequestAttributes() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());
        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("127.0.0.1");

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());
        assertEquals("testMethod", mf.getMockServletRequest().getAttribute(REQUEST_TYPE.toString()));
        assertEquals("json", mf.getMockServletRequest().getAttribute(FORMAT.toString()));
        assertEquals("url", mf.getMockServletRequest().getAttribute(PARAM_SOURCE.toString()));
        assertEquals("param", mf.getMockServletRequest().getAttribute(PARAM_TYPE.toString()));

        /** Verify that filter does proceed */
        verify(mf.getMockFilterChain(), atLeastOnce()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));

        /** Now check that the body input uri works as well */
        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validBodyUri);
        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());
        assertEquals("testMethod", mf.getMockServletRequest().getAttribute(REQUEST_TYPE.toString()));
        assertEquals("json", mf.getMockServletRequest().getAttribute(FORMAT.toString()));
        assertEquals("body", mf.getMockServletRequest().getAttribute(PARAM_SOURCE.toString()));
        assertEquals("param", mf.getMockServletRequest().getAttribute(PARAM_TYPE.toString()));

        /** Verify that filter does proceed */
        verify(mf.getMockFilterChain(), atLeastOnce()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    public void apiFilterParsesInvalidURI_BlocksFilterChain() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("127.0.0.1");

        /** Missing format */
        when(mf.getMockServletRequest().getRequestURI()).thenReturn(invalidUri);
        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        /** Check that the response has the invalid api format message */
        assertTrue(mf.getMockFilterResponseOutput().contains(ApiFilter.INVALID_API_FORMAT));

        /** Verify that filter does NOT proceed */
        verify(mf.getMockFilterChain(), never()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }
}
