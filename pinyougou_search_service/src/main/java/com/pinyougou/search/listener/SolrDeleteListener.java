package com.pinyougou.search.listener;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

/**
 * 监听类
 * 删除索引记录
 * @author hongbin
 */
public class SolrDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] goodsIds = (Long[]) objectMessage.getObject();
                System.out.println("ItemDeleteListener监听接收到消息..."+goodsIds);
                itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
                System.out.println("成功删除索引库中的记录");

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
