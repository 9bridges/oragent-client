# fzsclient

九桥数据同步工具 FZS 向第三方软件集成提供的接口
## 编译打包

```
mvn clean package
```

打完包后，jar 包在 target 目录下。
## 使用方法
### 前置条件
FZS 的增量抽取进程和 web 已经成功启动。
### client 调用方法
创建 FzsAgent 和 FzsClient 对象，分别启动两个模块，下面例子中展示如何打印出从 FZS 获取到的增量 DML 操作数据
```
import net.tbsoft.fzsclient.FzsAgent;
import net.tbsoft.fzsclient.FzsClient;
import net.tbsoft.fzsclient.agent.SimpleFzsAgent;
import net.tbsoft.fzsclient.client.entry.FzsDmlEntry;

public class Main {
    public static void main(String[] args) {
        int dataPort = 8303;
        int webPort = 8303;
        FzsAgent fzsAgent = SimpleFzsAgent.getInstance()
                .hostname("192.168.31.222") // database host
                .dbPort(1521) // database port
                .clientHost("192.168.31.206") //fzs client host
                .database("lhr11g") // database service name or sid
                .tableList("fzy.test,fzy.test2") // white table list
                .username("fzs1") // database user name
                .password("fzs1") // database password
                .dataPort(dataPort) // data port
                .webPort(webPort); // web port
        fzsAgent.start();

        FzsClient fzsClient = new FzsClient(dataPort);
        fzsClient.setListener(fzsEntry -> {
            if (fzsEntry instanceof FzsDmlEntry) {
                System.out.println(fzsEntry);
            }
        });
        fzsClient.start();

      /*
        call stop function to stop module
        fzsClient.stop();
        fzsAgent.stop();
       */
    }

}

```
pom.xml 中引入依赖

```
<dependency>
    <groupId>net.tbsoft</groupId>
    <artifactId>fzsclient</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.30</version>
</dependency>
<dependency>
    <groupId>dom4j</groupId>
    <artifactId>dom4j</artifactId>
    <version>1.6.1</version>
</dependency>
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-buffer</artifactId>
    <version>4.1.59.Final</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.3.2</version>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.13</version>
</dependency>
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.13</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.24</version>
</dependency>
```