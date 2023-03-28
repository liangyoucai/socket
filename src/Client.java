import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Socket {
    private static final String C_PATH = "." + File.separator + "client";//根目录
    private static final String SERVER_IP = "127.0.0.1"; // 服务端IP
    private static final int SERVER_PORT = 8888; // 服务端端口
    private Socket client;//客户端套接字
    private FileOutputStream fos;//文件输出流
    private DataOutputStream dos;//数据输出流
    private DataInputStream dis;//数据输入流

    //构造函数
    public Client() throws IOException {
        //调用父类构造函数，初始化client
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        System.out.println("Cliect[port:" + client.getLocalPort() + "] 成功连接服务端");
    }

    //发送信息
    public void sendMessage(String message) {
        try {
            dos = new DataOutputStream(client.getOutputStream());
            dos.write(message.getBytes());
            dos.flush();//清空缓存区
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    //接收文件
    public void receiveFile() {
        try {
            dis = new DataInputStream(client.getInputStream());
            if (dis.readBoolean()) {//找到文件，接收文件并返回确认信息
                // 读取文件名和长度
                String fileName = dis.readUTF();
                long fileLength = dis.readLong();
                File directory = new File(C_PATH);
                //找不到路径则创建新路径
                if (!directory.exists()) {
                    directory.mkdir();
                }

                File file = new File(C_PATH + File.separator + fileName);
                fos = new FileOutputStream(file);
                System.out.println("======== 开始接收文件 ========");
                // 开始接收文件
                byte[] bytes = new byte[1024];
                int length, tempLength = 0;//templength检查当前读入位置
                while (tempLength <= fileLength) {//tempLength <= fileLength，没有读完文件
                    if((length = dis.read(bytes, 0, bytes.length)) == -1)//一次读取1024bytes，-1代表读到文件末尾
                        break;//结束循环
                    fos.write(bytes, 0, length);
                    fos.flush();
                    tempLength += length;//更新当前读取位置
                    System.out.print("| " + (100 * tempLength / fileLength) + "% |");
                }

                System.out.println("======== 文件接收成功 [File Name：" + fileName + "]  ========");
                sendMessage("client已接收到文件！");
            } else{//没找到文件，输出接收到的错误信息
                System.out.println(dis.readUTF());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        try{
            //1.发送文件名
            System.out.println("socket客户端已启动.../n请输入要查找的文件名：");
            Scanner sc = new Scanner(System.in);
            String filename = sc.nextLine();
            sendMessage(filename);
            //2.接收文件
            receiveFile();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //回收资源
            if (dos != null)
                dos.close();
            if (fos != null)
                fos.close();
            if (dis != null)
                dis.close();
            client.close();
            System.out.println("Client已关闭。");
        }
    }
    public static void main(String[] args) {
        try{
            //1.连接指定的服务器
            Client client = new Client();
            //2.处理信息
            client.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

