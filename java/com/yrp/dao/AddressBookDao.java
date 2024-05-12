package com.yrp.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yrp.pojo.AddressBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookDao extends BaseMapper<AddressBook> {

}