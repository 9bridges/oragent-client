# Oragent Client

Oragent Client for Java 是 Java 开发者对接 [Oragent](https://github.com/tb-soft/synjq-web/pkgs/container/oragent) 的最佳途径。以下示例可以帮助 Java 开发者快速上手，并使用 [Oragent](https://github.com/tb-soft/synjq-web/pkgs/container/oragent) 的全部开放能力。

## 添加依赖

在 `pom.xml` 中引入依赖：

```xml
<dependency>
  <groupId>net.tbsoft</groupId>
  <artifactId>oragent-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

## 使用方法

### 前置条件

[Oragent](https://github.com/tb-soft/synjq-web/pkgs/container/oragent) 的增量抽取进程和 web 已经成功启动。

### 代码示例

创建 `FzsAgent` 和 `FzsClient` 对象，并分别启动两个模块。

以下例子中展示了如何打印出从 FZS 获取到的 DML 操作增量数据

```Java
import net.tbsoft.fzsclient.FzsAgent;
import net.tbsoft.fzsclient.FzsClient;
import net.tbsoft.fzsclient.agent.SimpleFzsAgent;
import net.tbsoft.fzsclient.client.entry.FzsDmlEntry;

public class Example {
    public static void main(String[] args) {
        // 创建并启动 FzsAgent
        FzsAgent fzsAgent = SimpleFzsAgent.getInstance()
            .hostname("192.168.31.222") // database host
            .dbPort(1521) // database port
            .clientHost("192.168.31.206") //fzs client host
            .database("lhr11g") // database service name or sid
            .tableList("fzy.test,fzy.test2") // white table list
            .username("fzs1") // database user name
            .password("fzs1") // database password
            .dataPort(8304) // data port
            .webPort(8303); // web port
        fzsAgent.start();

        // 创建，启动 FzsClient 并挂载增量数据监听：
        FzsClient fzsClient = new FzsClient(dataPort);
        fzsClient.setListener(fzsEntry -> {
            if (fzsEntry instanceof FzsDmlEntry) {
                // 在这里获取 DML 增量
                System.out.println(fzsEntry);
            }
        });
        fzsClient.start();

        /*
         * 调用 stop() 方法来停止 FzsAgent 与 FzsClient
         * fzsClient.stop();
         * fzsAgent.stop();
         */
    }
}
```

## 加入社群

使用 Oragent Client 中有任何问题，进群解惑：

![群二维码](https://image-1302181629.cos.ap-beijing.myqcloud.com/contact_me_qr.png)
