
# 中航工业洪都集团-智能中心合作项目

> 分布式车牌识别和人脸识别项目，基于Java实现。

## 项目内容

- [项目简介](项目简介)
- [项目架构](项目架构)
- [开发人员](开发人员)

----

## 项目简介

### 项目起因

基于Hadoop实现分布式车牌识别和人脸识别功能。

### 项目框架

`hfut-fr`: 分布式人脸识别工程

`hfut-lpr`: 分布式车牌识别工程

`hfut-hadoop`: 基于HDFS的图片数据读写，以及车牌和人脸识别的分布式运行框架


> **备注:** 框架持续更新中。

----

## 项目架构

1. edu.hfut.fr： 人脸图像相关处理。 
2. edu.hfut.lpr： 车牌图像相关处理。
3. edu.hfut.mapred： 用于提供分布式图像处理服务。

### 常见约束词
Item      | Value
--------- | -----
Dao  | Interface接口层
Domain    | 数据
Constant  | 常量
Util      |  工具

### 示例代码

```java

      Configuration conf = new Configuration();
		Job job = Job.getInstance(super.getConf(), "PlateRecognitionDistribution");

		job.setInputFormatClass(BufferedImageInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapperClass(PlateRecognitionMapper.class);
		job.setNumReduceTasks(0);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setJarByClass(PlateRecognitionDistribution.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
```

## 开发人员

WeChat: wgybzb

QQ: 1010437118

E-mail: wgybzb@sina.cn

