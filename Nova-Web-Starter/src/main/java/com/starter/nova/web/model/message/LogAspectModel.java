/*
 * Copyright © 2019 collin (1634753825@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.starter.nova.web.model.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 *
 * @author tql
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogAspectModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求方向：IN代表服务被外部调用，OUT代表服务调用外部
     */
    @JsonPropertyOrder(value = "0")
    private String direction;

    /**
     * 请求路径
     */
    @JsonPropertyOrder(value = "10")
    private String url;
    /**
     * http请求方式
     */
    @JsonPropertyOrder(value = "20")
    private String method;
    /**
     * 花费时间（毫秒）
     */
    @JsonPropertyOrder(value = "30")
    private Long cost;
    /**
     * http头部数据
     */
    @JsonPropertyOrder(value = "40")
    private Object head;
    /**
     * url参数
     */
    @JsonPropertyOrder(value = "50")
    private String queryParams;
    /**
     * body部分请求体参数
     */
    @JsonPropertyOrder(value = "60")
    private Object args;
    /**
     * 请求结果
     */
    @JsonPropertyOrder(value = "70")
    private Object result;

    @Override
    public String toString() {
        String resultStr = ReflectionToStringBuilder.toString(result);
        if (StringUtils.isNotEmpty(resultStr)) {
            if (resultStr.length() > 4096) {
                resultStr = resultStr.substring(0, 4096);
            }
            this.result = resultStr;
        }
        return ReflectionToStringBuilder.toString(this);
    }
}