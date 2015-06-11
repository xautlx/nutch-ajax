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
public class JtCpdHtmlParseFilter extends AbstractHtmlParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(JtCpdHtmlParseFilter.class);

    @Override
    public Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception {
        List<CrawlData> crawlDatas = Lists.newArrayList();

        String articleTitle = getXPathValue(doc, "//DIV[@id='content']//GETTITLE");
        if (StringUtils.isNotBlank(articleTitle)) {
            //新闻阅读页面

            crawlDatas.add(new CrawlData(url, "articleTitle", "标题名称").setTextValue(articleTitle, page));

            String nav = getXPathValue(doc, "//DIV[@id='c_left']/DIV[@id='path']");
            nav = StringUtils.remove(nav, " ");
            crawlDatas.add(new CrawlData(url, "position", "导航分类").setTextValue(nav, page));

            String note = getXPathValue(doc, "//DIV[@id='c_left']//SPAN[@id='pub_time_report']");
            String date = cleanInvisibleChar(note, true);
            crawlDatas.add(new CrawlData(url, "publishDate", "发布日期").setTextValue(date, page));

            String txt = getXPathValue(doc, "//DIV[@id='c_left']//DIV[@class='news_contents hetitle_01']");
            crawlDatas.add(new CrawlData(url, "articleText", "文章内容").setTextValue(txt, page));
        } else {

            String articleList = getXPathValue(doc, "//DIV[@id='newslist']");
            if (StringUtils.isNotBlank(articleList)) {
                //新闻列表界面
                crawlDatas.add(new CrawlData(url, "articleText", "文章内容").setTextValue(articleList, page));

                String nav = getXPathValue(doc, "//DIV[@id='path_left']");
                nav = StringUtils.remove(nav, " ");
                crawlDatas.add(new CrawlData(url, "position", "导航分类").setTextValue(nav, page));

                crawlDatas.add(new CrawlData(url, "articleTitle", "标题名称").setTextValue(nav, page));
            } else {
                //其他页面，如首页
                String text = TableUtil.toString(parse.getText());
                crawlDatas.add(new CrawlData(url, "articleText", "文章内容").setTextValue(text, page));

                String title = TableUtil.toString(parse.getTitle());
                crawlDatas.add(new CrawlData(url, "position", "导航分类").setTextValue(title, page));
                crawlDatas.add(new CrawlData(url, "articleTitle", "标题名称").setTextValue(title, page));
            }

        }

        saveCrawlData(url, crawlDatas, page);

        return parse;
    }

    @Override
    public String getUrlFilterRegex() {
        return "^http://jt.cpd.com.cn/.*.html$|http://jt.cpd.com.cn/|http://jt.cpd.com.cn";
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
