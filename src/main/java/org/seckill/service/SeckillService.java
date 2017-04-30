package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口：站在使用者角度定义接口
 * 三个方面：方法定义粒度，参数，返回类型（return 类型/异常）
 * Created by lxh on 2017/4/27.
 */
public interface SeckillService {
    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(Long seckillId);

    /**
     *  秒杀开启时输出秒杀接口地址，
     *  否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(Long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(Long seckillId, Long userPhone, String md5)
        throws SeckillException, RepeatKillException, SeckillCloseException;

    SeckillExecution executeSeckillByProcedure(Long seckillId, Long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;
}
