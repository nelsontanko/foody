package dev.account.filter;

import dev.FoodyIntegrationTest;
import dev.security.AuthoritiesConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@AutoConfigureMockMvc
@FoodyIntegrationTest
class SpaWebFilterIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFilterForwardsToIndex() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void testFilterDoesNotForwardToIndexForApi() throws Exception {
        mockMvc.perform(get("/api/account/authenticate")).andExpect(status().isOk()).andExpect(forwardedUrl(null));
    }

    @Test
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void testFilterDoesNotForwardToIndexForV3ApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(forwardedUrl(null));
    }


    @Test
    void getBackendEndpoint() throws Exception {
        mockMvc.perform(get("/test")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedFirstLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedThirdLevelMapping() throws Exception {
        mockMvc.perform(get("/first-level/second-level/third-level")).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardUnmappedDeepMapping() throws Exception {
        mockMvc.perform(get("/1/2/3/4/5/6/7/8/9/10")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void getUnmappedFirstLevelFile() throws Exception {
        mockMvc.perform(get("/foo.js")).andExpect(status().isNotFound());
    }

}
