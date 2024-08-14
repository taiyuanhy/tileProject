# tileProject
该工程主要服务于瓦片地图生产和部署流程 ，主要功能有两个
#### 1.将下载好的原始地图瓦片（散图）压缩成bundle格式的文件，便于传输
#### 2.将压缩后的bundle格式的文件发布成ZXY格式的瓦片服务，可以让前端gis引擎进行加载
###  📖 如何使用：
 1 com.uinv.gis.tileProject.GUI是一个图像界面，运行后可以在图像界面下选择散图存放路径，然后点击开始转换，
 会在散图路径下创建一bundle目录，将转换后的bundle文件按层级存放。

 2 com.uinv.gis.tileProject.GeoTileController 是对外暴露的接口，通过/getTile/{z}/{x}/{y}的方式将bundle目录发布成服务。

 3 工程可以打包成两个jar包，一个用于散图压缩的jar包，双击可运行，一个用于发布服务，通过：
```
java -jar tileProjectServer.jar F:\\tiles\\bundle
```
将bundle目录发布成瓦片服务(F:\\tiles\\bundle 代表转换后的bundle分级文件存放的目录)

### 📦 打包
用于散图压缩的jar包，可通过Elicpse或者Idea工具，指定程序乳沟为GUI.java，输出一个jar包，可双击运行。
用于发布服务的jar包，直接通过mvn package -dSkipTests 进行打包

### 🤝 贡献者 
huyang@uino.com