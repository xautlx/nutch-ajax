package org.apache.nutch.protocol.htmlunit;

import org.apache.commons.lang.StringUtils;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Add Cache-Control header info process for htmlunit cache
 * 
 * @author EMAIL:xautlx@hotmail.com , QQ:2414521719
 */
public class ExtHtmlunitCache extends Cache {

    protected boolean isDynamicContent(final WebResponse response) {
        final String cacheControl = response.getResponseHeaderValue("Cache-Control");
        if (StringUtils.isNotBlank(cacheControl) && cacheControl.toLowerCase().indexOf("max-age") > -1) {
            return false;
        }

        return super.isDynamicContent(response);
    }
}
