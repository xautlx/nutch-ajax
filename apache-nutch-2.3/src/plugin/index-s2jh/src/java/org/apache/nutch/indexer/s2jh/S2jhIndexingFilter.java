package org.apache.nutch.indexer.s2jh;

import java.util.Set;

import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.TableUtil;
import org.apache.solr.pinyin.ChineseToPinyinConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class S2jhIndexingFilter extends AbstractIndexingFilter {

    public static final Logger LOG = LoggerFactory.getLogger(S2jhIndexingFilter.class);

    @Override
    public NutchDocument filterInternal(NutchDocument doc, String url, WebPage page) {
        Set<String> pinyins = ChineseToPinyinConvertor.toPinyin(TableUtil.toString(page.getTitle()),
                ChineseToPinyinConvertor.MODE.capital);
        for (String pinyin : pinyins) {
            doc.add("pinyin", pinyin);
        }
        doc.add("productPrice", getValueFromPageMetaData(page, "price"));
        doc.add("productTitle", getValueFromPageMetaData(page, "title"));
        return doc;
    }
}
