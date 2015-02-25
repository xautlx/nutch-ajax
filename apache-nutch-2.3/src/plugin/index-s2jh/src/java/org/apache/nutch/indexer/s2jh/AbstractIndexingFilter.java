package org.apache.nutch.indexer.s2jh;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIndexingFilter implements IndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractIndexingFilter.class);

    private Configuration conf;

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
            LOG.debug("Skipped as NutchDocument doc is null");
            return doc;
        }

        return filterInternal(doc, url, page);
    }

    protected String cancelIndexRegex() {
        return null;
    }

    /**
     * Gets all the fields for a given {@link WebPage} Many datastores need to
     * setup the mapreduce job by specifying the fields needed. All extensions
     * that work on WebPage are able to specify what fields they need.
     */
    @Override
    public Collection<WebPage.Field> getFields() {
        return null;
    }

    public abstract NutchDocument filterInternal(NutchDocument doc, String url, WebPage page);
}
