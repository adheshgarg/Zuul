package com.example.apiGateway.filters;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;

public class MerchantPreFilter extends ZuulFilter {

    private RequestContext ctx;
    private HttpServletRequest request;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        ctx=RequestContext.getCurrentContext();
        request=ctx.getRequest();
        String uri=request.getRequestURI();
        System.out.println(uri);
        if ((uri != null) && (uri.startsWith("/merchant/") || uri.equals("/product/addProduct")) && !uri.equals("/merchant/add")) {
            return true;
        }
        return false;
    }

    @Override
    public Object run() {
        Object idToken= request.getHeader("token");
        System.out.println(String.valueOf(idToken));
        FirebaseToken decodedToken = null;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(String.valueOf(idToken));
            String uid=decodedToken.getUid();
            if(uid==null){
                ctx.setSendZuulResponse(false);
                ctx.setResponseStatusCode(401);
                ctx.setResponseBody("User not verified!!");
            }
            System.out.println(uid);
            System.out.println(decodedToken.getEmail());
            System.out.println(decodedToken.getIssuer());
            System.out.println(decodedToken.getName()+"\n");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ctx.addZuulRequestHeader("merchantId", decodedToken.getUid());
        ctx.addZuulRequestHeader("merchantName",decodedToken.getName());
        ctx.addZuulRequestHeader("merchantEmail", decodedToken.getEmail());
        return ctx;
    }
}
