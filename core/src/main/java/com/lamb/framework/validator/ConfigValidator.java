package com.lamb.framework.validator;

import com.lamb.framework.adapter.protocol.constant.AdapterConfConstants;
import com.lamb.framework.channel.constant.ServiceConfConstants;
import com.lamb.framework.exception.ServiceRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title : 服务配置验证器</p>
 * <p>Description : 验证输入输出数据的正确性</p>
 * <p>Date : 2017/3/6 9:15</p>
 *
 * @author : hejie (hjnlxuexi@126.com)
 * @version : 1.0
 */
public class ConfigValidator {
    private static Logger logger = LoggerFactory.getLogger(ConfigValidator.class);

    /**
     * 验证目标数据合法性
     * @param value 数据
     * @param field 字段对象
     */
    public static Object validateField(Object value,Map field){
        logger.debug("通过服务配置验证数据，开始...");
        long start = System.currentTimeMillis();
        //1、必输项校验
        if (field.get(ServiceConfConstants.REQUIRED_PROP) != null
                && Boolean.valueOf(field.get(ServiceConfConstants.REQUIRED_PROP).toString())
                && value==null)//必要字段不能为空
            throw new ServiceRuntimeException("1006" , ConfigValidator.class , field.get(ServiceConfConstants.NAME_PROP));

        //2、字符串类型校验
        if(value!=null && field.get(ServiceConfConstants.TYPE_PROP).equals(ServiceConfConstants.FIELD_TYPE_S)){//字符串型
            if (!(value instanceof String)) //字段必须为字符串类型
                throw new ServiceRuntimeException("1007" , ConfigValidator.class, field.get(ServiceConfConstants.NAME_PROP));
            if (field.get(ServiceConfConstants.REGEXP_PROP)!=null
                    && !field.get(ServiceConfConstants.REGEXP_PROP).toString().isEmpty()){//正则表达式判断
                String regEx = field.get(ServiceConfConstants.REGEXP_PROP).toString();
                Pattern pattern = Pattern.compile(regEx);
                Matcher matcher = pattern.matcher(value.toString());
                if (!matcher.matches()) //字符串格式不正确
                    throw new ServiceRuntimeException("1008" , ConfigValidator.class , field.get(ServiceConfConstants.NAME_PROP));
            }
        }
        //3、整型判断
        if(value!=null && field.get(ServiceConfConstants.TYPE_PROP).equals(ServiceConfConstants.FIELD_TYPE_I)){
            try{
                Integer.valueOf(value.toString());
            }catch (NumberFormatException e){//字段必须为整数
                throw new ServiceRuntimeException("1007" , ConfigValidator.class , e , field.get(ServiceConfConstants.NAME_PROP));
            }
        }
        //4、浮点数判断
        if(value!=null && field.get(ServiceConfConstants.TYPE_PROP).equals(ServiceConfConstants.FIELD_TYPE_F)){
            try{
                Float.valueOf(value.toString());
            }catch (NumberFormatException e){//字段必须为浮点数
                throw new ServiceRuntimeException("1007" , ConfigValidator.class , e , field.get(ServiceConfConstants.NAME_PROP));
            }
        }
        //5、布尔值判断
        if(value!=null && field.get(ServiceConfConstants.TYPE_PROP).equals(ServiceConfConstants.FIELD_TYPE_B)){
            try{
                Boolean.valueOf(value.toString());
            }catch (NumberFormatException e){//字段必须为布尔值
                throw new ServiceRuntimeException("1007" , ConfigValidator.class, e , field.get(ServiceConfConstants.NAME_PROP));
            }
        }
        //5、列表类型判断
        if(value!=null && field.get(ServiceConfConstants.TYPE_PROP).equals(ServiceConfConstants.FIELD_TYPE_E)){
            if (!(value instanceof List)) //字段必须为列表类型
                throw new ServiceRuntimeException("1007" , ConfigValidator.class , field.get(ServiceConfConstants.NAME_PROP));
            List<Map> list = (List<Map>)value;//数据列表
            List<Map> newList = new ArrayList<Map>();
            for (Map record : list) {//每条记录
                Map newParam = new HashMap();
                List<Map> fields = (List<Map>)field.get(ServiceConfConstants.FIELD_KEY_LIST);//字段列表
                for (Map f : fields) {//每个字段
                    String name = f.get(ServiceConfConstants.NAME_PROP).toString();
                    Object v = record.get(name);
                    //递归验证字段值
                    v = validateField(v,f);
                    Object target_name = f.get(AdapterConfConstants.TARGET_NAME_PROP);
                    if (target_name!=null&&!target_name.toString().isEmpty()) name = target_name.toString();
                    newParam.put(name ,v);
                }
                newList.add(newParam);
            }
            value = newList;
        }
        long end = System.currentTimeMillis();
        logger.debug("通过服务配置验证数据，结束【"+(end-start)+"毫秒】");
        return value;
    }
}
