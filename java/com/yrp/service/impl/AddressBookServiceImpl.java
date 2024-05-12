package com.yrp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yrp.dao.AddressBookDao;
import com.yrp.pojo.AddressBook;
import com.yrp.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookDao, AddressBook> implements AddressBookService {

}
