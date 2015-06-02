package org.apache.nutch.parse.s2jh;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.TableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.google.common.collect.Lists;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class ScjtaqHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(ScjtaqHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
        List<CrawlData> crawlDatas = Lists.newArrayList();

        String articleTitle = getXPathValue(doc, "//DIV[@id='left']/DIV[@class='title']");
        if (StringUtils.isNotBlank(articleTitle)) {
            //新闻阅读页面

            crawlDatas.add(new CrawlData(url, "articleTitle", "标题名称").setTextValue(articleTitle, page));

            String nav = getXPathValue(doc, "//DIV[@id='left']/DIV[@class='nav']");
            nav = StringUtils.remove(nav, " ");
            crawlDatas.add(new CrawlData(url, "position", "导航分类").setTextValue(nav, page));

            String note = getXPathValue(doc, "//DIV[@id='left']/DIV[@class='note']");
            String date = cleanInvisibleChar(StringUtils.substringBetween(note, "www.scjtaq.com", "快速分享"), true);
            crawlDatas.add(new CrawlData(url, "publishDate", "发布日期").setTextValue(date, page));

            String txt = getXPathValue(doc, "//DIV[@id='left']/DIV[@class='cont']");
            crawlDatas.add(new CrawlData(url, "articleText", "文章内容").setTextValue(txt, page));
        } else {

            String articleList = getXPathValue(doc, "//DIV[@id='inright']/DIV[@class='single']");
            String title = TableUtil.toString(parse.getTitle());
            crawlDatas.add(new CrawlData(url, "position", "导航分类").setTextValue(title, page));
            crawlDatas.add(new CrawlData(url, "articleTitle", "标题名称").setTextValue(title, page));
            
            if (StringUtils.isNotBlank(articleList)) {
                //新闻列表界面
                crawlDatas.add(new CrawlData(url, "articleText", "文章内容").setTextValue(articleList, page));
            } else {
                //其他页面，如首页
                String text = TableUtil.toString(parse.getText());
                crawlDatas.add(new CrawlData(url, "articleText", "文章内容").setTextValue(text, page));
            }

        }

        saveCrawlData(url, crawlDatas, page);

        // 用于网页内容索引的页面内容，一般是去头去尾处理后的有效信息内容
        //        String txt = getXPathValue(doc, "//DIV[@id='left']/DIV[@class='cont']");
        //        if (StringUtils.isNotBlank(txt)) {
        //            LOG.debug("Trim text: {}", txt);
        //            parse.setText(txt);
        //        } else {
        //            LOG.warn("NO data parased");
        //        }

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        //http://www.scjtaq.com/driver/zijia/18962.html
        return "^http://www.scjtaq.com/.*.html$|http://www.scjtaq.com|http://www.scjtaq.com/";
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
