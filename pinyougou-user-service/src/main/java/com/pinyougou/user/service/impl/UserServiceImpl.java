package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl extends CoreServiceImpl<TbUser>  implements UserService {

	@Value("${template_code}")
	private String templateCode;
	@Value("${sign_name}")
	private String signName;
	
	private TbUserMapper userMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private DefaultMQProducer producer;

	@Autowired
	public UserServiceImpl(TbUserMapper userMapper) {
		super(userMapper, TbUser.class);
		this.userMapper=userMapper;
	}

	/**
	 * 用户注册添加数据
	 * @param record
	 */
	@Override
	public void add(TbUser record) {
		//1.md5加密存储密码
		String password = record.getPassword();
		String passwordEncode = DigestUtils.md5DigestAsHex(password.getBytes());
		record.setPassword(passwordEncode);
		//2.补充必要的字段属性值
		record.setCreated(new Date());
		record.setUpdated(new Date());
		//3.将数据存储到数据库中
		userMapper.insert(record);

	}

	/**
	 * 创建短信验证码
	 * @param phone
	 */
	@Override
	public void createSmsCode(String phone) {
		//1.生成6位随机数字
		String code =  (long) ((Math.random() * 9 + 1) * 100000)+"";
		//2.存储到redis中
		redisTemplate.boundValueOps("ZHUCE_"+phone).set(code);//key:ZHUCE_17879818888,value:验证码
		redisTemplate.boundValueOps("ZHUCE_"+phone).expire(60L, TimeUnit.SECONDS);//设置存放在redis的时间为60秒
		//3.组装消息对象 手机号 签名 模板 验证码
		Map<String, String> map = new HashMap<>();
		map.put("mobile",phone);
		map.put("sign_name",signName);
		map.put("template_code",templateCode);
		map.put("param","{\"code\":\""+code+"\"}");
		try {
			//4.发送消息 1.依赖 2.配置文件 3.注入producer 4.发送消息
			Message message = new Message("SMS_TOPIC","SEND_MESSAGE_TAG","createSmsCode",JSON.toJSONString(map).getBytes());
			producer.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 校验短信验证码
	 * @param phone
	 * @param smscode
	 * @return
	 */
	@Override
	public boolean checkSmsCode(String phone, String smscode) {
		//得到缓存中存储的验证码
		String code = (String) redisTemplate.boundValueOps("ZHUCE_"+phone).get();
		if(code==null){
			return false;
		}
		if(!code.equals(smscode)){
			return false;
		}
		return true;
	}

	@Override
    public PageInfo<TbUser> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbUser> all = userMapper.selectAll();
        PageInfo<TbUser> info = new PageInfo<TbUser>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbUser> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }

	
	

	 @Override
    public PageInfo<TbUser> findPage(Integer pageNo, Integer pageSize, TbUser user) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();

        if(user!=null){			
						if(StringUtils.isNotBlank(user.getUsername())){
				criteria.andLike("username","%"+user.getUsername()+"%");
				//criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(StringUtils.isNotBlank(user.getPassword())){
				criteria.andLike("password","%"+user.getPassword()+"%");
				//criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(StringUtils.isNotBlank(user.getPhone())){
				criteria.andLike("phone","%"+user.getPhone()+"%");
				//criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(StringUtils.isNotBlank(user.getEmail())){
				criteria.andLike("email","%"+user.getEmail()+"%");
				//criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(StringUtils.isNotBlank(user.getSourceType())){
				criteria.andLike("sourceType","%"+user.getSourceType()+"%");
				//criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(StringUtils.isNotBlank(user.getNickName())){
				criteria.andLike("nickName","%"+user.getNickName()+"%");
				//criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(StringUtils.isNotBlank(user.getName())){
				criteria.andLike("name","%"+user.getName()+"%");
				//criteria.andNameLike("%"+user.getName()+"%");
			}
			if(StringUtils.isNotBlank(user.getStatus())){
				criteria.andLike("status","%"+user.getStatus()+"%");
				//criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(user.getHeadPic())){
				criteria.andLike("headPic","%"+user.getHeadPic()+"%");
				//criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(StringUtils.isNotBlank(user.getQq())){
				criteria.andLike("qq","%"+user.getQq()+"%");
				//criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(StringUtils.isNotBlank(user.getIsMobileCheck())){
				criteria.andLike("isMobileCheck","%"+user.getIsMobileCheck()+"%");
				//criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(StringUtils.isNotBlank(user.getIsEmailCheck())){
				criteria.andLike("isEmailCheck","%"+user.getIsEmailCheck()+"%");
				//criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(StringUtils.isNotBlank(user.getSex())){
				criteria.andLike("sex","%"+user.getSex()+"%");
				//criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
        List<TbUser> all = userMapper.selectByExample(example);
        PageInfo<TbUser> info = new PageInfo<TbUser>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbUser> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

}
