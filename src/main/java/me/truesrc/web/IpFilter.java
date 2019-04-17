package me.truesrc.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

import static me.truesrc.web.IpBlocksHelper.getClientIpAddress;


/**
 * @author truesrc
 * @since 28.03.2019
 */

@Component
public class IpFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(IpFilter.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private IpBlocksHelper ipBlocksHelper;

    @Autowired
    private IpFilter(IpBlocksHelper ipBlocksHelper) {
        this.ipBlocksHelper = ipBlocksHelper;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        ipBlocksHelper.start();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        log.info("RemoteAddr: " + req.getRemoteAddr());
        try {
            if (ipBlocksHelper.getIps().contains(getClientIpAddress(req))) {
                res.getOutputStream().write(mapper.writeValueAsBytes("Bad IP " + getClientIpAddress(req)));
            } else chain.doFilter(req, res);
        } catch (IOException | ServletException e) {
            log.error(e.getMessage());
        }
    }


    @Override
    public void destroy() {
    }
}


