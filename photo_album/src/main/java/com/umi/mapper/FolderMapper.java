package com.umi.mapper;

import com.umi.pojo.Folder;
import com.umi.pojo.FolderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FolderMapper {
    long countByExample(FolderExample example);

    int deleteByExample(FolderExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Folder record);

    int insertSelective(Folder record);

    List<Folder> selectByExample(FolderExample example);

    Folder selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Folder record, @Param("example") FolderExample example);

    int updateByExample(@Param("record") Folder record, @Param("example") FolderExample example);

    int updateByPrimaryKeySelective(Folder record);

    int updateByPrimaryKey(Folder record);
}