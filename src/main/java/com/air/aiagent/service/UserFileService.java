package com.air.aiagent.service;
import com.air.aiagent.domain.entity.UserFile;
import com.air.aiagent.domain.vo.UserFileVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 30280
* @description 针对表【file(文件存储表)】的数据库操作Service
* @createDate 2025-09-28 19:28:32
*/
public interface UserFileService extends IService<UserFile> {

    /**
     * 获取用户的文件存储列表
     */
    List<UserFileVO> getUserFileList(Long userId);
}
