package com.yrp.dto;


import com.yrp.pojo.Setmeal;
import com.yrp.pojo.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
