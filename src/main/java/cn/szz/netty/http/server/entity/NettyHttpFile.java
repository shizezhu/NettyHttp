package cn.szz.netty.http.server.entity;

import static cn.szz.netty.http.util.CommUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.netty.handler.codec.http.multipart.FileUpload;

/**
 * 文件上传
 *
 * @author Shi Zezhu
 * @date 2019年8月7日 下午6:57:33
 */
public class NettyHttpFile {

    private String name;

    private byte[] data;

    private NettyHttpFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public static NettyHttpFile getInstance(FileUpload fileUpload) {
        notNull(fileUpload, "fileUpload");
        NettyHttpFile nettyHttpFile = new NettyHttpFile();
        nettyHttpFile.setName(fileUpload.getFilename());
        nettyHttpFile.setData(byteBufToBytes(fileUpload.content()));
        return nettyHttpFile;
    }

    public void transferTo(String name) throws IOException {
        notEmpty(name, "name");
        this.transferTo(new File(name));
    }

    public void transferTo(File dest) throws IOException {
        notNull(dest, "dest");
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(this.getData());
            os.flush();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
