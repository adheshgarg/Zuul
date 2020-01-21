package com.example.apiGateway.filters;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;

public class MerchantPreFilter extends ZuulFilter {

    private RequestContext ctx;
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        System.out.println(ctx.get("proxy"));
        if ((ctx.get("proxy") != null) && ctx.get("proxy").equals("merchant")) {
            return true;
        }
        return false;
    }

    @Override
    public Object run() {
        ctx=RequestContext.getCurrentContext();
        HttpServletRequest request=ctx.getRequest();
        String idToken= String.valueOf(request.getHeaders("token"));
        FirebaseToken decodedToken = null;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();
        }
        catch (FirebaseAuthException e) {
            e.printStackTrace();
        }
        return null;
    }
}
