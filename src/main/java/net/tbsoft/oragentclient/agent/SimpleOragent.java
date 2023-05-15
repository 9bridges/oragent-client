package net.tbsoft.oragentclient.agent;

import lombok.extern.slf4j.Slf4j;
import net.tbsoft.oragentclient.Oragent;
import net.tbsoft.oragentclient.agent.config.AsmConfig;
import net.tbsoft.oragentclient.agent.config.AsmMode;
import net.tbsoft.oragentclient.agent.config.OragentConfig;
import net.tbsoft.oragentclient.util.FileUtils;
import net.tbsoft.oragentclient.util.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

@Slf4j
public class SimpleOragent extends Oragent {
    private static final SimpleOragent instance = new SimpleOragent();
    private boolean running = false;

    private static final String SYNC_OBJECTS_FILE_PREFIX = "syncObjects_";
    /**
     * 主机保存的配置缓存数据
     */
    private static List<SyncObject> SYNC_OBJECTS = new ArrayList<>();

    private SimpleOragent() {
    }

    public static SimpleOragent getInstance() {
        return instance;
    }

    public static String buildExportConfig(OragentConfig oragentConfig) {

        StringBuilder export = new StringBuilder();
        export.append("src_id=").append(oragentConfig.getSrcId()).append("\n");
        export.append("src_full_cnt=").append(oragentConfig.getFullCnt()).append("\n");
        export.append("src_login=").append(oragentConfig.getSrcLogin()).append("\n");
        export.append("\n");
        for (SyncObject syncObject : SYNC_OBJECTS) {
            if (syncObject.getTableList() != null) {
                export.append("map_id=").append(syncObject.getMapId()).append("\n");
                export.append("map_use=").append(syncObject.getUsed()).append("\n");
                Arrays.stream(syncObject.getTableList()).forEach(table -> {
                    //user1.table1,user2.table2,user3.*
                    //user1.*,user2.*,user3.*
                    //(?!user1.table1)(?!user2.table2)(^user1.*|^user2.*) fixme 暂时无法处理
                    if (!StringUtils.isEmpty(table)) {
                        String[] split = table.split(",");
                        Arrays.stream(split).forEach(s -> {
                            if (s.trim().endsWith(".*")) {
                                export.append("map_user=").append(s.split("\\.")[0]).append("\n");
                            } else {
                                export.append("map_table=").append(s).append("\n");
                            }
                        });
                    }
                });
                export.append("\n");
            }
        }

        export.append("map_full_sync=").append(2047).append("\n");

        export.append("map_fix_sync=").append(3).append("\n");
        switch (oragentConfig.getStartupMode()) {
            case INITIAL:
                export.append("map_must_full_sync=").append(1).append("\n");
                break;
            case LATEST_OFFSET:
            default:
                export.append("map_must_full_sync=").append(0).append("\n");
                break;

        }
        export.append("map_if_oragent_text=").append(1).append("\n");
        export.append("map_tgt_not_drop=").append(0).append("\n");
        export.append("map_if_rid_mode=").append(1).append("\n");
        export.append("map_charset_u2g=").append(1).append("\n");
        export.append("map_index_paral_cnt=").append(8).append("\n");
        export.append("map_tgt_id=").append(oragentConfig.getTgtId()).append("\n\n");

        //fixme 目标端是否需要
        export.append("tgt_id=").append(oragentConfig.getTgtId()).append("\n");
        export.append("tgt_port=").append(oragentConfig.getDataPort()).append("\n");
        export.append("tgt_md5_port=").append(oragentConfig.getDataPort()).append("\n");
        export.append("tgt_md5_ip=").append(oragentConfig.getClientHost()).append("\n");//fixme 备端ip
        export.append("tgt_web_port=").append(oragentConfig.getWebPort()).append("\n\n");//备端web端口
        //where条件配置
        export.append("\n");

        export.append("param_no_user=").append("\n");
        export.append("param_proc_max_mem=").append(0).append("\n");//export进程最大内存
        export.append("param_left_mem=").append(1024).append("\n");//系统剩余内存
        export.append("param_cpu_max=").append(0).append("\n");//最大CPU使用率
        export.append("param_disk_quota=").append(0).append("\n");//软件最大占用磁盘空间
        export.append("param_left_space=").append(120).append("\n");//磁盘最小剩余空间(M)
//
        export.append("param_is_ddl=").append(1).append("\n");//同步ddl操作
        export.append("param_archive_log=").append(0).append("\n");//同步ddl操作
        export.append("param_tbl_drt_rnm=").append(0).append("\n");//删除表时目标端重命名
//
        export.append("param_exadata=").append(0).append("\n");//EXADATA是否大端存储
        export.append("param_off=").append(0).append("\n");//ASM磁盘轮循存储
        export.append("param_new_user=").append(0).append("\n");//是否支持新建用户增量
        export.append("param_sync_tbspace=").append(0).append("\n");//是否支持新建表看空间增量
        export.append("param_full_sync_user=").append(0).append("\n");//全库级同步是否同步用户
        export.append("param_full_sync_tbspace=").append(0).append("\n");//全库级同步是否同步表空间
        export.append("param_full_dbf=").append(0).append("\n");//全同步从表空间读取数据
        export.append("param_table_file=").append(0).append("\n");//分区file条件
        export.append("param_table_block=").append(0).append("\n");//分区block条件
        export.append("param_buf_trans=").append(0).append("\n");//是否缓存事务
//
        export.append("param_cmt_delays=").append(36).append("\n");//Oragent Params1
        export.append("param_log_check_time=").append(0).append("\n");//日志检查周期
        export.append("param_conn_error_time=").append(120).append("\n");//连接失败报错时间
        export.append("param_lock_wait=").append(120).append("\n");//全同步锁表等待时间
        export.append("param_send_cnt=").append(1).append("\n");//增量发送并发数
        AsmConfig asmConfig = oragentConfig.getAsmConfig();
        if (asmConfig != null) {
            export.append("asm_login=").append(StringUtils.isEmpty(asmConfig.getLogin()) ? "/as sysdba" : asmConfig.getLogin()).append("\n");
            export.append("asm_oracle_sid=").append(StringUtils.isEmpty(asmConfig.getOracleSid()) ? "/please input" : asmConfig.getOracleSid()).append("\n");
            export.append("asm_oracle_home=").append(StringUtils.isEmpty(asmConfig.getOracleHome()) ? "" : asmConfig.getOracleHome()).append("\n");
            export.append("asm_mode=").append(asmConfig.getMode().name().toLowerCase()).append("\n");
            for (int i = 0; !asmConfig.getMode().equals(AsmMode.DB) && i < asmConfig.getDisk().length && i < asmConfig.getDev().length; i++) {
                export.append("asm_device_id=").append(i + 1).append("\n");
                export.append("asm_disk=").append(asmConfig.getDisk()[i]).append("\n");
                export.append("asm_dev=").append(asmConfig.getDev()[i]).append("\n");
            }
        }

        log.info("export.conf=\n{}", export);
        return export.toString();
    }

