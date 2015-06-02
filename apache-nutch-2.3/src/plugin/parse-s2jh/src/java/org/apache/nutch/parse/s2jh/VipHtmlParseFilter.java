package org.apache.nutch.parse.s2jh;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.google.common.collect.Lists;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class VipHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(VipHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) {
        List<CrawlData> crawlDatas = Lists.newArrayList();

        crawlDatas.add(new CrawlData(url, "title", "商品名称").setTextValue(getXPathValue(doc, "//DIV[@class='pib-title']/P[@class='pib-title-detail']"),
                page));
        crawlDatas.add(new CrawlData(url, "price", "价格").setTextValue(getXPathValue(doc, "//SPAN[@class='pbox-price']/EM"), page));

        saveCrawlData(url, crawlDatas, page);

        // 用于网页内容索引的页面内容，一般是去头去尾处理后的有效信息内容
        String txt = getXPathValue(doc, "//DIV[@class='FW-wrap fwr']");
        if (StringUtils.isNotBlank(txt)) {
            parse.setText(txt);
        } else {
            LOG.warn("NO data parased");
        }

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        //http://www.vip.com/detail-371486-48229111.html
        return "http://www.vip.com/detail-\\d*-\\d*.html";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {
        //页面源码已包含所有需要的信息
        return true;
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
}
