package com.youlexuan.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.youlexuan.search.service.ItemSearchService;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.util.CONSTANT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.stereotype.Component;


import java.util.*;

@Service(timeout=10000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 1.根据关键字高亮查询
     * 2.得到分类分组
     * 3.根据选择的不同的分类信息,得到对应的品牌列表和规格列表
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map searchMap) {
        if (!"".equals(searchMap.get("keywords")) && searchMap.get("keywords")!=null){
            String keywords = (String) searchMap.get("keywords");
            searchMap.put("keywords",keywords.replace(" ",""));
        }


        Map resultMap = new HashMap<>();
        //1.查询列表
        Map map = searchList(searchMap);
        resultMap.putAll(map);
        //2.根据关键字查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);
        resultMap.put("categoryList",categoryList);
        //3.
//        String category = (String)searchMap.get("category");
//        category = StringUtils.isEmpty(category)?categoryList.get(0):category;
//        Map brandAndSpecMap = searchBrandAndSpecList(category);
//        resultMap.putAll(brandAndSpecMap);
        if (categoryList.size()>0){
            resultMap.putAll(searchBrandAndSpecList(categoryList.get(0)));
        }
        return resultMap;
    }

    /**
     * 1.高亮显示
     *   1.1关键字高亮查询
     *   1.2分类过滤查询   category
     *   1.3品牌过滤查询   brand
     *   1.4规格过滤查询   网络  机身内存等
     *   1.5根据价格区间过滤
     *   1.6分页查询
     *   1.7排序
     * @param searchMap
     * @return
     */
    public Map<String, Object> searchList(Map searchMap) {

        /**
         *处理关键字中的空格
         * 多关键字处理
         *  solr会把多关键字分词,分词以后每个关键字进行查询,然后取并集
         */




        Map<String,Object> map=new HashMap<>();
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //设置高亮的域(哪个字段高亮)  1.1加工高亮的条件
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<font style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</font>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //按照关键字查询   加工查询的条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2分类过滤查询
        if (!"".equals(searchMap.get("category"))){
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3按品牌过滤
        if (!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4规格过滤
        Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
        for (String key:specMap.keySet()){
            Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key) );
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.5铵价格筛选
        if (!"".equals(searchMap.get("price"))){
            //先转成字符串,再分割
            String[] price = ((String)searchMap.get("price")).split("-");
            if (!price[0].equals("0")){//如果区间  起点  不等于0
                //过滤条件
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){//如果区间  终点  不等于*
                Criteria filterCriteria = new  Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo==null){
            pageNo = 1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize == null){
            pageSize = 20;//默认一页20条数据
        }
        query.setOffset((pageNo-1)*pageSize);//计算偏移量(从第几条记录查询)
        query.setRows(pageSize);

        //1.7新品排序
        String sortValue = (String) searchMap.get("sort");//ASC  DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue != null && !"".equals(sortValue)){
            if (sortValue.equals("ASC")){
              Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
              query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }


        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
        //遍历
        for (HighlightEntry<TbItem> h:page.getHighlighted()){//循环高亮入口集合
            TbItem item = h.getEntity();//获得原实体类
            if (h.getHighlights().size()>0&&h.getHighlights().get(0).getSnipplets().size()>0){
                //设置高亮的结果
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());//分页后的列表

        //分页相关
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数

        return map;
    }

    /**
     * 2.根据关键字查询的结果集,再根据分类分组,得到所有的分类名
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query,TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    /**
     * 3.查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
       Map map = new HashMap(3);

       //获取模板id
       Long typeId = (Long) redisTemplate.boundHashOps(CONSTANT.ITEMCAT_LIST_KEY).get(category);
       if (typeId!=null){
           //根据模板id查询品牌列表
           List brandList = (List) redisTemplate.boundHashOps(CONSTANT.BRAND_LIST_KEY).get(typeId);
           map.put("brandList",brandList);//返回添加品牌列表
           //根据模板id查询规格列表
           List specList = (List) redisTemplate.boundHashOps(CONSTANT.SPEC_LIST_KEY).get(typeId);
           map.put("specList",specList);
       }
       return map;
    }

    /**
     * 导入数据
     * @param list
     */
    @Override
    public void importList(List<TbItem> list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }



    /**
     * 删除数据
     * @param ids
     */
    @Override
    public void deleteByGoodsIds(Long[] ids) {
        System.out.println("删除商品id"+ids);
        SolrDataQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(Arrays.asList(ids));
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
