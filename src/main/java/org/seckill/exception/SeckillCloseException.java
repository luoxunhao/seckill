package org.seckill.exception;

/**
 * 秒杀关闭异常
 * Created by lxh on 2017/4/27.
 */
public class SeckillCloseException extends RuntimeException{
    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
