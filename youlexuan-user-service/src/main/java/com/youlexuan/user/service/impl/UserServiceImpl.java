package com.youlexuan.user.service.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.youlexuan.util.CONSTANT;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.mapper.TbUserMapper;
import com.youlexuan.pojo.TbUser;
import com.youlexuan.pojo.TbUserExample;
import com.youlexuan.pojo.TbUserExample.Criteria;
import com.youlexuan.user.service.UserService;

import com.youlexuan.entity.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.DigestUtils;

import javax.jms.*;

/**
 * 用户表服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;

	@Autowired
    private RedisTemplate redisTemplate;

	@Autowired
	private Destination smsSendDestination;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Value("${template_code}")
	private String template_code;

	@Value("${sign_name}")
	private String sign_name;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		user.setCreated(new Date());//创建日期
		user.setUpdated(new Date());//修改日期

		//对密码加密
		String password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
		user.setPassword(password);
		userMapper.insert(user);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    /**
     * 生成短信验证码
     * 1.生成验证码
     * 2.缓存验证码
     * 3.发送验证码[放到mq]
     * @param phone
     */
    @Override
    public void createSmsCode(String phone) {
        //1.生成6位随机数
        String code = "";
        for(int i = 0;i<6;i++){
        	code+=(int)(Math.random()*10)+"";
		}
		String finalcode = code;
        System.out.println("验证码："+finalcode);
        //2.存入缓存,并且设置缓存时间
        redisTemplate.boundHashOps(CONSTANT.SMS_KEY).put(phone, finalcode);
        //设置过期时间
        redisTemplate.boundHashOps(CONSTANT.SMS_KEY).expire(5, TimeUnit.MINUTES);
        //发送到activeMQ   加工发送的消息

		jmsTemplate.send(this.smsSendDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				Map param = new HashMap();
				param.put("code",finalcode);
				//map.put("param",param);//xxx你好,你的验证码是...
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setString("mobile",phone);//手机号
				mapMessage.setString("template_code",template_code);//短信模板
				mapMessage.setString("sign_name",sign_name);//签名
				mapMessage.setObject("param", JSON.toJSONString(param));
				return mapMessage;
			}
		});
    }

	/**
	 * 判断验证码是否正确
	 * @param phone
	 * @param iCode
	 * @return
	 */
	@Override
	public boolean checkSmsCode(String phone, String iCode) {
		//得到缓存中存储的验证码
		String sysCode = (String) redisTemplate.boundHashOps(CONSTANT.SMS_KEY).get(phone);
		if(sysCode==null){//缓存中不存在
			return false;
		}
		if(!sysCode.equals(iCode)){//生成的与输入的不相等
			return false;
		}
		return true;
	}

}
