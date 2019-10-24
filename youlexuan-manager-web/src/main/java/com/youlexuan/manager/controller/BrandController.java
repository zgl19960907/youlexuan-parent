package com.youlexuan.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.entity.PageResult;
import com.youlexuan.entity.Result;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;
    /**
     * 返回全部列表
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页列表  分页
     */
    @RequestMapping("/findPage")
    public PageResult findPage(@RequestParam("page") int pageNum, @RequestParam("rows") int pageSize){
        return brandService.findPage(null,pageNum,pageSize);
    }

    @RequestMapping("/search")
    public PageResult search(
            @RequestBody TbBrand brand,
            @RequestParam("page") int pageNum, @RequestParam("rows") int pageSize){
        return brandService.findPage(brand,pageNum,pageSize);
    }

    //增加品牌
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"增加成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"增加失败");
        }
    }

    //修改品牌
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    //修改品牌前先查出来
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }

    //批量删除
    @RequestMapping("/delet")
    public Result delet(Long[] ids){
        try {
            brandService.delet(ids);
            return new Result(true,"删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    //从数据库获取品牌信息
    @RequestMapping("/selectOption")
    public List<Map> selectOption(){
        return brandService.selectOption();
    }

}
