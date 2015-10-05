
# 人脸识别分布式实现

> 基于Java实现。

### 项目内容

图像预处理、人脸检测、人脸识别和基于MapReduce实现。

### 项目构建

* *环境清空* :  `mvn clean`
* *单元测试* :  `mvn clean test`
* *功能/集成测试* :  `mvn clean verify -Pintegration`
* *GUI示例运行* :  `mvn exec:java`
* *打包项目*（不含源码） :  `mvn clean package -DskipTests`
* *安装项目*（含源码） :  `mvn clean install -DskipTests`

### 测试人脸库

> 本项目使用两类测试库：hdfaces和orlfaces，相关信息如下：

`hdfaces`：洪都测试库

`orlfaces`：ORL国外标准测试库[http://www.cl.cam.ac.uk/research/dtg/attarchive/facedatabase.html](http://www.cl.cam.ac.uk/research/dtg/attarchive/facedatabase.html)

### 开发人员

WeChat: wgybzb

QQ: 1010437118

E-mail: wgybzb@sina.cn
