# 1. 项目说明
## 1.1 环境
RDFox项目对于不同的OS，环境是不一样的，最主要的区别在于linux下lib文件夹下是libRDFox.so，而windows环境下是libRDFox.dll

## 1.2 使用说明
有几个文件需要注意，data文件夹下放置.nt文件和对应的规则文件（如rules.txt）。

项目主文件是NewRDFox.java

# 2. linux下编译运行说明
## 2.1 测试
在windows下测试没有问题之后，上传至服务器

## 2.2编译
在项目根目录下，运行

javac -d bin -sourcepath examples/Java -cp lib/JRDFox.jar examples/Java/tech/oxfordsemantic/jrdfox/NewRDFox.java

## 2.3 运行
在编译通过后，将.nt文件和规则文件放置于bin/tech/oxfordsemantic/jrdfox/data/目录下

运行

java -cp bin:lib/JRDFox.jar tech.oxfordsemantic.jrdfox.NewRDFox

# 3.版权
&copy; copyright:牛津大学

网址：https://docs.oxfordsemantic.tech