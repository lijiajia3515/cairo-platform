package io.github.lijiajia3515.cairo.auth.framework.lock4j;

import com.baomidou.lock.LockFailureStrategy;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class CairoLockFailureStrategy implements LockFailureStrategy {

    protected static String DEFAULT_MESSAGE = "请稍候再试";

    @Override
    public void onLockFailure(String key, Method method, Object[] arguments) {
        throw new ConflictBusinessException(DEFAULT_MESSAGE);
    }
}
