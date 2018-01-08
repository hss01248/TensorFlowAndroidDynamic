# 主要参考
[官方集成指南](https://www.tensorflow.org/mobile/android_build)

[官方demo](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android)

[深度学习利器：TensorFlow在智能终端中的应用](https://juejin.im/post/5a1e2f89f265da432b4a9328)

[机器学习:在Android中集成TensorFlow ](http://blog.csdn.net/leon8/article/details/77455633)

[ 动态加载so库的实现方法与问题处理](http://blog.csdn.net/aqi00/article/details/72763742)



# 集成简述
> 集成的代码是在[AndroidTensorFlowMachineLearningExample](https://github.com/MindorksOpenSource/AndroidTensorFlowMachineLearningExample)基础上修改.

Android上集成tensorflow最简单是步骤是:

1 complile引入
```
compile 'org.tensorflow:tensorflow-android:1.4.0'
```

2 拿到AI算法开发人员训练好并压缩好的pb文件

3 根据其java的api开始写代码

一般so库通过compile引入后自动会打入,pb放在asserts文件夹,但这样会增大apk包体积.
so库一般一个abi就10-15M,pb文件20-100M不等.
所以需要动态加载: 放在服务器,下载到本地,需要时直接从本地读取.

# pb文件的生成

参考: [Preparing models for mobile deployment](https://www.tensorflow.org/mobile/prepare_models)

## pb文件是什么

save out graphs ,serialized as protocol buffers



# pb文件的加载
AndroidTensorFlowMachineLearningExample中api并无从流中加载pb文件的api,而1.4.0中java api提供了以下构造方法,可以从文件中加载. 只要预先下载好就行了.
```
new TensorFlowInferenceInterface(inputStream);
```

# so库的动态加载
略显麻烦,
一需要识别所支持的abi,然后下载对应的so文件
二 需要注释掉java代码中的静态代码块加载so库的代码

> 依赖引入方式不再是上方的一行代码compile,而是将gradle缓存中的tensorflow-android:1.4.0的jar包和jni包拷贝出来.
> jni包中的so文件上传到服务器.
## 注释静态方法中的加载so库的代码:
jar包需要注释掉里面静态代码块加载so库的代码:
jar包中多处调用TensorFlow.init()初始化,所以只要注释init内部内容就好.
```
TensorFlow类中的: 
static void init() {
    //NativeLibrary.load();//注释掉此行,由我们自己动态加载
  }

  static {
    init();
  }
```

>操作方法: 


新建一个包名相同的TensorFlow类,将jar包中代码拷贝至此,注释掉那一行代码,用java7(不能用java8)编译后,
将jar包用winrar打开,将编译后的class文件拖进jar包替换即可.
然后将jar包添加到工程libs目录,添加为依赖.

## so库动态加载

获取手机系统首选abi,然后去获取对应url,下载到app内目录,加载即可.

### 获取首选abi:

```
private static String getFirstSupportedAbi() {
        String abi1 = "";
        if (Build.VERSION.SDK_INT >= 21) {
            String[] abis = Build.SUPPORTED_ABIS;
            if (abis != null) {
                String abistr  = "";
                //第一个是原生支持的,后面的是兼容模式.虽然是兼容,但手动加载时很多并不兼容.
                abi1 = abis[0];
                for (String abi : abis) {
                    abistr = abistr + abi+",";
                }
                if(showLog)
                Log.e(TAG, "[copySo] supported api:" + abistr);
            }
        } else {
            if(showLog)
            Log.e(TAG, "[copySo] supported api:" + Build.CPU_ABI + "--- " + Build.CPU_ABI2);
            if (!TextUtils.isEmpty(Build.CPU_ABI)) {
                abi1 = Build.CPU_ABI;
            } else if (!TextUtils.isEmpty(Build.CPU_ABI2)) {
                abi1 = Build.CPU_ABI2;
            }
        }
        return abi1;
    }

```

### 下载后的so文件要放到app内目录,不要放到sd卡:

```
String abi = getFirstSupportedAbi();
File dir = context.getDir("jnilibs", Context.MODE_PRIVATE);
File subDir = new File(dir, abi);
```

### 加载so:

```
            try {
                    System.load(filePath);
                }catch (Throwable e){
                    e.printStackTrace();
                }

```

# 文件下载
都是大文件,选一个比较靠谱,能够断点续传的库:[FileDownloader](https://github.com/lingochamp/FileDownloader)

同时还需要注意:
* 文件很大,只在wifi时下载,以免耗流量
* 下载完校验md5.

# TensorFlowInferenceInterface需要的参数

参考demo:

[将tensorflow训练好的模型移植到android上](http://blog.csdn.net/cxq234843654/article/details/71171293)

```
private static final String MODEL_FILE = "file:///android_asset/cxq.pb"; //模型存放路径

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

## 接口使用的三步走:

```
inferenceInterface.feed(...)

inferenceInterface.run(outputNames, logStats);

 inferenceInterface.fetch(outputNames[0], outputLocationsEncoding);
```

## 输入输出参数:

见 [pb文件说明.md]()



# 代码

[TensorFlowAndroidDynamic](https://github.com/hss01248/TensorFlowAndroidDynamic)

