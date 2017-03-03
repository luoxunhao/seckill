package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by lxh on 2017/3/2.
 * 配置spring和junit整合，junit启动时加在springIOC容器
 * spring-test, junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDAOTest {

    @Resource
    private SeckillDAO seckillDAO;

    @Test
    public void queryById() throws Exception {
        long id = 1000L;
        Seckill seckill = seckillDAO.queryById(id);
        System.out.println(seckill.getName());
        System.out.print(seckill.toString());
    }

    @Test
    public void queryAll() throws Exception {
        //Parameter 'offset' not found. Available parameters are [0, 1, param1, param2]
        // java没有保存形参的记录：queryAll(int offset, int limit) -> queryAll(arg0, arg1)
        List<Seckill> seckills = seckillDAO.queryAll(0,100);
        for (Seckill seckill : seckills){
            System.out.println(seckill.getName());
        }
    }

    @Test
    public void reduceNumber() throws Exception {
        /*
         JDBC Connection [com.mchange.v2.c3p0.impl.NewProxyConnection@72bc6553] will not be managed by Spring
         Preparing: UPDATE seckill SET number = number - 1 WHERE seckill_id = ? AND start_time <= ? AND end_time >= ? AND number > 0
         Parameters: 1000(Long), 2017-03-03 10:14:04.696(Timestamp), 2017-03-03 10:14:04.696(Timestamp)
         Updates: 0
         */
        Date killTime = new Date();
        int updateCount = seckillDAO.reduceNumber(1000L, killTime);
        System.out.println("updateCount: " + updateCount);
    }
}