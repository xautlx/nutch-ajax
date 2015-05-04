package org.apache.nutch.parse.s2jh;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.util.Utf8;
import org.apache.commons.lang3.StringUtils;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * 
 * @author EMAIL:s2jh-dev@hotmail.com , QQ:2414521719
 *
 */
public class CrawlData implements Serializable {

	private static final long serialVersionUID = -2670885640064330298L;

	public static final Logger LOG = LoggerFactory.getLogger(CrawlData.class);

	public final static DateFormat DEFAULT_TIME_FORMATER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	//添加固定前缀标示属性需要添加到Solr Index属性中
	private final static String INDEX_PROPERTY_PREFIX = "_index_";

	private String url;
	private String code;
	private ValueType type;
	private String name;
	private String category;
	private Integer orderIndex;
	private String textValue;
	private String htmlValue;
	private BigDecimal numValue;
	private Date dateValue;

	public static enum ValueType {
		text, html, num, date, img
	}

	public CrawlData(String url, String code) {
		super();
		this.url = url;
		this.code = code;
	}

	public CrawlData(String url, String code, String name) {
		this(url, code);
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

	public CrawlData setTextValue(String textValue, WebPage page) {
		Assert.isNull(type, "一条记录只能存放一种类型数据：type=" + type);
		this.type = ValueType.text;
		this.textValue = textValue;
		injectDataToPageMetadata(code,
				textValue == null ? null : textValue.toString(), page);
		return this;
	}

	public CrawlData setImgValue(String imgSrc) {
		return setTextValue(imgSrc, null);
	}

	public CrawlData setImgValue(String imgSrc, WebPage page) {
		Assert.isNull(type, "一条记录只能存放一种类型数据：type=" + type);
		this.type = ValueType.img;
		this.textValue = imgSrc;
		injectDataToPageMetadata(code,
				textValue == null ? null : textValue.toString(), page);
		return this;
	}

	public CrawlData setTextValue(String textValue) {
		return setTextValue(textValue, null);
	}

	public String getHtmlValue() {
		return htmlValue;
	}

	public CrawlData setHtmlValue(String htmlValue) {
		return setHtmlValue(htmlValue, null);
	}

	public CrawlData setHtmlValue(String htmlValue, WebPage page) {
		Assert.isNull(type, "一条记录只能存放一种类型数据：type=" + type);
		this.type = ValueType.html;
		this.htmlValue = htmlValue;
		injectDataToPageMetadata(code,
				htmlValue == null ? null : htmlValue.toString(), page);
		return this;
	}

	public BigDecimal getNumValue() {
		return numValue;
	}

	public CrawlData setNumValue(String numValue, WebPage page) {
		Assert.isNull(type, "一条记录只能存放一种类型数据：type=" + type);
		this.type = ValueType.num;
		try {
			this.numValue = StringUtils.isBlank(numValue) ? null
					: new BigDecimal(numValue);
		} catch (Exception e) {
			LOG.warn("Parse String to BigDecimal error, num value is "
					+ numValue);
		}
		injectDataToPageMetadata(code, numValue, page);
		return this;
	}

	public CrawlData setNumValue(String numValue) {
		return setNumValue(numValue, null);
	}

	public Date getDateValue() {
		return dateValue;
	}

	public CrawlData setDateValue(Date dateValue, WebPage page) {
		Assert.isNull(type, "一条记录只能存放一种类型数据：type=" + type);
		this.type = ValueType.date;
		this.dateValue = dateValue;
		injectDataToPageMetadata(code, dateValue == null ? null
				: DEFAULT_TIME_FORMATER.format(dateValue), page);
		return this;
	}

	public CrawlData setDateValue(Date dateValue) {
		return setDateValue(dateValue, null);
	}

	private void injectDataToPageMetadata(String code, String value,
			WebPage page) {
		if (page != null) {
			// 添加固定前缀标示属性需要添加到Solr Index属性中
			String metadataCode = INDEX_PROPERTY_PREFIX + code;
			if (value == null) {
				page.getMetadata().remove(new Utf8(metadataCode));
			} else {
				page.getMetadata().put(new Utf8(metadataCode),
						ByteBuffer.wrap(Bytes.toBytes(value)));
			}
		}
	}

	public String getDisplayValue() {
		Object value = getValue();
		if (value == null) {
			return "NULL";
		}
		if (value instanceof Date) {
			return DEFAULT_TIME_FORMATER.format((Date) value);
		}
		String strValue = value.toString();
		if (strValue.length() > 200) {
			return strValue.substring(0, 200);
		} else {
			return strValue;
		}
	}

	public Object getValue() {
		if (ValueType.img.equals(type)) {
			return textValue;
		} else if (ValueType.text.equals(type)) {
			return textValue;
		} else if (ValueType.html.equals(type)) {
			return htmlValue;
		} else if (ValueType.num.equals(type)) {
			return numValue;
		} else if (ValueType.date.equals(type)) {
			return dateValue;
		}
		return "Undefined";
	}

	public ValueType getType() {
		return type;
	}
}
