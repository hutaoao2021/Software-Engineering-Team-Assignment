package com.yrp.dto;


import com.yrp.pojo.Dish;
import com.yrp.pojo.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();


    private String categoryName;


    private Integer copies;


}
