# distribute-sequence
分布式id解决方案

# 使用说明
## 使用前提
- spring 项目
- 项目classpath中必须包含包含数据库访问相应jar包，因为segment id需要访问数据库
```text
1、mysql-connector-java，建议5.1版本

2、druid连接池或HikariCP连接池，不限制版本，二者取其一，会自动探测

3、mybatis: 不限制mybatis上层框架，只需要基本的mybatis包，不限制版本
```

- 参考如下，业务方自己保证相应包在classpath下，版本不限
```xml
<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>5.1.49</version>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.5.9</version>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.2.8</version>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>3.2.0</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## 数据库配置
- 请在业务方自己的数据库中建表
```roomsql
  CREATE TABLE `peanut_sequence` (
  `biz_tag` varchar(128)  NOT NULL DEFAULT '' COMMENT '业务标识:代表自己需要的sequence的业务key',
  `max_id` bigint(20) NOT NULL DEFAULT '1' COMMENT '初始值，默认1即可',
  `step` int(11) NOT NULL COMMENT '每次拉取步长，业务根据自己吞吐量设置',
  `description` varchar(256)  DEFAULT NULL COMMENT '业务说明,可选参数',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`biz_tag`)
  ) ENGINE=InnoDB;
  
  # 根据业务需要自己插入，可插入多条
  insert into peanut_sequence(biz_tag, max_id, step, description) values('test', 1, 10000, 'test')
```




## 依赖引入
```xml
    <dependency>
        <groupId>com.soul</groupId>
        <artifactId>infra-sequence</artifactId>
        <version>1.0.2</version>
    </dependency>
```


## 基本配置
- 本地 application.properties 或配置中心均可
```properties
# 是否启用segment id，可选配置，默认true
peanut.sequence.segment.enabled=true

# segment表所在数据库，业务方自己指定
peanut.sequence.jdbc.url=jdbc:mysql://localhost:3306/sequence-test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull

# segment表所在数据库，业务方自己指定
peanut.sequence.jdbc.username=root

# segment表所在数据库，业务方自己指定
peanut.sequence.jdbc.password=root

```

## API使用
### spring bean中使用
- 直接注入SegmentService，调用getId方法
```java
  @RestController
public class TestController {

    // 直接注入SegmentService
    @Autowired
    SegmentService segmentService;

    @GetMapping("/seq")
    public Long test1() {
        // 直接使用
        Result result = segmentService.getId("seq-test");
        long id = result.getId();
        return id;
    }

    @GetMapping("/seq2")
    public Long test2() {
        // 超时使用(毫秒数)
        long id = segmentService.tryGet("seq-test",1000);
        return id;
    }
    
}
```

### 非spring bean使用
- 该方式在spring bean 或 非spring bean 中均可使用
```text
# 注意业务key是自己的业务key
SequenceUtils.getId("业务key"); 
```