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

public class HuanqiuHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(JumeiHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
        List<CrawlData> crawlDatas = Lists.newArrayList();
        
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("huanqiu.com", page));

        crawlDatas.add(new CrawlData(url, "pubtime", "新闻时间").setTextValue(getXPathValue(doc, "//STRONG[@id='pubtime_baidu']"), page));
        crawlDatas.add(new CrawlData(url, "title", "新闻标题").setTextValue(getXPathValue(doc, "//DIV[@class='conText']/H1"), page));
        crawlDatas.add(new CrawlData(url, "text", "新闻文本").setTextValue(getXPathValue(doc, "//DIV[@id='text']"), page));
        crawlDatas.add(new CrawlData(url, "html", "新闻HTML").setHtmlValue(getXPathHtml(doc, "//DIV[@id='text']"), page));

        saveCrawlData(url, crawlDatas, page);

        // 用于网页内容索引的页面内容，一般是去头去尾处理后的有效信息内容
        String txt = getXPathValue(doc, "//DIV[@class='conText']") + getXPathValue(doc, "//DIV[@id='text']");
        if (StringUtils.isNotBlank(txt)) {
            parse.setText(txt);
        } else {
            LOG.warn("NO data parased");
        }

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://.*.huanqiu.com/roll/[0-9]{4}-[0-9]{2}/[0-9]*.html$";
    }

    @Override
    protected boolean isParseDataFetchLoadedInternal(String url, String html) {
        return true;
    }

    @Override
    protected boolean isContentMatchedForParse(String url, String html) {
        return true;
    }
}