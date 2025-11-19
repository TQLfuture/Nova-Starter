package com.stater.nova.storage.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author tql
 * @date: 2025/10/14
 * @time: 19:24
 * @desc:
 */
@Slf4j
public class OssStsClient {
    /**
     * 获取临时访问凭证AccessTokenendpoint     eg: sts.cn-hangzhou.aliyuncs.com
     * @return
     */
    public static AssumeRoleResponse getTemporaryAccessCredentials(TreeMap<String, Object> config) {
        Parameters parameters = new Parameters();
        parameters.parse(config);
        AssumeRoleResponse response = null;
        try {
            // 添加endpoint。适用于Java SDK 3.12.0及以上版本。
            DefaultProfile.addEndpoint(parameters.region, "Sts", parameters.endpoint);
            IClientProfile profile = DefaultProfile.getProfile(parameters.region, parameters.secretId, parameters.secretKey);
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(parameters.roleArn);
            request.setRoleSessionName(parameters.roleSessionName);
            request.setDurationSeconds(parameters.duration);
            response = client.getAcsResponse(request);
        } catch (Exception e) {
            log.info("获取临时访问凭证失败,{}", e);
        }
        return response;
    }


    static class Parameters {
        String secretId;
        String secretKey;
        String bucketName;
        String region;
        String roleArn;
        String roleSessionName;
        String endpoint;
        Long duration = 1800L;

        Parameters() {
        }

        public void parse(Map<String, Object> config) {
            if (config == null) {
                throw new NullPointerException("config == null");
            } else {
                Iterator var2 = config.entrySet().iterator();

                while(var2.hasNext()) {
                    Map.Entry<String, Object> entry = (Map.Entry)var2.next();
                    String key = (String)entry.getKey();
                    if ("SecretId".equalsIgnoreCase(key)) {
                        this.secretId = (String)entry.getValue();
                    } else if ("SecretKey".equalsIgnoreCase(key)) {
                        this.secretKey = (String)entry.getValue();
                    } else if ("durationSeconds".equalsIgnoreCase(key)) {
                        this.duration = (Long) entry.getValue();
                    } else if ("bucketName".equalsIgnoreCase(key)) {
                        this.bucketName = (String)entry.getValue();
                    } else if ("roleArn".equalsIgnoreCase(key)) {
                        this.roleArn = (String)entry.getValue();
                    } else if ("region".equalsIgnoreCase(key)) {
                        this.region = (String)entry.getValue();
                    }else if ("roleSessionName".equalsIgnoreCase(key)) {
                        this.roleSessionName = (String)entry.getValue();
                    }else if ("endpoint".equalsIgnoreCase(key)) {
                        this.endpoint = (String)entry.getValue();
                    }
                }

            }
        }
    }
}
