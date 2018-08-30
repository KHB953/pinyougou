package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.List;
import java.util.Map;

/**
 * 索引更新监听类
 * 执行manager 传过来的ids 来更新索引库
 * @author hongbin
 */
public class SolrUpdateListener implements MessageListener {

    @Autowired
    private ItemService itemService;

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        //接收消息并更新索引 1.接收消息 消息内容就是Long[] ids
        if (message instanceof ObjectMessage){
            System.out.println("更新索引库开始");
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] ids = (Long[]) objectMessage.getObject();
                //2.根据iD从数据库中获取商品的数据
                List<TbItem> items = itemService.findItemsByIds(ids);

                //3.调用solrtemplate的更新索引库的方法 将数据更新到索引库中
                //设置规格对应的动态域
                for (TbItem tbItem : items) {
                    String spec = tbItem.getSpec();
                    Map map = JSON.parseObject(spec, Map.class);
                    tbItem.setSpecMap(map);
                }
                solrTemplate.saveBeans(items);
                solrTemplate.commit();
                System.out.println("更新索引库结束");
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
