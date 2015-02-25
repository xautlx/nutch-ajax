package org.apache.nutch.parse.s2jh;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * @author EMAIL:xautlx@hotmail.com , QQ:2414521719
 *
 */
public class CrawlData implements Serializable {

    private static final long serialVersionUID = -2670885640064330298L;

    private String url;
    private String code;
    private String name;
    private String category;
    private Integer orderIndex;
    private String textValue;
    private String htmlValue;
    private BigDecimal numValue;
    private Date dateValue;

    public CrawlData(String url, String code) {
        super();
        this.url = url;
        this.code = code;
    }

    public CrawlData(String url, String code, String name) {
        super();
        this.url = url;
        this.code = code;
        this.name = name;
    }

    public String getKey() {
        if (orderIndex != null) {
            return code + "_" + orderIndex;
        } else {
            return code;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getCode() {
        return code;
    }

    public CrawlData setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public CrawlData setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public CrawlData setCategory(String category) {
        this.category = category;
        return this;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public CrawlData setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
        return this;
    }

    public String getTextValue() {
        return textValue;
    }

    public CrawlData setTextValue(String textValue) {
        this.textValue = textValue;
        return this;
    }

    public String getHtmlValue() {
        return htmlValue;
    }

    public CrawlData setHtmlValue(String htmlValue) {
        this.htmlValue = htmlValue;
        return this;
    }

    public BigDecimal getNumValue() {
        return numValue;
    }

    public CrawlData setNumValue(BigDecimal numValue) {
        this.numValue = numValue;
        return this;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public CrawlData setDateValue(Date dateValue) {
        this.dateValue = dateValue;
        return this;
    }

    public String getDisplayValue() {
        if (textValue != null) {
            return textValue;
        }
        if (htmlValue != null) {
            if (htmlValue.length() > 200) {
                return htmlValue.substring(0, 200);
            } else {
                return htmlValue;
            }
        }
        if (numValue != null) {
            return numValue.toString();
        }
        if (dateValue != null) {
            return dateValue.toString();
        }
        return "Undefined";
    }

    public Map<String, Object> getMapValue() {
        Map<String, Object> data = Maps.newHashMap();
        if (textValue != null) {
            data.put("textValue", textValue);
        }
        if (htmlValue != null) {
            data.put("htmlValue", htmlValue);
        }
        if (numValue != null) {
            data.put("numValue", numValue);
        }
        if (dateValue != null) {
            data.put("dateValue", dateValue);
        }
        return data;
    }
}
