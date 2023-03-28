import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final String S_PATH = "." + File.separator + "server";//根目录
    private static final int SERVER_PORT = 8888; // 服务端端口
    private Socket clientSocket;//与client的通信套接字
    private ServerSocket server;//服务器
    private FileInputStream fis;//文件输入流
    private DataOutputStream dos;//数据输出流
    private DataInputStream dis;//数据输入流
    //构造函数
    public Server() throws IOException {
        //1.启动服务器，等待连接
        server = new ServerSocket(SERVER_PORT);
        System.out.println("Server服务器运行中...");
        //2.接收来自客户端的连接请求
        clientSocket = server.accept();
    }

    //接收信息，信息不超过1024B
    public String receiveMessage() throws IOException {
        try {
            dis = new DataInputStream(clientSocket.getInputStream());
            byte[] bytes = new byte[1024];
            int len = dis.read(bytes);//使用byte数组读入信息
            return new String(bytes, 0, len);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    //发送文件
    //若文件存在返回true,且输入流发送一个true值，反之方法返回false且输入流发送一个false值
    public boolean sendFile(String filename) throws IOException {
        try {
            File file = new File(S_PATH + File.separator + filename);
            dos = new DataOutputStream(clientSocket.getOutputStream());
            if (file.exists()) {//文件存在
                fis = new FileInputStream(file);
                dos.writeBoolean(true);
                dos.flush();
                // 文件名和长度
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                // 开始传输文件
                System.out.println("======== 开始传输文件 ========");
                byte[] bytes = new byte[1024];

                int length;//文件当前读入的最后一个指针
                long progress = 0;//当前读入进度
                //API: read(): the total number of bytes read into the buffer,
                // or -1 if there is no more data because the end of the stream has been reached.
                while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();//清空缓存区，继续读入
                    //传输进度条
                    progress += length;
                    System.out.print("| " + (100 * progress / file.length()) + "% |");
                }
                System.out.println();
                System.out.println("======== 文件传输成功 ========");
                return true;
            }else{//没有文件，返回错误信息
                dos.writeBoolean(false);
                dos.flush();
                System.out.println("服务器在目录下没有找到该文件！");
                dos.writeUTF("服务器在目录下没有找到该文件！");
                dos.flush();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void run() throws IOException {
        try {
            //1.接收查找文件名
            String filename = receiveMessage();
            System.out.println("client查找的文件名为：" + filename);
            //2.发送文件
            if(sendFile(filename))
                System.out.println(receiveMessage());//接收确认信息
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            //3.最后统一回收资源
            if (dis != null)
                dis.close();
            if (fis != null)
                fis.close();
            if (dos != null)
                dos.close();
            clientSocket.close();
            System.out.println("Server服务器已关闭。");
        }

    }
    public static void main(String[] args) {
        try {
            //1.启动服务器并接入客户端
            Server server = new Server();
            //2.处理信息
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}