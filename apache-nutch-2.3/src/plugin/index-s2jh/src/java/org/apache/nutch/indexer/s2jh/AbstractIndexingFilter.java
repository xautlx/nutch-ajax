package org.apache.nutch.indexer.s2jh;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.s2jh.AbstractHtmlParseFilter;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public abstract class AbstractIndexingFilter implements IndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractIndexingFilter.class);

    private Configuration conf;

    // 从MetaData数据中提取标识需要添加到Solr Index的前缀
    // 为了避免对parse-s2jh组件的依赖，在各自组件中定义常量，请注意和CrawlData对应常量值保持一致
    protected final static String INDEX_PROPERTY_PREFIX = "_index_";

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public NutchDocument filter(NutchDocument doc, String url, WebPage page) throws IndexingException {
        LOG.debug("Invoking  indexer {} for url: {}", this.getClass().getName(), url);

        if (doc == null) {
            LOG.debug("Skipped index as NutchDocument doc is null");
            return doc;
        }

        if (!isMatchedForParseIndex(url)) {
            LOG.debug("Skipped index as URL NOT match any htmlParseFilter.");
            return null;
        }

        //默认用的是reverseUrl，转换为直接用原始url,便于进行关联比对
        doc.removeField("id");
        doc.add("id", url);

        return filterInternal(doc, url, page);
    }

    /**
     * 调用解析过滤器中判断只有被自定义解析的url地址内容才添加到solr索引，其余的忽略
     * 
     * @param url
     * @return
     */
    private boolean isMatchedForParseIndex(String url) {
        boolean ok = false;
        AbstractHtmlParseFilter[] parseFilters = AbstractHtmlParseFilter.getParseFilters(conf);
        if (parseFilters != null) {
            for (AbstractHtmlParseFilter htmlParseFilter : parseFilters) {
                Boolean ret = htmlParseFilter.isUrlMatchedForParse(url);
                // 任何一个判断返回true表示为需要解析并进行索引的数据
                if (ret == true) {
                    ok = true;
                    break;
                }
            }
        }
        return ok;
    }

    private static final Collection<WebPage.Field> FIELDS = new HashSet<WebPage.Field>();
    static {
        FIELDS.add(WebPage.Field.METADATA);
    }

    /**
     * Gets all the fields for a given {@link WebPage} Many datastores need to
     * setup the mapreduce job by specifying the fields needed. All extensions
     * that work on WebPage are able to specify what fields they need.
     */
    @Override
    public Collection<WebPage.Field> getFields() {
        return FIELDS;
    }

    protected String getValueFromPageMetaData(WebPage page, String key) {
        ByteBuffer bytes = page.getMetadata().get(new Utf8(key));
        if (bytes == null) {
            return null;
        }
        return Bytes.toString(bytes);
    }

    public abstract NutchDocument filterInternal(NutchDocument doc, String url, WebPage page);
}
