package org.apache.nutch.indexer.s2jh;

import java.util.regex.Pattern;

import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author EMAIL:xautlx@hotmail.com , QQ:2414521719
 *
 */
public class S2jhDiscardIndexingFilter extends AbstractIndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(S2jhDiscardIndexingFilter.class);

    //http://detail.tmall.com/item.htm?spm=a220o.1000855.w5003-5270320300.15.NhnkIC&id=36641396665&mt&scene=taobao_shop
    private Pattern keepIndexPattern = Pattern.compile("http://.*.china.com/.*/[0-9]*/[0-9|_|-]*.html");

    @Override
    public NutchDocument filterInternal(NutchDocument doc, String url, WebPage page) {

        if (!keepIndexPattern.matcher(url.toString()).find()) {
            LOG.info("Skip index for {} as not match regex [{}]", url, keepIndexPattern);
            doc = null;
            return null;
        }

        return doc;
    }
}
