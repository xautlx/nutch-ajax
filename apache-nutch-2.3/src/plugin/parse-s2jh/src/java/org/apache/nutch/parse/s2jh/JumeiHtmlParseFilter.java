package org.apache.nutch.parse.s2jh;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class JumeiHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(JumeiHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
        List<CrawlData> crawlDatas = Lists.newArrayList();
        crawlDatas.add(new CrawlData(url, "domain", "域名").setTextValue("jumei.com", page));

        if (url.startsWith("http://item.jumei.com/")) {
            crawlDatas.add(new CrawlData(url, "name", "商品名称").setTextValue(
                    getXPathValue(doc, "//DIV[@id='product_parameter']/DIV[@class='deal_con_content']/TABLE/TBODY/TR[1]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "brand", "品牌").setTextValue(
                    getXPathValue(doc, "//DIV[@id='product_parameter']/DIV[@class='deal_con_content']/TABLE/TBODY/TR[2]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "category", "分类").setTextValue(
                    getXPathValue(doc, "//DIV[@id='product_parameter']/DIV[@class='deal_con_content']/TABLE/TBODY/TR[3]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "purpose", "功效").setTextValue(
                    getXPathValue(doc, "//DIV[@id='product_parameter']/DIV[@class='deal_con_content']/TABLE/TBODY/TR[4]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "price", "价格").setNumValue(getXPathValue(doc, "//SPAN[@class='deal_accout_two']"), page));
            crawlDatas.add(new CrawlData(url, "refPrice", "参考价格").setNumValue(getXPathValue(doc, "//SPAN[@class='ref_price']"), page));

            String primaryImageSrc = getImgSrcValue(url, doc, "//IMG[@id='detail_main_img']", "//IMG[@id='firstGroupImg']");
            //将详情界面的第一个小图转换为对应列表界面的大图
            primaryImageSrc = primaryImageSrc.replace("_100_100", "_350_350");
            crawlDatas.add(new CrawlData(url, "primaryImage", "主图").setImgValue(primaryImageSrc, page));
        } else if (url.startsWith("http://www.jumeiglobal.com/deal/")) {
            //所需数据在textarea元素下面，htmlunit在printXml时会对textarea内容进行escape处理，导致得到的doc对象无法直接XPath定位
            //因此需要先提前textarea元素内容转换为单独的DocumentFragment对象，然后基于此文档对象进行数据解析
            String htmlFragment = getXPathValue(doc, "//DIV[@id='spxx']/DIV[@class='content_text']/TEXTAREA");
            StringReader sr = new StringReader(htmlFragment);
            InputSource is = new InputSource(sr);
            DocumentFragment docFragment = parse(is);

            crawlDatas.add(new CrawlData(url, "name", "商品名称").setTextValue(
                    getXPathValue(docFragment, "//DIV[@class='deal_con_content']/TABLE/TBODY/TR[1]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "brand", "品牌").setTextValue(
                    getXPathValue(docFragment, "//DIV[@class='deal_con_content']/TABLE/TBODY/TR[3]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "category", "分类").setTextValue(
                    getXPathValue(docFragment, "//DIV[@class='deal_con_content']/TABLE/TBODY/TR[4]/TD[2]/SPAN"), page));
            crawlDatas.add(new CrawlData(url, "purpose", "功效").setTextValue(
                    getXPathValue(docFragment, "//DIV[@class='deal_con_content']/TABLE/TBODY/TR[5]/TD[2]/SPAN"), page));

            crawlDatas.add(new CrawlData(url, "price", "价格").setNumValue(
                    getXPathValue(doc, "//DIV[@class='shoping_attribute']//STRONG[@class='jumei_price']/text()"), page));
            crawlDatas.add(new CrawlData(url, "refPrice", "参考价格").setNumValue(
                    getXPathValue(doc, "//DIV[@class='shoping_attribute']//SPAN[@class='market_price']/text()"), page));

            String primaryImageSrc = getImgSrcValue(url, doc, "//IMG[@id='deal_img']");
            crawlDatas.add(new CrawlData(url, "primaryImage", "主图").setImgValue(primaryImageSrc, page));
        }

        /**
        SELECT url ,fetch_time, 
        GROUP_CONCAT(CASE WHEN  code = 'domain'  THEN  text_value ELSE  null  END)   AS  `domain`,
        GROUP_CONCAT(CASE WHEN  code = 'name'  THEN  text_value ELSE  null  END)   AS  `name`,
        GROUP_CONCAT(CASE WHEN  code = 'brand'  THEN  text_value ELSE  null  END)   AS  `brand`,
        GROUP_CONCAT(CASE WHEN  code = 'category'  THEN  text_value ELSE  null  END)   AS  `category`,
        GROUP_CONCAT(CASE WHEN  code = 'purpose'  THEN  text_value ELSE  null  END)   AS  `purpose`,
        GROUP_CONCAT(CASE WHEN  code = 'price'  THEN  num_value ELSE  null  END)   AS  `price`,
        GROUP_CONCAT(CASE WHEN  code = 'refPrice'  THEN  num_value ELSE  null  END)   AS  `refPrice`,
        GROUP_CONCAT(CASE WHEN  code = 'primaryImage'  THEN  text_value ELSE  null  END)   AS  `primaryImage`
        FROM crawl_data GROUP BY url,fetch_time
         */
        saveCrawlData(url, crawlDatas, page);

        // 用于网页内容索引的页面内容，一般是去头去尾处理后的有效信息内容
        String txt = getXPathValue(doc, "//DIV[@class='deal_content']") + getXPathValue(doc, "//DIV[@class='product_detail_l fl']");
        if (StringUtils.isNotBlank(txt)) {
            parse.setText(txt);
        } else {
            LOG.warn("NO data parased");
        }

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        //http://item.jumei.com/sh150311p619680.html
        //http://www.jumeiglobal.com/deal/ht150122p933681t1.html
        return "http://item.jumei.com/.*.html|http://www.jumeiglobal.com/deal/.*.html";
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
