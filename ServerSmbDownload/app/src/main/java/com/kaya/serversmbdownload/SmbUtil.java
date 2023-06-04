package com.kaya.serversmbdownload;



import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import java.io.IOException;
import java.util.ArrayList;

public class SmbUtil {
    private static final String TAG = SmbUtil.class.getSimpleName();
    private static final String mRemoteIp = Guider.getInstance().host;
    private static final String mRemoteShareUrl = "AstroStation";
    private static final String mUserName = "pi";
    private static final String mPassword = "raspberry";
    public static final String IMAGES_SIZE_PATH = "image";
    public static final String TASK_FLAT_SIZE_PATH = "sequence/flat";
    public static final String TASK_DARK_SIZE_PATH = "sequence/dark";
    public static final String TASK_LIGHT_SIZE_PATH = "sequence/light";
    public static final String TASK_BIAS_SIZE_PATH = "sequence/bias";

    public static final int DIRECTORY = 1;
    public static final int FILE = 0;

    private static SMBClient client = null;
    private static Session session = null;
    private static DiskShare mNormalDiskShare = null,mSizeDiskShare,mFileManagerShare;
    private static Connection connection = null;
    private static AuthenticationContext authenticationContext = null;

    public static synchronized DiskShare getNormalDiskShare() throws IOException {
        if (null == mNormalDiskShare){
            client = new SMBClient();
            connection = client.connect(mRemoteIp);
            // 创建连接会话.
            authenticationContext = new AuthenticationContext(mUserName, mPassword.toCharArray(), null);
            session = connection.authenticate(authenticationContext);
            // 操作共享文件.
            mNormalDiskShare = (DiskShare) session.connectShare(mRemoteShareUrl);
            loge("TAG", "getDiskShare: null == diskShare");
        }else if (mNormalDiskShare.isConnected()){
            loge("TAG", "getDiskShare: diskShare.isConnected()");
            return mNormalDiskShare;
        }else {
            loge("TAG", "getDiskShare: diskShare not null and !diskShare.isConnected()");
/*            try {
                if (null != diskShare) {
                    diskShare.close();
                    diskShare = null;
                }
                if (null != session) {
                    session.close();
                    session = null;
                }
                if (null != client) {
                    client.close();
                    client = null;
                }
            } catch (IOException ex) {
            }*/

            client = new SMBClient();
            connection = client.connect(mRemoteIp);
            // 创建连接会话.
            authenticationContext = new AuthenticationContext(mUserName, mPassword.toCharArray(), null);
            session = connection.authenticate(authenticationContext);
            // 操作共享文件.
            mNormalDiskShare = (DiskShare) session.connectShare(mRemoteShareUrl);
        }
        return mNormalDiskShare;
    }

    public static synchronized DiskShare getSizeDiskShare() throws IOException {
        if (null == mSizeDiskShare){
            mSizeDiskShare = (DiskShare) new SMBClient()
                    .connect(mRemoteIp)
                    .authenticate(new AuthenticationContext(mUserName, mPassword.toCharArray(), null))
                    .connectShare(mRemoteShareUrl);
            loge("TAG", "getDiskShare: getSizeDiskShare null == diskShare");
        }else if (mSizeDiskShare.isConnected()){
            loge("TAG", "getDiskShare: getSizeDiskShare diskShare.isConnected()");
            return mSizeDiskShare;
        }else {
            loge("TAG", "getDiskShare: getSizeDiskShare diskShare not null and !diskShare.isConnected()");
            mSizeDiskShare = (DiskShare) new SMBClient()
                    .connect(mRemoteIp)
                    .authenticate(new AuthenticationContext(mUserName, mPassword.toCharArray(), null))
                    .connectShare(mRemoteShareUrl);
        }
        return mSizeDiskShare;
    }

    public static synchronized DiskShare getFileManagerDiskShare() throws IOException {
        if (null == mFileManagerShare){
            mFileManagerShare = (DiskShare) new SMBClient()
                    .connect(mRemoteIp)
                    .authenticate(new AuthenticationContext(mUserName, mPassword.toCharArray(), null))
                    .connectShare(mRemoteShareUrl);
            loge("TAG", "getDiskShare: getFileManagerDiskShare null == diskShare");
        }else if (mFileManagerShare.isConnected()){
            loge("TAG", "getDiskShare: getFileManagerDiskShare diskShare.isConnected()");
            return mSizeDiskShare;
        }else {
            loge("TAG", "getDiskShare: getFileManagerDiskShare diskShare not null and !diskShare.isConnected()");
            mFileManagerShare = (DiskShare) new SMBClient()
                    .connect(mRemoteIp)
                    .authenticate(new AuthenticationContext(mUserName, mPassword.toCharArray(), null))
                    .connectShare(mRemoteShareUrl);
        }
        return mFileManagerShare;
    }

    public static ArrayList<SmbFileItem> loadRemoteFile(String path){
        ArrayList<SmbFileItem> smbFileItems = new ArrayList<>();
        try (DiskShare share = getFileManagerDiskShare()) {
            for (FileIdBothDirectoryInformation f : share.list(path)) {
                SmbFileItem smbFileItem = new SmbFileItem();
                smbFileItem.mName = f.getFileName();
                smbFileItem.mPath = path;
                smbFileItem.mSize = f.getFileAttributes();
                if (isFileDirectory(f)){
                    smbFileItem.mType = DIRECTORY;
                }else {
                    smbFileItem.mType = FILE;
                }
                logi(TAG, "loadRemoteFile: "+smbFileItem.mName );
                smbFileItems.add(smbFileItem);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return smbFileItems;
    }


    public static long getRemoteDirectorySize(String path){
        long size = 0;
        try (DiskShare share = getSizeDiskShare()) {
            for (FileIdBothDirectoryInformation f : share.list(path)) {
                size += f.getAllocationSize();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return size;
    }

    public static long getRemoteFileSize(String path,String fileName){
        long size = 0;
        if (fileName == null) {
            return size;
        }
        fileName = fileName.replace("/","");
        try (DiskShare share = getSizeDiskShare()) {
            for (FileIdBothDirectoryInformation f : share.list(path)) {
                logi(TAG, "getRemoteFileSize: "+f.getFileName()+"  loadInBackground  filne" +fileName+" 大小getEaSize "+f.getEaSize()+"  " );
                if (f.getFileName().equals(fileName)) {
                    logi(TAG, "getRemoteFileSize: "+f.getFileName()+"  loadInBackground  filne" +fileName+" 大小 getAllocationSize "+f.getAllocationSize() );
                    size = f.getAllocationSize();
                    return size;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return size;
    }

    public static void rmFile(String path){
        try (DiskShare share = getFileManagerDiskShare()) {
            share.rm(path);
        }catch (Exception e){
            loge("File rm: " + e,"smb");
            e.printStackTrace();
        }
    }
    public static void rmDirectory(String path){
        try (DiskShare share = getFileManagerDiskShare()) {
            share.rmdir(path,true);
        }catch (Exception e){
            loge("File rm: " + e,"smb");
            e.printStackTrace();
        }
    }

    public static boolean isFileDirectory(FileIdBothDirectoryInformation f){
        if (!f.getFileName().equals("") && f.getAllocationSize() == 0){
            return true;
        }
        return false;
    }
}
