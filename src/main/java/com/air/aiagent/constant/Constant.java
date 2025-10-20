package com.air.aiagent.constant;

import java.util.List;

/**
 * @author WyH524
 * @since 2025/9/30 13:12
 */
public interface Constant {

    String VERIFICATIONCODE = "loveai:verificationCode:";

    /**
     * 随机用户昵称
     */
    List<String> USER_NICK_NAME_PREFIX = List.of("咖啡不加糖","熬夜冠军","西瓜味的夏天","走路带风","发呆专业户"
            ,"爱吃薯片的猫","周末不上班","懒人小助手","快乐小肥宅","今天也要加油鸭");


    String LOGIN_USER = "login_user";

    /**
     * 文件保存目录
     * 由于会影响系统资源，所以我们需要将文件统一存放到一个隔离的目录进行存储，
     * 在 constant 包下新建文件常量类，约定文件保存目录为项目根目录下的 /tmp 目录中。
     */
    String FILE_SAVE_DIR = System.getProperty("user.dir") + "/tmp";
    // 通常是项目的根目录（与 pom.xml 或 build.gradle 同级）


    /**
     * 游戏的会话名称，之后我们如果想要删除游戏的聊天记录的话，可以删除这个 sessionName
     */
    String GAME_SESSION_NAME = "d4728f1a-5b3e-4c89-b12f-7a9c83e14f21";
}
