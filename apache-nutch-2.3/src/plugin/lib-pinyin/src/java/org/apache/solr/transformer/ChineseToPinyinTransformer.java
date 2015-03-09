package org.apache.solr.transformer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.DataImporter;
import org.apache.solr.handler.dataimport.Transformer;
import org.apache.solr.pinyin.ChineseToPinyinConvertor;
import org.apache.solr.pinyin.ChineseToPinyinConvertor.MODE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * solr dataimport transform extension for convert Chinese to Pinyin
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 */
public class ChineseToPinyinTransformer extends Transformer {

    protected final static Logger logger = LoggerFactory.getLogger(ChineseToPinyinTransformer.class);

    public Map<String, Object> transformRow(Map<String, Object> row, Context context) {
        List<Map<String, String>> fields = context.getAllEntityFields();

        for (Map<String, String> field : fields) {

            // Apply trim on this field
            String columnName = field.get(DataImporter.COLUMN);
            // Get this field's value from the current row
            Object value = row.get(columnName);

            if (value == null || String.valueOf(value).trim().length() == 0) {
                continue;
            }

            // Check if this field has pinyin column specified in the data-config.xml
            String pinyin = field.get("toPinyin");
            if (pinyin != null && pinyin.trim().length() > 0) {
                // Check if this field has mode specified in the data-config.xml
                MODE mode = MODE.both;
                if (MODE.capital.name().equalsIgnoreCase(field.get("mode"))) {
                    mode = MODE.capital;
                } else if (MODE.whole.name().equalsIgnoreCase(field.get("mode"))) {
                    mode = MODE.whole;
                }

                // Setup piyin filed in the current row
                String raw = String.valueOf(value);
                if (logger.isInfoEnabled()) {
                    logger.info("Parsing pinyin column[id=" + row.get("id") + "]: " + columnName + "->" + value);
                }

                Set<String> pinyins = new LinkedHashSet<String>();
                raw = raw.replaceAll("-", ",");
                String[] raws = raw.split(",");
                for (String item : raws) {
                    item = item.trim();
                    if (StringUtils.isBlank(item)) {
                        continue;
                    }
                    if (item.length() > 30) {
                        item = item.substring(0, 30);
                    }
                    pinyins.addAll(ChineseToPinyinConvertor.toPinyin(item, mode));
                }
                row.put(pinyin, pinyins);
            }

        }

        return row;
    }

}
