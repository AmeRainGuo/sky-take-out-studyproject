package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /*
    * 批量插入套餐菜品数据
    * @param setmealDishes
    */

    void insertBatch(List<SetmealDish> setmealDishes);

    /*
    * 批量删除套餐菜品数据
    * @param ids
    */
    void deleteBatch(List<Long> ids);

    /*
    * 根据套餐id查询菜品选项
    * @param setmealId
    * @return
    */
    @Select( "select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
