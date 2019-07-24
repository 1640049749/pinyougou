package com.pinyougou.shop.test;

import com.pinyougou.common.util.FastDFSClient;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import tk.mybatis.mapper.entity.Example;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.shop.test *
 * @since 1.0
 */
public class FastdfsTest {

    //上传图片
    @Test
    public void upload() throws Exception {
        //1.创建一个配置文件 文件用于存储服务器的ip地址和端口
        //2.加载配置文件
        ClientGlobal.init("C:\\Users\\Administrator\\IdeaProjects\\60pinyougou\\pinyougou-shop-web\\src\\main\\resources\\config\\fastdfs_client.conf");
        //3.创建trackerclient
        TrackerClient trackerClient = new TrackerClient();
        //4.获取trackerserver
        TrackerServer trackerServer = trackerClient.getConnection();
        //5.创建storageClient
        StorageClient storageClient = new StorageClient(trackerServer,null);
        //6.上传图片
        //第一个参数：指定本地图片文件的路径
        //第二个参数：文件的扩展名  不要带“.”
        //第三个参数：文件的元数据
        String[] jpgs = storageClient.upload_file("C:\\Users\\Administrator\\Pictures\\timg.jpg", "jpg", null);
        for (String jpg : jpgs) {
            System.out.println(jpg);
        }
    }

    @Test
    public void fastdfsclient() throws Exception{
        FastDFSClient fastDFSClient = new FastDFSClient("C:\\Users\\Administrator\\IdeaProjects\\60pinyougou\\pinyougou-shop-web\\src\\main\\resources\\config\\fastdfs_client.conf");
        String s = fastDFSClient.uploadFile("C:\\Users\\Administrator\\Pictures\\5b13cd6cN8e12d4aa.jpg", "jpg");
        System.out.println(s);
    }
}
