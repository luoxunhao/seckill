package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDAO;
import org.seckill.dao.SuccessKilledDAO;
import org.seckill.dao.cache.RedisDAO;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnums;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.*;

/**
 * Created by lxh on 2017/4/27.
 */
//@Component @Service @Dao @Controller
@Service
public class SeckillServiceImpl implements SeckillService {
    private static final String salt = "ashds()_cljcnl*cxho$%#%$";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //注入service依赖
    @Autowired
    private SeckillDAO seckillDAO;

    @Autowired
    private SuccessKilledDAO successKilledDAO;

    @Autowired
    private RedisDAO redisDAO;

    public List<Seckill> getSeckillList() {
        return seckillDAO.queryAll(0,10);
    }

    public Seckill getById(Long seckillId) {
        return seckillDAO.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(Long seckillId) {
        //优化点：缓存优化：超时的基础上维护一致性
        //1: 访问redis
        Seckill seckill = redisDAO.getSecill(seckillId);
        if (seckill == null){
            //2:访问数据库
            seckill = seckillDAO.queryById(seckillId);
            if (seckill == null){
                return new Exposer(false, seckillId);
            }else {
                //3:放入redis
                redisDAO.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()){
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        String md5 = getMD5(seckillId);
        return new Exposer(true, seckillId, md5);
    }

    private String getMD5(long seckillId){
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1：开发团队达成一致约定，明确标注事务方法的编程风格。
     * 2：保证实务方法的执行时间尽可能短，不要穿插其他网络操作 RPC/HTTP请求或者剥离到事务方法外部。
     * 3：不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制。
     */
    public SeckillExecution executeSeckill(Long seckillId, Long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();
        try {
            //记录购买行为
            int insertCount = successKilledDAO.insertSuccessKilled(seckillId, userPhone);
            //唯一：seckillId, userPhone
            if (insertCount <= 0){
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            }else {
                //减库存，热点商品竞争
                int updateCount = seckillDAO.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0){
                    //没有更新到记录，秒杀结束，rollback
                    throw new SeckillCloseException("seckill is close");
                }else {
                    //秒杀成功，commit
                    SuccessKilled successKilled = successKilledDAO.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnums.SUCCESS, successKilled);
                }
            }
        }catch (SeckillCloseException e1){
            throw e1;
        }catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            //所有编译期异常转换为运行期异常，这样声明式事务会roll back
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    public SeckillExecution executeSeckillByProcedure(Long seckillId, Long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId, SeckillStateEnums.DATA_REWRITE);
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date now = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", now);
        map.put("result", null);
        //执行存储过程，result被赋值
        try {
            seckillDAO.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1){
                SuccessKilled sk = successKilledDAO.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnums.SUCCESS, sk);
            }else {
                return new SeckillExecution(seckillId, SeckillStateEnums.stateOf(result));
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnums.INNER_ERROR);
        }
    }
}
