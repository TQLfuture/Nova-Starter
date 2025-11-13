package com.starter.nova.web.anno;

import java.lang.annotation.*;

/**
 * @Author: tql
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLogIgnore {
}
