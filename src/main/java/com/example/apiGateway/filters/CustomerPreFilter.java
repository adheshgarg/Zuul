package com.example.apiGateway.filters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.net.URL;

public class CustomerPreFilter extends ZuulFilter {

    private RequestContext ctx;
    private HttpServletRequest request;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        ctx=RequestContext.getCurrentContext();
        request=ctx.getRequest();
        String uri=request.getRequestURI();
        System.out.println(uri);
        if ((uri != null) && (uri.equals("/cartandorder/cart") || uri.equals("/cartandorder/order") || uri.equals("/login/"))) {
            return true;
        }
        return false;
    }

    @Override
    public Object run() {
        Object idToken = request.getHeader("token");
        System.out.println(String.valueOf(idToken));
        String requestBody = getRequestBody(request);
        FirebaseToken decodedToken = null;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(String.valueOf(idToken));
            String uid = decodedToken.getUid();
            if (uid==null) {
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(401);
                ctx.setResponseBody("User not verified!!");
                return ctx;
            }
            System.out.println(uid);
            System.out.println(decodedToken.getEmail());
            System.out.println(decodedToken.getIssuer());
            System.out.println(decodedToken.getName() + "\n");
        } catch (Exception e) {
            ctx.setResponseStatusCode(402);
            ctx.setResponseBody("Error in token!");
            e.printStackTrace();
            return ctx;
        }
        ctx.addZuulRequestHeader("customerId", decodedToken.getUid());
        ctx.addZuulRequestHeader("customerName",decodedToken.getName());
        ctx.addZuulRequestHeader("customerEmail", decodedToken.getEmail());
        return ctx;
    }

    private String getRequestBody(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            if (reader == null) {
                System.out.println("NO BODY");
                return "NO BODY";
            }
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (final Exception e) {
//            logger.trace("Could not obtain the saml request body from the http request", e);
            return e.getMessage();
        }
    }
}
