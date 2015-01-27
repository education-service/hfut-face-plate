
## 基于MapReduce的图像处理框架

> 提供在Hadoop上处理图像的功能。

主要包含：

* 图像基本数据类型封装
* 图像的输入和输出格式
* 图像处理的几个作业示例

### Hadoop安装配置

参考伪集群或者YARN全分布式官方部署文档进行部署。


### 基本软件安装


* Java 7 
* Maven 3

### 项目构建

```
    cd hfut-hadoop
    mvn package
```

### 测试示例


1. 将存放图片的文件夹拷贝到HDFS中

    $ hadoop fs -copyFromLocal local_image_folder hdfs_image_folder

2. 运行测试作业，将彩色图片灰度化

    $ hadoop jar mapred-images-1.0.0-jar-with-dependencies.jar bufferedImage2Gray hdfs_image_folder hdfs_output_folder

3. 将处理后的图片从HDFS中拷贝到本地

    $ hadoop fs -copyToLocal hdfs_output_folder local_output_folder

4. 检查处理后的图片是否处理正确

### 开发人员

WeChat: wgybzb

QQ: 1010437118

E-mail: wgybzb@sina.cn

