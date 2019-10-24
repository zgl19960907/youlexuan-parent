package com.youlexuan.page.service.impl;


import com.youlexuan.mapper.TbGoodsDescMapper;
import com.youlexuan.mapper.TbGoodsMapper;
import com.youlexuan.mapper.TbItemCatMapper;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.page.service.IItemPageService;
import com.youlexuan.pojo.TbGoods;
import com.youlexuan.pojo.TbGoodsDesc;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 给定一个goodsID,生成goodsID对应的静态HTML
 * 生成的HTML页面通过page-web工程显示,因此生成的路径是page-web工程下
 */
@Service
public class ItemPageServiceImpl implements IItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Value("${pagedir}")
    private String pageDir;

    @Override
    public boolean genItemHtml(Long goodsId) {
        /**
         * 第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker的版本号。
         * 第二步：设置模板文件所在的路径。
         * 第三步：设置模板文件使用的字符集。一般就是 utf-8.
         * 第四步：加载一个模板，创建一个模板对象。
         * 第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
         * 第六步：创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
         * 第七步：调用模板对象的 process 方法输出文件。
         * 第八步：关闭流
         */

        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map dataModel = new HashMap();
            //1.加载商品数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);
            //2.增加商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);
            //3.商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);
            //4.SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//状态为有效
            criteria.andGoodsIdEqualTo(goodsId);//指定SPU ID
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);



            Writer out = new FileWriter(pageDir+goodsId+".html");
            template.process(dataModel,out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                new File(pageDir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
