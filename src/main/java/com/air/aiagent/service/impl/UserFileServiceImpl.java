package com.air.aiagent.service.impl;


import com.air.aiagent.domain.entity.UserFile;
import com.air.aiagent.domain.vo.UserFileVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.air.aiagent.service.UserFileService;
import com.air.aiagent.mapper.UserFileMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
* @author 30280
* @description 针对表【file(文件存储表)】的数据库操作Service实现
* @createDate 2025-09-28 19:28:32
*/
@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements UserFileService {

    /**
     * 获取用户文件列表
     */
    @Override
    public List<UserFileVO> getUserFileList(Long userId) {
        List<UserFile> userFilelist = this.lambdaQuery().eq(UserFile::getUserId, userId).list();
        if(userFilelist.isEmpty()){
            return List.of();
        }
        return userFilelist.stream()
                .map(file -> UserFileVO.builder()
                        .id(file.getId())
                        .fileUrl(file.getFileUrl())
                        .fileName(file.getFileName())
                        .createTime(file.getCreateTime())
                        .build())
                // 添加 sorted 操作，根据创建时间升序排序
                .sorted(Comparator.comparing(UserFileVO::getCreateTime))
                .toList();
    }
}




