package org.apache.nutch.parse.s2jh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseFilter;
import org.apache.nutch.parse.html.DOMBuilder;
import org.apache.nutch.parse.s2jh.CrawlData.ValueType;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;
import org.apache.nutch.protocol.htmlunit.HttpWebClient;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.storage.WebPage.Field;
import org.apache.nutch.util.StringUtil;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.Page;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public abstract class AbstractHtmlParseFilter implements ParseFilter {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractHtmlParseFilter.class);

    private static final long start = System.currentTimeMillis(); // start time of fetcher run

    private AtomicInteger pages = new AtomicInteger(0); // total pages fetched

    private Pattern filterPattern;

    protected Transformer transformer;

    private Configuration conf;

    private static String imgSaveRootDir;

    private String defaultCharEncoding;

    private String parserImpl;

    public void setConf(Configuration conf) {
        this.conf = conf;
        String filterRegex = getUrlFilterRegex();
        if (StringUtils.isNotBlank(filterRegex)) {
            this.filterPattern = Pattern.compile(getUrlFilterRegex());
        }

        this.parserImpl = getConf().get("parser.html.impl", "neko");
        this.defaultCharEncoding = getConf().get("parser.character.encoding.default", "windows-1252");

        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //transformer.setOutputProperty(OutputKeys.INDENT, "no");
            //transformer.setOutputProperty(OutputKeys.METHOD, "html");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Configuration getConf() {
        return this.conf;
    }

    private String convertXPath(String xpath) {
        String[] paths = xpath.split("/");
        List<String> convertedPaths = Lists.newArrayList();
        for (String path : paths) {
            if ("text()".equalsIgnoreCase(path)) {
                convertedPaths.add(path.toLowerCase());
            }
            if (path.indexOf("[") > -1) {
                String[] splits = StringUtils.split(path, "[");
                convertedPaths.add(splits[0].toUpperCase() + "[" + splits[1]);
            } else {
                convertedPaths.add(path.toUpperCase());
            }
        }

        String convertedPath = StringUtils.join(convertedPaths, "/");
        LOG.trace("Converted XPath is: {}", convertedPath);
        return convertedPath;
    }

    /**
     * 基于xpath获取Node列表
     * @param node
     * @param xpath
     * @return
     */
    protected NodeList selectNodeList(Node node, String xpath) {
        try {
            xpath = convertXPath(xpath);
            return XPathAPI.selectNodeList(node, xpath);
        } catch (TransformerException e) {
            LOG.warn("Bad 'xpath' expression [{}]", xpath);
        }
        return null;
    }

    /**
     * 基于xpath获取Node节点
     * @param node
     * @param xpath
     * @return
     */
    protected Node selectSingleNode(Node contextNode, String xpath) {
        try {
            xpath = convertXPath(xpath);
            return XPathAPI.selectSingleNode(contextNode, xpath);
        } catch (TransformerException e) {
            LOG.warn("Bad 'xpath' expression [{}]", xpath);
        }
        return null;
    }

    /**
     * 基于xpath定义的img元素解析返回完整路径格式的URL字符串
     * @param url 页面URL，有些img的src元素为相对路径，通过此url合并组装图片完整URL路径
     * @param contextNode
     * @param xpaths 多个xpath字符串，主要用于容错处理，有些页面格式不统一，可能一会所需图片在xpath1，有些在xpath2，给定多个可能的xpath列表，按顺序循环找到一个匹配就终止循环
     * @return http开头的完整路径图片URL
     */
    protected String getImgSrcValue(String url, Node contextNode, String... xpaths) {
        for (String xpath : xpaths) {
            Node node = selectSingleNode(contextNode, xpath);
            String imgUrl = null;
            if (node != null) {
                NamedNodeMap atrributes = node.getAttributes();
                Node attr = atrributes.getNamedItem("data-ks-lazyload");
                if (attr == null) {
                    attr = atrributes.getNamedItem("lazy-src");
                }
                if (attr == null) {
                    attr = atrributes.getNamedItem("src");
                }
                if (attr != null) {
                    imgUrl = attr.getTextContent();
                }
            }
            if (StringUtils.isNotBlank(imgUrl)) {
                return parseImgSrc(url, imgUrl);
            }
        }
        return "";
    }

    /**
     * 基于xpath定位返回text内容，如果未找到元素返回null
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getXPathValue(Node contextNode, String xpath) {
        return getXPathValue(contextNode, xpath, null);
    }

    /**
     * 基于xpath定位返回text内容，如果未找到元素则返回默认defaultVal
     * @param contextNode
     * @param xpath
     * @param defaultVal
     * @return
     */
    protected String getXPathValue(Node contextNode, String xpath, String defaultVal) {
        NodeList nodes = selectNodeList(contextNode, xpath);
        if (nodes == null || nodes.getLength() <= 0) {
            return defaultVal;
        }
        String txt = "";
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Text) {
                txt += node.getNodeValue();
            } else {
                txt += node.getTextContent();
            }
        }
        return cleanInvisibleChar(txt);

    }

    /**
     * 基于xpath返回对应的html格式内容
     * @param contextNode
     * @param xpath
     * @return
     */
    protected String getXPathHtml(Node contextNode, String xpath) {
        Node node = selectSingleNode(contextNode, xpath);
        return asString(node);
    }

    private String asString(Node node) {
        if (node == null) {
            return "";
        }
        try {
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            String xml = writer.toString();
            xml = StringUtils.substringAfter(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xml = xml.trim();
            return xml;
        } catch (Exception e) {
            throw new IllegalArgumentException("error for parse node to string.", e);
        }
    }

    /**
     * 处理不同src图片属性格式，返回统一格式的http格式的图片URL
     * @param url
     * @param imgSrc
     * @return
     */
    private String parseImgSrc(String url, String imgSrc) {
        if (StringUtils.isBlank(imgSrc)) {
            return "";
        }
        imgSrc = imgSrc.trim();
        //去掉链接最后的#号
        imgSrc = StringUtils.substringBefore(imgSrc, "#");
        if (imgSrc.startsWith("http")) {
            return imgSrc;
        } else if (imgSrc.startsWith("/")) {
            if (url.indexOf(".com") > -1) {
                return StringUtils.substringBefore(url, ".com/") + ".com" + imgSrc;
            } else if (url.indexOf(".net") > -1) {
                return StringUtils.substringBefore(url, ".net/") + ".net" + imgSrc;
            } else {
                throw new RuntimeException("Undefined site domain suffix");
            }
        } else {
            return StringUtils.substringBeforeLast(url, "/") + "/" + imgSrc;
        }
    }

    /**
     * 清除无关的不可见空白字符
     * @param str
     * @return
     */
    protected String cleanInvisibleChar(String str) {
        return cleanInvisibleChar(str, false);
    }

    /**
     * 清除无关的不可见空白字符
     * @param str
     * @param includingBlank 是否包括移除文本内部的空白字符
     * @return
     */
    protected String cleanInvisibleChar(String str, boolean includingBlank) {
        if (str != null) {
            str = StringUtils.remove(str, (char) 160);
            if (includingBlank) {
                //普通空格
                str = StringUtils.remove(str, " ");
                //全角空格
                str = StringUtils.remove(str, (char) 12288);
            }
            str = StringUtils.remove(str, "\r");
            str = StringUtils.remove(str, "\n");
            str = StringUtils.remove(str, "\t");
            str = StringUtils.remove(str, "\\s*");
            str = StringUtils.remove(str, "◆");
            str = StringUtil.cleanField(str);
            str = str.trim();
        }
        return str;
    }

    /**
     * 清除无关的Node节点元素
     * @param str
     * @return
     */
    protected void cleanUnusedNodes(Node doc) {
        cleanUnusedNodes(doc, "//STYLE");
        cleanUnusedNodes(doc, "//MAP");
        cleanUnusedNodes(doc, "//SCRIPT");
        cleanUnusedNodes(doc, "//script");
    }

    /**
     * 清除无关的Node节点元素
     * @param str
     * @return
     */
    protected void cleanUnusedNodes(Node node, String xpath) {
        try {
            NodeList nodes = XPathAPI.selectNodeList(node, xpath);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                element.getParentNode().removeChild(element);
            }
        } catch (DOMException e) {
            throw new IllegalStateException(e);
        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Parse filter(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) {
        LOG.debug("Invoking parse  {} for url: {}", this.getClass().getName(), url);
        try {
            //URL匹配
            if (!isUrlMatchedForParse(url)) {
                LOG.debug("Skipped {} as not match regex [{}]", this.getClass().getName(), getUrlFilterRegex());
                return parse;
            }

            if (page.getContent() == null) {
                LOG.warn("Empty content for url: {}", url);
                return parse;
            }

            //检测内容是否业务关注页面
            String html = asString(doc);
            if (!isContentMatchedForParse(url, html)) {
                LOG.debug("Skipped as content not match excepted");
                return parse;
            }

            //清除无关的Node节点元素
            cleanUnusedNodes(doc);

            pages.incrementAndGet();
            parse = filterInternal(url, page, parse, metaTags, doc);

            if (LOG.isInfoEnabled()) {
                long elapsed = (System.currentTimeMillis() - start) / 1000;
                float avgPagesSec = (float) pages.get() / elapsed;
                LOG.info(" - Custom prased total " + pages.get() + " pages, " + elapsed + " seconds, avg " + avgPagesSec + " pages/s");
            }
            return parse;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 初始化属性记录表的DDL脚本（MySQL版本）：

    CREATE TABLE `crawl_data` (
    `url` varchar(255) NOT NULL,
    `code` varchar(255) NOT NULL,
    `name` varchar(255) DEFAULT NULL,
    `category` varchar(255) DEFAULT NULL,
    `order_index` int(255) DEFAULT NULL,
    `fetch_time` datetime NOT NULL,
    `text_value` text,
    `html_value` text,
    `date_value` datetime DEFAULT NULL,
    `num_value` decimal(18,2) DEFAULT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

     * 按照代码纵向存储各属性值，在需要的时候再转成横向，参考SQL脚本：
      SELECT url,fetch_time, 
      GROUP_CONCAT(CASE WHEN  code = 'title'  THEN  text_value ELSE  null  END)   AS  `title` 
      GROUP_CONCAT(CASE WHEN  code = 'price'  THEN  num_value ELSE  null  END)   AS  `价格`,
      FROM crawl_data GROUP BY url,fetch_time
     */
    private static final String selectSQL = "SELECT count(*) from crawl_data where url=?";
    private static final String deleteSQL = "DELETE from crawl_data where url=?";
    private static final String insertSQL = "INSERT INTO crawl_data(url, code, name, category, order_index, fetch_time, text_value, html_value,date_value, num_value) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?)";

    /**
     * 属性持久化处理，基于nutch-site.xml中parse.data.persist.mode定义值
     * @param url
     * @param crawlDatas
     * @param page
     */
    protected void saveCrawlData(String url, List<CrawlData> crawlDatas, WebPage page) {

        String persistMode = conf.get("parse.data.persist.mode");
        if (StringUtils.isBlank(persistMode) || "println".equalsIgnoreCase(persistMode)) {
            System.out.println("Parsed data properties:");
            for (CrawlData crawlData : crawlDatas) {
                System.out.println(" - " + crawlData.getCode() + " : " + crawlData.getDisplayValue());
            }
            return;
        }

        //img fetch and persist
        for (CrawlData crawlData : crawlDatas) {
            if (ValueType.img.equals(crawlData.getType()) && StringUtils.isNotBlank(crawlData.getTextValue())) {
                if (imgSaveRootDir == null) {
                    imgSaveRootDir = getConf().get("parse.img.save.dir");
                    Assert.isTrue(StringUtils.isNoneBlank(imgSaveRootDir), "'parse.img.save.dir' conf parameter missing");
                    if (!imgSaveRootDir.endsWith("/")) {
                        imgSaveRootDir += "/";
                    }
                }
                try {
                    String imgSrc = crawlData.getTextValue();
                    Page imgPage = HttpWebClient.getPage(imgSrc, getConf());
                    InputStream is = imgPage.getWebResponse().getContentAsStream();
                    File file = new File(imgSaveRootDir + StringUtils.substringAfter(imgSrc, "//"));
                    FileUtils.copyInputStreamToFile(is, file);
                } catch (IOException e) {
                    crawlData.setImgValue(null, page);
                    LOG.error("Error to img process", e);
                }
            }
        }

        if ("jdbc".equalsIgnoreCase(persistMode)) {
            Connection conn = null;
            try {
                Class.forName(conf.get("jdbc.driver"));
                conn = DriverManager.getConnection(conf.get("jdbc.url"), conf.get("jdbc.username"), conf.get("jdbc.password"));
                PreparedStatement selectPS = conn.prepareStatement(selectSQL);
                selectPS.setString(1, url);
                ResultSet rs = selectPS.executeQuery();
                if (rs.next()) {
                    int cnt = rs.getInt(1);
                    rs.close();
                    selectPS.close();
                    if (cnt > 0) {
                        LOG.debug("Cleaning exists properties for url: {}", url);
                        PreparedStatement deletePS = conn.prepareStatement(deleteSQL);
                        deletePS.setString(1, url);
                        deletePS.execute();
                        deletePS.close();
                    }
                    LOG.debug("Saving properties for url: {}", url);
                    PreparedStatement insertPS = conn.prepareStatement(insertSQL);
                    int idx = 10;
                    for (CrawlData crawlData : crawlDatas) {
                        if (!crawlData.getUrl().equals(url)) {
                            LOG.error("Invalid crawlData not match url: {}", url);
                            continue;
                        }
                        LOG.debug(" - {} : {}", crawlData.getKey(), crawlData.getDisplayValue());
                        insertPS.setString(1, crawlData.getUrl());
                        insertPS.setString(2, crawlData.getCode());
                        insertPS.setString(3, crawlData.getName());
                        insertPS.setString(4, crawlData.getCategory());
                        if (crawlData.getOrderIndex() != null) {
                            insertPS.setInt(5, crawlData.getOrderIndex());
                        } else {
                            insertPS.setInt(5, idx);
                        }
                        insertPS.setTimestamp(6, new java.sql.Timestamp(new Date().getTime()));
                        insertPS.setString(7, crawlData.getTextValue());
                        insertPS.setString(8, crawlData.getHtmlValue());
                        if (crawlData.getDateValue() != null) {
                            insertPS.setTimestamp(9, new java.sql.Timestamp(crawlData.getDateValue().getTime()));
                        } else {
                            insertPS.setTimestamp(9, null);
                        }
                        if (crawlData.getNumValue() != null) {
                            insertPS.setBigDecimal(10, crawlData.getNumValue());
                        } else {
                            insertPS.setBigDecimal(10, null);
                        }
                        insertPS.addBatch();
                        idx += 10;
                    }
                    insertPS.executeBatch();
                    insertPS.close();
                }
            } catch (Exception e) {
                LOG.error("Error to get jdbc operation", e);
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception e) {
                    LOG.error("Error to close jdbc connection", e);
                }
            }
            return;
        }

        if ("mongodb".equalsIgnoreCase(persistMode)) {
            try {
                MongoClient mongoClient = new MongoClient(conf.get("mongodb.host"), Integer.valueOf(conf.get("mongodb.port")));
                DB db = mongoClient.getDB(conf.get("mongodb.db"));
                DBCollection coll = db.getCollection("crawl_data");
                BasicDBObject bo = new BasicDBObject("url", url).append("fetch_time", new Date());
                LOG.debug("Saving properties for url: {}", url);
                for (CrawlData crawlData : crawlDatas) {
                    if (!crawlData.getUrl().equals(url)) {
                        LOG.error("Invalid crawlData not match url: {}", url);
                        continue;
                    }
                    bo.append(crawlData.getKey(), crawlData.getValue());
                }
                coll.update(new BasicDBObject("url", url), bo, true, false);
                mongoClient.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Collection<Field> getFields() {
        return null;
    }

    // 获取解析过滤器集合，用于过滤链回调判断页面加载完成
    private static AbstractHtmlParseFilter[] parseFilters;
    public static final String HTMLPARSEFILTER_ORDER = "htmlparsefilter.order";

    /**
     * 帮助类方法：获取当前所有配置的自定义过滤器集合
     * @param conf
     * @return
     */
    public static AbstractHtmlParseFilter[] getParseFilters(Configuration conf) {
        //Prepare  parseFilters
        String order = conf.get(HTMLPARSEFILTER_ORDER);
        if (parseFilters == null) {
            /*
             * If ordered filters are required, prepare array of filters based on
             * property
             */
            String[] orderedFilters = null;
            if (order != null && !order.trim().equals("")) {
                orderedFilters = order.split("\\s+");
            }
            HashMap<String, AbstractHtmlParseFilter> filterMap = new HashMap<String, AbstractHtmlParseFilter>();
            try {
                ExtensionPoint point = PluginRepository.get(conf).getExtensionPoint(ParseFilter.X_POINT_ID);
                if (point == null)
                    throw new RuntimeException(ParseFilter.X_POINT_ID + " not found.");
                Extension[] extensions = point.getExtensions();
                for (int i = 0; i < extensions.length; i++) {
                    Extension extension = extensions[i];
                    ParseFilter parseFilter = (ParseFilter) extension.getExtensionInstance();
                    if (parseFilter instanceof AbstractHtmlParseFilter) {
                        if (!filterMap.containsKey(parseFilter.getClass().getName())) {
                            filterMap.put(parseFilter.getClass().getName(), (AbstractHtmlParseFilter) parseFilter);
                        }
                    }
                }
                parseFilters = filterMap.values().toArray(new AbstractHtmlParseFilter[filterMap.size()]);
                if (orderedFilters != null) {
                    ArrayList<ParseFilter> filters = new ArrayList<ParseFilter>();
                    for (int i = 0; i < orderedFilters.length; i++) {
                        ParseFilter filter = filterMap.get(orderedFilters[i]);
                        if (filter != null) {
                            filters.add(filter);
                        }
                    }
                    parseFilters = filters.toArray(new AbstractHtmlParseFilter[filters.size()]);
                }
            } catch (PluginRuntimeException e) {
                throw new RuntimeException(e);
            }
        }
        return parseFilters;
    }

    /**
     * 判断url是否符合自定义解析匹配规则
     * @param url
     * @return
     */
    public boolean isUrlMatchedForParse(String url) {
        if (filterPattern == null) {
            //没有url控制规则，直接放行
            return true;
        }
        if (filterPattern.matcher(url).find()) {
            return true;
        }
        return false;
    }

    /**
     * 检测url获取页面内容是否已加载完毕，主要用于支持一些AJAX页面延迟等待加载
     * 返回false则表示告知Fetcher处理程序继续AJAX执行短暂等待后再回调此方法直到返回true标识内容已加载完毕
     * @param fetchUrl
     * @param html 页面HTML
     * @return 默认返回true，子类根据需要定制判断逻辑
     */
    public boolean isParseDataFetchLoaded(String url, String html) {
        if (filterPattern == null) {
            //没有url控制规则，直接放行
            return true;
        }
        //首先判断url是否匹配当前过滤器，如果是则继续调用内容判断逻辑
        if (filterPattern.matcher(url).find()) {
            if (StringUtils.isBlank(html)) {
                return false;
            }
            return isParseDataFetchLoadedInternal(url, html);
        }
        return true;
    }

    /**
     * 目前已知类似：http://www.jumeiglobal.com/deal/ht150312p1286156t1.html
     * 所需采集数据在textarea元素下面，htmlunit在printXml时会对textarea内容进行escape处理，导致得到的doc对象无法直接XPath定位
     * 因此需要先提前textarea元素内容转换为单独的DocumentFragment对象，然后基于此文档对象进行数据解析
     * @see org.apache.nutch.parse.html.HtmlParser#parse
     * @param input
     * @return 
     * @throws Exception
     */
    protected DocumentFragment parse(InputSource input) throws Exception {
        if (parserImpl.equalsIgnoreCase("tagsoup"))
            return parseTagSoup(input);
        else
            return parseNeko(input);
    }

    /**
     * @see org.apache.nutch.parse.html.HtmlParser#parseTagSoup
     */
    private DocumentFragment parseTagSoup(InputSource input) throws Exception {
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment();
        DOMBuilder builder = new DOMBuilder(doc, frag);
        org.ccil.cowan.tagsoup.Parser reader = new org.ccil.cowan.tagsoup.Parser();
        reader.setContentHandler(builder);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.ignoreBogonsFeature, true);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.bogonsEmptyFeature, false);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
        reader.parse(input);
        return frag;
    }

    /**
     * @see org.apache.nutch.parse.html.HtmlParser#parseNeko
     */
    private DocumentFragment parseNeko(InputSource input) throws Exception {
        DOMFragmentParser parser = new DOMFragmentParser();
        try {
            parser.setFeature("http://cyberneko.org/html/features/scanner/allow-selfclosing-iframe", true);
            parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", defaultCharEncoding);
            parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);
            parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
            parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
            parser.setFeature("http://cyberneko.org/html/features/report-errors", LOG.isTraceEnabled());
        } catch (SAXException e) {
        }
        // convert Document to DocumentFragment
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        doc.setErrorChecking(false);
        DocumentFragment res = doc.createDocumentFragment();
        DocumentFragment frag = doc.createDocumentFragment();
        parser.parse(input, frag);
        res.appendChild(frag);

        try {
            while (true) {
                frag = doc.createDocumentFragment();
                parser.parse(input, frag);
                if (!frag.hasChildNodes())
                    break;
                if (LOG.isInfoEnabled()) {
                    LOG.info(" - new frag, " + frag.getChildNodes().getLength() + " nodes.");
                }
                res.appendChild(frag);
            }
        } catch (Exception x) {
            LOG.error("Failed with the following Exception: ", x);
        }
        ;
        return res;
    }

    /**
     * 设置当前解析过滤器匹配的URL正则表达式
     * 只有匹配的url才调用当前解析处理逻辑
     * @return
     */
    protected abstract String getUrlFilterRegex();

    /**
     * 检测url获取页面内容是否已加载完毕，主要用于支持一些AJAX页面延迟等待加载
     * 返回false则表示告知Fetcher处理程序继续AJAX执行短暂等待后再回调此方法直到返回true标识内容已加载完毕
     * @param html 页面HTML
     * @return 默认返回true，子类根据需要定制判断逻辑
     */
    protected abstract boolean isParseDataFetchLoadedInternal(String url, String html);

    /**
     * 判断当前页面内容是否业务关注的页面
     * @param url
     * @param html
     * @return
     */
    protected abstract boolean isContentMatchedForParse(String url, String html);

    /**
     * 子类实现具体的页面数据解析逻辑
     * @return
     */
    public abstract Parse filterInternal(String url, WebPage page, Parse parse, HTMLMetaTags metaTags, DocumentFragment doc) throws Exception;
}
