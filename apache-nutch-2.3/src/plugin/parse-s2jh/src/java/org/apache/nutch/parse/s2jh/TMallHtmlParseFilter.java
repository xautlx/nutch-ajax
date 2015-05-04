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
public class TMallHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(TMallHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) {
        List<CrawlData> crawlDatas = Lists.newArrayList();

        crawlDatas.add(new CrawlData(url, "title", "商品名称").setTextValue(getXPathValue(doc, "//DIV[@class='tb-detail-hd']/H1")));
        crawlDatas.add(new CrawlData(url, "tm-price", "价格区间").setTextValue(getXPathValue(doc, "//SPAN[@class='tm-price']")));
        crawlDatas.add(new CrawlData(url, "description", "描述HTML").setHtmlValue(getXPathHtml(doc, "//DIV[@id='description']")));

        saveCrawlData(url, crawlDatas, page);

        // 用于网页内容索引的页面内容，一般是去头去尾处理后的有效信息内容
        String txt = getXPathValue(doc, "//DIV[@id='page']");
        if (StringUtils.isNotBlank(txt)) {
            parse.setText(txt);
        } else {
            LOG.warn("NO data parased");
        }

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        return "http://detail.tmall.com/item.htm.*";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {
        return !html.contains("描述加载中");
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
}
