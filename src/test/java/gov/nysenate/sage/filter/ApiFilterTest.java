package gov.nysenate.sage.filter;

import gov.nysenate.sage.MockFilter;
import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.auth.ApiUser;
import gov.nysenate.sage.util.Config;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ApiFilterTest extends TestBase
{
    MockFilter mf;
    ApiFilter apiFilter = new ApiFilter();
    Config config;

    @Before
    public void setUp()
    {
        super.setUp();
        config = ApplicationFactory.getConfig();
        mf = new MockFilter();
    }

    @Test
    public void apiFilterAuthenticatesDefaultUser_SetsApiUserInResponse() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

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
    }

    @Test
    public void apiFilterAuthenticatesValidKey_SetsApiUserInResponse() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        /** Set remote ip to something that's not loopback.
         *  Set the key to the default key in the request */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("X.X.X.X");
        when(mf.getMockServletRequest().getParameter("key")).thenReturn(config.getValue("user.default"));

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNotNull(apiUser);
        assertEquals(config.getValue("user.default"), apiUser.getApiKey());
    }

    @Test
    public void apiFilterAuthenticatesInvalidKey_WritesInvalidKeyError() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        /** Set remote ip to something that's not loopback.
         *  Set the key to an invalid key */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("X.X.X.X");
        when(mf.getMockServletRequest().getParameter("key")).thenReturn(config.getValue("INVALID"));

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNull(apiUser);

        assertTrue(mf.getMockFilterResponseOutput().contains(ApiFilter.INVALID_KEY_MESSAGE));
    }

    @Test
    public void apiFilterAuthenticatesInvalidKey_WritesMissingKeyError() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        /** Set remote ip to something that's not loopback.
         *  Set the key to an invalid key */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("X.X.X.X");

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNull(apiUser);

        assertTrue(mf.getMockFilterResponseOutput().contains(ApiFilter.MISSING_KEY_MESSAGE));
    }
}
