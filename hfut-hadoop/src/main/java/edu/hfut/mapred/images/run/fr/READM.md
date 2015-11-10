

## 使用步骤

### 第一步：将样本库数据转换成序列化文件

```
bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar facesSequenceWrite -D facesdb=hdfaces -D facesdbseq=hdfaces_seq
```

### 第二步：运行人脸识别分布式代码

```
bin/hadoop jar hfut-hadoop-jar-with-dependencies.jar faceRecognitionDistribution -D input=test_faces -D output=rec_result -D facesdbseq=hdfaces_seq
```