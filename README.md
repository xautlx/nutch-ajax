Nutch AJAX page Fetch, Parse, Index Plugin
==============

### 项目简介

基于Apache Nutch 2.3和Htmlunit, Selenium WebDriver等组件扩展，实现对于AJAX加载类型页面的完整页面内容抓取，以及特定数据项的解析和索引。

According to the implementation of Apache Nutch 2.X, we can't get dynamic HTML information from fetch pages including AJAX requests as it will ignore all AJAX requests.

This plugin will use Htmlunit and Selenium WebDriver to fetch whole page content with necessary dynamic AJAX requests. 

It developed and tested with Apache Nutch 2.3, you can try it on other Nutch 2.X version or refactor the source codes as your design.

### 主要特性

* **常规的HTML页面抓取**: 对于常规的例如新闻类没有AJAX特性的页面可以直接用Nutch自带的protocol-http插件抓取。

* **常规的AJAX页面抓取**: 对于绝大部分诸如jQuery ajax加载的页面，可以直接用htmlunit扩展插件抓取。

* **特殊的AJAX请求页面抓取**: 诸如淘宝/天猫的页面采用了独特的Kissy Javascript组件，目前测试htmlunit无法正确解析，因此退而求其次采用效率低一些的Selenium WebDriver方式实现页面数据抓取。

* **基于页面滚动的AJAX请求页面抓取**: 诸如淘宝/天猫的商品详情页面会基于页面滚动发起商品描述信息的加载，通过Htmlunit或Selenium WebDriver扩展处理可以实现此类页面数据抓取。

### 运行方式

整个项目基于官方的Apache Nutch 2.3源码基础之上添加插件代码和配置，运行方式和官方指南保持一致，具体请参考：http://wiki.apache.org/nutch/

同时工程代码中提交了Eclipse的工程配置文件，可以直接import Eclipse中Run或Debug运行，Nutch工程以Ivy进行依赖管理，可采用ANT Build方式或建议在Eclipse IDE安装Apache Ivy IDE插件进行工程编译运行。

![snapshot](http://git.oschina.net/xautlx/nutch-ajax/raw/master/snapshot/eclipse-run.jpg)

![snapshot](http://git.oschina.net/xautlx/nutch-ajax/raw/master/snapshot/storage-data.jpg)

![snapshot](http://git.oschina.net/xautlx/nutch-ajax/raw/master/snapshot/parse-data.jpg)

### 扩展插件说明

* **lib-pinyin**: 用于parse或index插件转换中文到拼音提交solr；部署用于solr dataimporthandler组件进行拼音转换的transformer扩展插件

* **lib-htmlunit**: 基于Htmlunit的多线程处理，缓存控制，请求正则控制等特性扩展插件

* **protocol-s2jh**: 基于Htmlunit和Selenium WebDriver实现的AJAX页面Fetcher插件

* **parse-s2jh**: 基于XPath解析页面元素内容; 持久化解析到的结构化数据，如MySQL，MongoDB等; 对于个别复杂类型AJAX页面定制判断页面加载完成的回调判断逻辑

* **index-s2jh**: 追加设置需要额外传递给SOLR索引的属性数据; 设定不需要索引的页面规则;

### 详细参考文档

项目提供一份比较详细的“基于Nutch&Solr定向采集解析和索引搜索的整合技术指南文档”，可通过以下两种方式查看参考文档内容：

* 直接获取项目内容后，在document目录下根据自己熟悉的编辑器查看对应的md或html格式文档；
* GitHub直接解析md文件，并且能正确处理图片链接，因此可直接在线访问 https://github.com/xautlx/nutch-ajax/blob/master/document/Apache_Nutch_Solr_Solution_with_AJAX_support.md 

### 许可说明

* Free Open Source

本项目所有代码完整开源，在保留标识本项目来源信息以及保证不对本项目进行非授权的销售行为的前提下，可以以任意方式自由免费使用：开源、非开源、商业及非商业。

* Charge Support Service

如果你还有兴趣在Apache Nutch/Solr/Lucene等系列技术的定制的扩展实现/技术咨询服务/毕业设计指导/二次开发项目指导等方面的合作意向，可联系 E-Mail: s2jh-dev@hotmail.com 或 QQ: 2414521719 (加Q请注明：nutch/solr/lucene) 洽谈。[上述联系方式恕不直接提供咨询类询问，为了提升项目活跃度，若对项目有任何技术问题或Issue反馈，请直接提交到项目站点提问或Git平台的Issue]

### Reference

欢迎关注作者其他项目：

* [Nutch 2.X AJAX Plugins (Active)](https://github.com/xautlx/nutch-ajax) -  基于Apache Nutch 2.3和Htmlunit, Selenium WebDriver等组件扩展，实现对于AJAX加载类型页面的完整页面内容抓取，以及特定数据项的解析和索引

* [S2JH4Net (Active)](https://github.com/xautlx/s2jh4net) -  基于Spring MVC+Spring+JPA+Hibernate的面向互联网及企业Web应用开发框架

* [S2JH (Deprecated)](https://github.com/xautlx/s2jh) -  基于Struts2+Spring+JPA+Hibernate的面向企业Web应用开发框架
 
* [Nutch 1.X AJAX Plugins (Deprecated)](https://github.com/xautlx/nutch-htmlunit) -  基于Apache Nutch 1.X和Htmlunit的扩展实现AJAX页面爬虫抓取解析插件
 
* [12306 Hunter (Deprecated)](https://github.com/xautlx/12306-hunter) - （功能已失效不可用，不过还可以当作Swing开发样列参考只用）Java Swing C/S版本12306订票助手，用处你懂的