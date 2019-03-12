package gov.nysenate.sage.filter;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.model.api.ApiUser;
import gov.nysenate.sage.model.result.ResultStatus;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Category(IntegrationTest.class)
public class ApiFilterIT extends BaseTests {

    private MockFilter mf = new MockFilter();

    @Autowired
    private ApiFilter apiFilter;

    @Autowired
    private Environment env;

    private static String validUri = "/api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY";

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void apiFilterAuthenticateDefaultUser() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("127.0.0.1");
        assertEquals("127.0.0.1", mf.getMockServletRequest().getRemoteAddr());

        /* Check that the ip filter is set in the config */
        assertNotEquals("", env.getUserIpFilter());

        /* Check that the writer is initialized */
        assertNotNull(mf.getMockServletResponse().getWriter());

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        /* Verify that apiUser has been set */
        assertNotNull(mf.getMockServletRequest().getAttribute("apiRequest"));

        /* Verify that filter proceeds */
        verify(mf.getMockFilterChain(), only()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void apiFilterAuthenticatesValidKey() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);

        /* Set remote ip to something that's not loopback.
         *  Set the key to the default key in the request */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("192.168.0.1");
        when(mf.getMockServletRequest().getParameter("key")).thenReturn(env.getUserDefaultKey());

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        assertNotNull(mf.getMockServletRequest().getAttribute("apiRequestId"));

        /* Verify that filter proceeds since uri is valid api format */
        verify(mf.getMockFilterChain(), only()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void apiFilterRejectsInvalidKey() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        /* Set remote ip to something that's not loopback.
         *  Set the key to an invalid key */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("192.168.0.1");
        when(mf.getMockServletRequest().getParameter("key")).thenReturn("FAKEKEY");

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        assertNull(mf.getMockServletRequest().getAttribute("apiRequestId"));

        assertTrue(mf.getMockFilterResponseOutput().contains(ResultStatus.API_KEY_INVALID.name()));

        /* Verify that filter does NOT proceed */
        verify(mf.getMockFilterChain(), never()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void apiFilterRejectsMissingKey() throws Exception
    {
        apiFilter.init(mf.getMockFilterConfig());

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        /* Set remote ip to something that's not loopback.
         *  Set the key to an invalid key */
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("192.168.0.1");

        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        ApiUser apiUser = (ApiUser) mf.getMockServletRequest().getAttribute("apiUser");
        assertNull(apiUser);

        assertTrue(mf.getMockFilterResponseOutput().contains(ResultStatus.API_KEY_MISSING.name()));

        /* Verify that filter does NOT proceed */
        verify(mf.getMockFilterChain(), never()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void apiFilterParsesValidURI() throws Exception
    {
        String validBodyUri = "/GeoApi/api/testMethod/json/body/param?somestuff";

        apiFilter.init(mf.getMockFilterConfig());
        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("127.0.0.1");

        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validUri);
        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        /* Verify that filter does proceed */
        verify(mf.getMockFilterChain(), atLeastOnce()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));

        /* Now check that the body input uri works as well */
        when(mf.getMockServletRequest().getRequestURI()).thenReturn(validBodyUri);
        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        /* Verify that filter does proceed */
        verify(mf.getMockFilterChain(), atLeastOnce()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void apiFilterParsesInvalidURI() throws Exception
    {
        String invalidUri = "/GeoApi/api/param?addr1=";

        apiFilter.init(mf.getMockFilterConfig());
        when(mf.getMockServletRequest().getRemoteAddr()).thenReturn("127.0.0.1");

        /* Missing format */
        when(mf.getMockServletRequest().getRequestURI()).thenReturn(invalidUri);
        apiFilter.doFilter(mf.getMockServletRequest(), mf.getMockServletResponse(), mf.getMockFilterChain());

        /* Check that the response has the invalid api format message */
        assertTrue(mf.getMockFilterResponseOutput().contains(ResultStatus.API_REQUEST_INVALID.name()));

        /* Verify that filter does NOT proceed */
        verify(mf.getMockFilterChain(), never()).doFilter(isA(ServletRequest.class), isA(ServletResponse.class));
    }
}
