package org.seckill.service.impl;

import org.seckill.dao.SeckillDAO;
import org.seckill.dao.SuccessKilledDAO;
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
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by lxh on 2017/4/27.
 */
public class SeckillServiceImpl implements SeckillService {
    private static final String salt = "ashds()_cljcnl*cxho$%#%$";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private SeckillDAO seckillDAO;

    private SuccessKilledDAO successKilledDAO;

    public List<Seckill> getSeckillList() {
        return seckillDAO.queryAll(0,10);
    }

    public Seckill getById(long seckillId) {
        return seckillDAO.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDAO.queryById(seckillId);
        if (seckill == null){
            return new Exposer(false, seckillId);
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

    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime = new Date();

        try {
            //减库存
            int updateCount = seckillDAO.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0){
                //没有更新到记录，秒杀结束
                throw new SeckillCloseException("seckill is close");
            }else {
                //记录购买行为
                int insertCount = successKilledDAO.insertSuccessKilled(seckillId, userPhone);
                //唯一：seckillId, userPhone
                if (insertCount <= 0){
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                }else {
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
}
