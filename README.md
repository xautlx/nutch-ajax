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

* **protocol-s2jh**: 基于Htmlunit和Selenium WebDriver实现的AJAX页面Fetcher插件

* **parse-s2jh**: 基于XPath解析页面元素内容; 持久化解析到的结构化数据，如MySQL，MongoDB等; 对于个别复杂类型AJAX页面定制判断页面加载完成的回调判断逻辑

* **index-s2jh**: 追加设置需要额外传递给SOLR索引的属性数据; 设定不需要索引的页面规则;

### 许可说明

* 开源协议

本项目所有代码完整开源，在保留标识本项目来源信息以及保证不对本项目进行非授权的销售行为的前提下，可以以任意方式自由使用：开源、非开源、商业及非商业。

* 收费服务

如果你希望提供基于Apache Nutch/Solr/Lucene等系列技术的定制的扩展实现/技术咨询服务/毕业设计指导/二次开发项目指导等任何有兴趣的合作形式，可以联系 E-Mail: xautlx@hotmail.com 或 QQ: 2414521719 (加Q请注明：nutch/solr/lucene) 协商收费服务。[上述联系方式恕不直接提供免费的技术咨询类询问，若对项目有任何技术问题或Issue反馈，请直接提交到项目站点提问或Git平台的Issue]
 
