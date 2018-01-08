

# TensorFlowInferenceInterface需要的参数

参考: [将tensorflow训练好的模型移植到android上](http://blog.csdn.net/cxq234843654/article/details/71171293)

```
    //数据的维度
    private static final int HEIGHT = 1;
    private static final int WIDTH = 2;

    //模型中输出变量的名称
    private static final String inputName = "input";
    //用于存储的模型输入数据
    private float[] inputs = new float[HEIGHT * WIDTH];

    //模型中输出变量的名称
    private static final String outputName = "output";
    //用于存储模型的输出数据
    private float[] outputs = new float[HEIGHT * WIDTH];
```

## 数据的维度和格式

python代码:

```
 pitch = my_head_pose_estimator.return_pitch(frame[face_y1: face_y2, face_x1: face_x2])
```

将脸部区域的image截取后传入,也就是,传入的是一张图片.(opencv已截取好脸部区域)

根据tf的规定,图片的输入格式如下:

100张RGB三通道的16×32（高为16宽为32）彩色图:

TensorFlow，的表达形式是（100,16,32,3），即把通道维放在了最后，这种数据组织方式称为“channels_last”。

体现在java代码上就是:

```
  private int[] intValues;
  private byte[] byteValues;
 
 //拿到像素点的一维数组:intValues
 bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//再将每个像素点拆解为RGB三原色,组成新的一维数组
    for (int i = 0; i < intValues.length; ++i) {
      byteValues[i * 3 + 2] = (byte) (intValues[i] & 0xFF);
      byteValues[i * 3 + 1] = (byte) ((intValues[i] >> 8) & 0xFF);
      byteValues[i * 3 + 0] = (byte) ((intValues[i] >> 16) & 0xFF);
    }
 
    // Copy the input data into TensorFlow.
    inferenceInterface.feed(inputName, byteValues, 1, height, width, 3);
```

## 输出的数据类型和个数

查看代码:

```
# 核心代码:
             pitch_raw = self._sess.run([self.cnn_pitch_output], feed_dict=feed_dict)

             pitch_vector = np.multiply(pitch_raw, 45.0)
             #pitch = pitch_raw #* 40 #cnn out is in range [-1, +1] --> [-45, + 45]
```

由cnn输出的数据范围是[-1, +1],float类型,一维/一个

那么转为java代码应该是:

```
inferenceInterface.run(outputNames, logStats);

 final float[] output = new float[1];
 inferenceInterface.fetch(outputNames[0], output);
```



## inputName:

没有设置,为默认值: Placeholder

##  outputName

生成pb的代码:

        minimal_graph = convert_variables_to_constants(self.sess, self.sess.graph_def, ['Tanh_9'])
        tf.train.write_graph(minimal_graph, '.', 'minimal_graph.pb', as_text=False)

所以outputName =  Tanh_9
