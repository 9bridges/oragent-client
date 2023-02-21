# fzsclient

��������ͬ������ FZS ���������������ṩ�Ľӿ�
## ������

```
mvn clean package
```

�������jar ���� target Ŀ¼�¡�
## ʹ�÷���
### ǰ������
FZS ��������ȡ���̺� web �Ѿ��ɹ�������
### client ���÷���
���� FzsAgent �� FzsClient ���󣬷ֱ���������ģ�飬����������չʾ��δ�ӡ���� FZS ��ȡ�������� DML ��������
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
pom.xml ����������

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