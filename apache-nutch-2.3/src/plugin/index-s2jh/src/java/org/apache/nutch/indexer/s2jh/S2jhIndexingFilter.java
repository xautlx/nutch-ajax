package org.apache.nutch.indexer.s2jh;

import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S2jhIndexingFilter extends AbstractIndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(S2jhIndexingFilter.class);

    @Override
    public NutchDocument filterInternal(NutchDocument doc, String url, WebPage page) {
        doc.add("biz", "123");

        return doc;
    }
}
