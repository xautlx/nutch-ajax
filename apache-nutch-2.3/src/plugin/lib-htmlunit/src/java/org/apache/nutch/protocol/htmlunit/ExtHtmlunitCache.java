package org.apache.nutch.protocol.htmlunit;

import org.apache.commons.lang.StringUtils;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Add Cache-Control header info process for htmlunit cache
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 */
public class ExtHtmlunitCache extends Cache {

    private static final long serialVersionUID = 5481919471569877756L;

    protected boolean isDynamicContent(final WebResponse response) {
        final String cacheControl = response.getResponseHeaderValue("Cache-Control");
        if (StringUtils.isNotBlank(cacheControl) && cacheControl.toLowerCase().indexOf("max-age") > -1) {
            return false;
        }

        return super.isDynamicContent(response);
    }
}