    public static void updateOragentExport(OragentConfig oragentConfig) throws IOException {
        //export配置内容
        String exportConfig = buildExportConfig(oragentConfig);
        //构建xml请求
        String xmlRequest = buildXmlRequestExport(oragentConfig, exportConfig);
        log.info("request={}", xmlRequest);
        try {
            sendExport(new InetSocketAddress(oragentConfig.getHostName(), oragentConfig.getWebPort()), xmlRequest, exportConfig);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    private static void sendExport(InetSocketAddress socketAddress, String request, String content) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(socketAddress, 3000);
            socket.setSoTimeout(3000);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            outputStream.write(request.getBytes());
            byte[] res = new byte[4096];
            inputStream.read(res);
            byte[] contentBytes = content.getBytes();
            outputStream.write(contentBytes, 0, contentBytes.length);
            outputStream.flush();
        } catch (IOException e) {
            log.warn("connect {},error={}", socketAddress, e.getMessage());
            throw e;
        }
    }

    private static String buildXmlRequestExport(OragentConfig oragentConfig, String content) {
        Element element = new DefaultElement("UpdateConfig");
        element.addAttribute("op", "add")
                .addAttribute("dataLen", "" + content.getBytes().length)
                .addAttribute("type", "export")
                .addAttribute("mapid", "" + oragentConfig.getMapId())
                .addAttribute("tgtid", "" + oragentConfig.getSrcId())
                .addAttribute("cps", "0")
                .addAttribute("kafka", "0");
        return element.asXML();
    }

    @Override
    public void start() {
        try {
            //读取文件内容
            loadSyncObjects();
            //复用停止的mapId
            SyncObject syncObject = SYNC_OBJECTS.stream()
                    .filter(f -> f.getUsed() == 0)
                    .min(Comparator.comparing(SyncObject::getMapId))
                    .orElse(null);
            if (syncObject != null) {
                syncObject.setTableList(oragentConfig.getTableList());
                syncObject.setUsed(1);
                oragentConfig.setMapId(syncObject.getMapId());
            } else {
                //使用最新的mapId 最大值+1;
                int maxMapId = SYNC_OBJECTS.stream()
                        .map(SyncObject::getMapId)
                        .max(Comparator.comparing(Integer::intValue)).orElse(0);
                oragentConfig.setMapId(maxMapId+1);
                SYNC_OBJECTS.add(new SyncObject(maxMapId+1, 1, oragentConfig.getTableList()));
            }
            storeSyncObjects();
            //update export.conf
            updateOragentExport(oragentConfig);
            //启动oragent源端
            Response<byte[]> response = HttpClientUtils.httpRequestGetOragentFull(new HttpHost(oragentConfig.getHostName(), oragentConfig.getWebPort()), oragentConfig.getMapId());
            if (response.getCode() != 0) {
                log.error("start oragent:{}", response.getMsg());
            } else {
                running = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("start oragent:{}", e.getMessage());
        }

    }
    private String getCacheFileKey() {
        return SYNC_OBJECTS_FILE_PREFIX
                + this.oragentConfig.getHostName() + "_"
                + this.oragentConfig.getWebPort() + ".json";
    }

    private void storeSyncObjects() {
        try {
            FileUtils.writeObject(getCacheFileKey(), SYNC_OBJECTS);
        } catch (IOException ignored) {
        }
    }

    private void loadSyncObjects() {
        SYNC_OBJECTS = FileUtils.readObject(getCacheFileKey(), SyncObject.class);
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            try {
                //停止oragent源端
                SYNC_OBJECTS.stream().filter(f -> f.getMapId() == oragentConfig.getMapId()).findFirst().ifPresent(syncObject -> syncObject.setUsed(0));
                storeSyncObjects();

                Response<byte[]> response = HttpClientUtils.httpRequestGetOragentStop(new HttpHost(oragentConfig.getHostName(), oragentConfig.getWebPort()), oragentConfig.getMapId());
                if (response.getCode() != 0) {
                    log.error("stop oragent:{}", response.getMsg());
                } else {
                    running = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("stop oragent:{}", e.getMessage());
            }
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }
}
