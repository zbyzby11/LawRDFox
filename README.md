# 1. 项目说明
## 1.1 环境
RDFox项目对于不同的OS，环境是不一样的，最主要的区别在于linux下lib文件夹下是libRDFox.so，而windows环境下是libRDFox.dll

## 1.2 使用说明
licence文件(RDFox.lic)在windows环境下，将这个文件放置入C:\Users\ZBY\AppData\Local\RDFox\文件夹下

在linux环境下，放置入~/.RDFox/文件夹下

有几个文件需要注意，data文件夹下放置.nt文件（包括本体文件）和对应的规则文件（如rules.txt）。

项目主文件有两个文件组成，一个是NewRDFox_dupin.java，一个是NewRDFoxWithOnto.java。
前者对应不加入本体的推理引擎，后者对应加入本体文件的推理引擎。

# 2. windows以及linux下编译运行说明
## 2.1 测试
可以在本地IDE中进行测试，测试没有问题则可以切换环境。

## 2.2编译
在项目根目录下，运行

linux和windows下均为以下命令（不带有本体文件推理）：
```
javac -d bin -sourcepath examples/Java -cp lib/JRDFox.jar examples/Java/tech/oxfordsemantic/jrdfox/NewRDFox_dupin.java
```

或者是带有本体文件推理：
```
javac -d bin -sourcepath examples/Java -cp lib/JRDFox.jar examples/Java/tech/oxfordsemantic/jrdfox/NewRDFoxWithOnto.java
```

## 2.3 运行
在编译通过后，将.nt文件（或者包括本体文件）和规则文件放置于bin/tech/oxfordsemantic/jrdfox/data/目录下

运行

```
java -cp bin;lib/JRDFox.jar tech.oxfordsemantic.jrdfox.NewRDFox_dupin(windows下带有本体文件)
java -cp bin;lib/JRDFox.jar tech.oxfordsemantic.jrdfox.NewRDFoxWithOnto(windows下不带有本体文件)

java -cp bin:lib/JRDFox.jar tech.oxfordsemantic.jrdfox.NewRDFox_dupin(linux下带有本体文件)
java -cp bin:lib/JRDFox.jar tech.oxfordsemantic.jrdfox.NewRDFoxWithOnto(linux下不带有本体文件)
```

# 3.版权
&copy; copyright:牛津大学

网址：https://docs.oxfordsemantic.tech