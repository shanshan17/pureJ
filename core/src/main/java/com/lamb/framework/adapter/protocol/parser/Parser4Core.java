package com.lamb.framework.adapter.protocol.parser;

import com.lamb.framework.adapter.protocol.constant.AdapterConfConstants;
import com.lamb.framework.base.Context;
import com.lamb.framework.channel.constant.ServicePacketConstants;
import com.lamb.framework.exception.ServiceRuntimeException;
import com.lamb.framework.validator.ConfigValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>Title : 框架服务报文解析器</p>
 * <p>Description : 解析本框架服务报文</p>
 * <p>Date : 2017/4/16 13:33</p>
 *
 * @author : hejie (hjnlxuexi@126.com)
 * @version : 1.0
 */
@Component
public class Parser4Core implements IParser {
    private static Logger logger = LoggerFactory.getLogger(Parser4Core.class);
    /**
     * 是否验证响应数据
     */
    @Value("${adapter.validate.output:false}")
    private boolean isValidateOutputData;
    /**
     * 服务执行成功状态码
     */
    @Value("${service.success.code:0000}")
    private String successCode;
    /**
     * 解析报文，数据总线中流向  ResponseData--->params
     * @param context 数据总线
     * @param adapterConfig 适配器配置
     */
    @Override
    public void parse(Context context, Map adapterConfig) {
        logger.debug("解析外部服务【"+adapterConfig.get(AdapterConfConstants.NAME_TAG)+"】请求报文，开始...");
        long start = System.currentTimeMillis();
        //1、解析报文头
        this.parseHeader(context , adapterConfig);
        //2、解析报文体
        this.parseBody(context , adapterConfig);

        long end = System.currentTimeMillis();
        logger.debug("解析外部服务服务【"+adapterConfig.get(AdapterConfConstants.NAME_TAG)+"】请求报文，结束【"+(end-start)+"毫秒】");
    }

    /**
     * 解析报文头
     * @param context 数据总线
     * @param adapterConfig 适配器配置
     */
    private void parseHeader(Context context, Map adapterConfig){
        Map data = (Map)context.getResponseData();
        Map header = (Map)data.get(ServicePacketConstants.HEADER);
        Object _status = header.get(ServicePacketConstants.STATUS);
        //1、状态码为空则表示失败
        if (_status==null)
            throw new ServiceRuntimeException("5002" , this.getClass() , adapterConfig.get(AdapterConfConstants.NAME_TAG));
        Object _msg = header.get(ServicePacketConstants.MSG);
        String status = _status.toString();
        String msg = _msg==null ? "" : _msg.toString();
        //2、状态码不为0000，则表示失败
        if (!status.equals(successCode))
            throw new ServiceRuntimeException("5003" , this.getClass() , adapterConfig.get(AdapterConfConstants.NAME_TAG) , msg);
    }

    /**
     * 解析报文体，数据总线中流向  ResponseData--->params
     * @param context 数据总线
     * @param adapterConfig 适配器配置
     */
    private void parseBody(Context context, Map adapterConfig){
        Map data = (Map)context.getResponseData();
        Map body = (Map)data.get(ServicePacketConstants.BODY);
        Object outputObj = adapterConfig.get(AdapterConfConstants.OUTPUT_TAG);
        if (outputObj==null)//服务配置结构不正确
            throw new ServiceRuntimeException("5001" , this.getClass());
        List<Map> outputList = (List<Map>)outputObj;

        //1、过路交易清空动态内容
        if (context.isDirect()) context.getParams().clear();

        //2、默认不过滤和验证输出数据
        if ( !isValidateOutputData){
            context.getParams().putAll(body);
            return;
        }
        //3、遍历输出域列表
        for (Map field : outputList) {
            Object _name = field.get(AdapterConfConstants.NAME_PROP);
            if ( _name == null) //服务配置中必须存在字段名称
                throw new ServiceRuntimeException("5001" , this.getClass());
            String name = _name.toString();
            Object value = body.get(name);
            //4、验证字段值
            value = ConfigValidator.validateField(value, field);
            //5、将字段键值对放入总线
            context.getParams().put(name, value);
        }
    }
}
