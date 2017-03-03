package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by lxh on 2017/3/3.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDAOTest {

    @Resource
    private SuccessKilledDAO successKilledDAO;

    @Test
    public void insertSuccessKilled() throws Exception {
        /*
         Preparing: INSERT ignore INTO success_killed ( seckill_id, user_phone ) VALUES ( ?, ? )
         Parameters: 1000(Long), 12345678911(Long)
         第一次：Updates: 1  insertCount = 1
         第二次：Updates: 0  insertCount = 0
         第三次：Updates: 0  insertCount = 0
         */
        long id = 1001L;
        long phone = 12345678911L;
        int insertCount = successKilledDAO.insertSuccessKilled(id, phone);
        System.out.println("insertCount = " + insertCount);
    }

    @Test
    public void queryByIdWithSeckill() throws Exception {
        long id = 1001L;
        long phone = 12345678911L;
        SuccessKilled successKilled = successKilledDAO.queryByIdWithSeckill(id, phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());
        /*
        Preparing: SELECT sk.seckill_id, sk.user_phone, sk.create_time, sk.state, s.seckill_id "seckill.seckill_id", s.name "seckill.name", s.number "seckill.number", s.start_time "seckill.start_time", s.end_time "seckill.end_time", s.create_time "seckill.create_time"
        FROM success_killed sk INNER JOIN seckill s ON sk.seckill_id = s.seckill_id WHERE sk.seckill_id = ? AND sk.user_phone = ?
        Parameters: 1000(Long), 12345678911(Long)
        SuccessKilled{
        seckillId=1000,
        userPhone=12345678911,
        state=-1,
        createTime=Fri Mar 03 10:30:53 CST 2017
        }
        Seckill{
        seckillId=1000,
        name='1000元秒杀iphone6',
        number=0,
        startTime=Thu Mar 02 00:00:00 CST 2017, e
        ndTime=Fri Mar 03 00:00:00 CST 2017,
        createTime=Thu Mar 02 16:48:06 CST 2017
        }
         */
    }

}