package org.apache.nutch.indexer.s2jh;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.storage.WebPage;
import org.apache.solr.pinyin.ChineseToPinyinConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class S2jhIndexingFilter extends AbstractIndexingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(S2jhIndexingFilter.class);

    @Override
    public NutchDocument filterInternal(NutchDocument doc, String url, WebPage page) {
        //取解析到的商品名称属性进行拼音转换添加到pinyin属性
        String name = getValueFromPageMetaData(page, INDEX_PROPERTY_PREFIX + "name");
        if (StringUtils.isNotBlank(name)) {
            Set<String> pinyins = ChineseToPinyinConvertor.toPinyin(name, ChineseToPinyinConvertor.MODE.capital);
            for (String pinyin : pinyins) {
                doc.add("pinyin", pinyin);
            }
        }

        if (page.getMetadata() != null) {
            for (CharSequence keyCS : page.getMetadata().keySet()) {
                String key = keyCS.toString();
                if (key.startsWith(INDEX_PROPERTY_PREFIX)) {
                    String indexKey = StringUtils.substringAfter(key, INDEX_PROPERTY_PREFIX);
                    String indexValue = getValueFromPageMetaData(page, key);
                    LOG.info("Adding index data to NutchDocument: {}={}", indexKey, indexValue);
                    doc.add(indexKey, indexValue);
                }
            }
        }
        return doc;
    }
}
