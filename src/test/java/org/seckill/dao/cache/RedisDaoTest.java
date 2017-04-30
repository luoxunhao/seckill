package org.seckill.dao.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dao.SeckillDAO;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by lxh on 2017/4/30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    private long id = 1001;

    @Autowired
    private RedisDAO redisDAO;

    @Autowired
    private SeckillDAO seckillDAO;

    @Test
    public void testSecill() throws Exception {
        //get and put
        Seckill seckill = redisDAO.getSecill(id);
        if (seckill == null){
            seckill = seckillDAO.queryById(id);
            if (seckill != null){
                String result = redisDAO.putSeckill(seckill);
                System.out.println(result);
                seckill = redisDAO.getSecill(id);
                System.out.println(seckill);
            }
        }
    }
}